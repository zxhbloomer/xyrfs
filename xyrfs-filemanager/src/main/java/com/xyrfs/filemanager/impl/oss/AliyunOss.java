package com.xyrfs.filemanager.impl.oss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSErrorCode;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.base.interfaces.OssFileSystem;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于阿里云 OSS 的文件系统实现
 * <p/>
 * 1. 开通 OSS <br/>
 * 2. 创建 bucket <br/>
 * 3. 为 bucket 绑定域名, eg: img.ponly.com --&gt; ponly.oss-cn-hongkong.aliyuncs.com <br/>
 *
 * @author vacoor
 */
public class AliyunOss implements OssFileSystem {
    private static final int DEFAULT_PERMISSIONS = 0600;

    private final String endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucketName;

    public AliyunOss(String endpoint, String accessKeyId, String secretAccessKey, String bucketName) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucketName = bucketName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String path) throws FileSystemException {
        String absPath = resolveAbsolutePath(path);
        String bucketName = getBucketName(absPath);
        return connect().doesObjectExist(bucketName, absPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileView stat(String path) throws FileSystemException {
        try {
            String absPath = resolveAbsolutePath(path);
            String bucketName = getBucketName(absPath);
            if (null == absPath || "".equals(absPath)) {
                return null;
            }
            OSSObject object = getObject(bucketName, absPath);
            FileView stat = null;

            if (null != object) {
                ObjectMetadata metadata = object.getObjectMetadata();
                String key = object.getKey();
                long length = metadata.getContentLength();
                long mtime = metadata.getLastModified().getTime();
                String objectType = metadata.getObjectType();
                String group = object.getBucketName();

                stat = new FileView(key, length, false, mtime, mtime, DEFAULT_PERMISSIONS, null, group, null);
            }

            return stat;
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileView[] ls(String path) throws FileSystemException {
        String absPath = resolveAbsolutePath(path);
        FileView stat = stat(absPath);
        /*
        if (null == stat) {
            return new FileView[0];
        }
        */
        // 对于阿里云都是文件不会有目录, 如果是文件 stat 返回 null
        if (null != stat && !stat.isDirectory()) {
            return new FileView[]{stat};
        }

        // 如果是目录, 确保是 "/" 结尾, 保证是目录
        absPath = absPath.endsWith("/") ? absPath : resolveAbsolutePath(absPath + "/");

        // 最大1000
        ListObjectsRequest listRequest = new ListObjectsRequest(getBucketName(absPath), absPath, null, null, 1000);
        ObjectListing listing = connect().listObjects(listRequest);
        List<OSSObjectSummary> summaries = listing.getObjectSummaries();
        FileView[] views = new FileView[summaries.size()];
        for (int i = 0; i < summaries.size(); i++) {
            views[i] = toView(summaries.get(i));
        }
        return views;
    }

    /**
     * 不支持该操作
     *
     * @throws FileSystemException
     */
    @Override
    public boolean mkdirs(String path) throws FileSystemException {
        throw new UnsupportedOperationException("Aliyun OSS is KEY-VALUE System, Unsupported directory.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream open(String file) throws FileSystemException {
        try {
            String absPath = resolveAbsolutePath(file);
            String bucketName = getBucketName(absPath);
            OSSObject object = getObject(bucketName, absPath);
            return null != object ? new DataInputStream(object.getObjectContent()) : null;
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream create(String file, boolean override) throws FileSystemException {
        return create(file, null, override);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream create(final String file, final String contentType, final boolean override) throws FileSystemException {
        try {
            final String absPath = resolveAbsolutePath(file);
            final String bucketName = getBucketName(absPath);

            FileView stat = stat(file);
            if (!override && null != stat) {
                throw new FileSystemException("File already exists: " + file);
            }

            final PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            Thread thread = new Thread("ALIYUN-OSS-UPLOADER") {
                @Override
                public void run() {
                    ObjectMetadata metadata = new ObjectMetadata();
                    // 默认是 application/octet-stream, 对于图片等, 某些浏览器会直接下载
                    if (null != contentType) {
                        metadata.setContentType(contentType);
                    }
                    connect().putObject(bucketName, absPath, in, metadata);
                }
            };
            thread.setDaemon(true);
            thread.start();
            return new DataOutputStream(out);
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rm(String path, boolean force, boolean recursive) throws FileSystemException {
        String absPath = resolveAbsolutePath(path);
        String bucketName = getBucketName(absPath);
        OSSClient client = connect();
        boolean deleted = false;

        DeleteObjectsRequest delRequest = new DeleteObjectsRequest(bucketName);
        DeleteObjectsResult ret;

        // 如果可能不是目录, 尝试直接删除
        if (!absPath.endsWith("/")) {
            delRequest.setKeys(Collections.singletonList(absPath));
            ret = client.deleteObjects(delRequest);
            deleted = 1 > ret.getDeletedObjects().size();
        }

        // 删除失败或不是文件
        if (!deleted) {
            List<String> keys;
            Set<String> faildKeys = Sets.newHashSet();      // 删除失败 Keys

            // 确保是目录
            String dirPath = resolveAbsolutePath(absPath.endsWith("/") ? absPath : absPath + "/");
            // 最大 1000
            ListObjectsRequest listRequest = new ListObjectsRequest(bucketName, dirPath, null, null, 1000);

            // 分批次删除
            while (!faildKeys.containsAll(keys = getKeys(client.listObjects(listRequest).getObjectSummaries()))) {
                // 如果目录非空, 且非强制删除所有文件
                if (!force && 0 < keys.size()) {
                    throw new FileSystemException("You cannot delete non-empty directory, use force=true to overide");
                }
                if (!recursive) {
                    for (String key : keys) {
                        // 如果非递归但存在目录
                        if (key.substring(dirPath.length()).contains("/")) {
                            throw new FileSystemException("Directory has contents, cannot delete without recurse=true");
                        }
                    }
                }
                DeleteObjectsRequest delChildRequest = new DeleteObjectsRequest(bucketName);
                delChildRequest.setKeys(keys);
                ret = client.deleteObjects(delChildRequest);

                // 记录删除失败的对象
                keys.removeAll(ret.getDeletedObjects());
                if (0 < keys.size()) {
                    throw new FileSystemException("cannot delete file : " + keys);
                }
                faildKeys.addAll(keys);
            }
            ret = client.deleteObjects(delRequest);
            deleted = 1 > ret.getDeletedObjects().size();
        }
        if (!deleted) {
            throw new FileSystemException("cannot delete : " + absPath);
        }
    }

    /**
     * 不支持该操作
     */
    @Override
    public void rename(String oldPath, String newPath) throws FileSystemException {
        // FIXME 目录如何处理？
        throw new UnsupportedOperationException("aliyun oss rename is unsupported");
    }

    protected String resolveAbsolutePath(String path) {
        if (null == path) {
            throw new IllegalArgumentException("path muse be not empty");
        }
        // 根目录为 ""
        if ("/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    protected String getBucketName(String absPath) {
        return bucketName;
    }

    protected OSSClient connect() {
        return new OSSClient(endpoint, accessKeyId, secretAccessKey);
    }

    private OSSObject getObject(String bucketName, String path) throws IOException {
        String absPath = resolveAbsolutePath(path);
        OSSObject object;
        try {
            object = connect().getObject(bucketName, absPath);
        } catch (OSSException ex) {
            if (OSSErrorCode.NO_SUCH_KEY.equals(ex.getErrorCode())) {
                object = null;
            } else {
                throw new IOException(ex);
            }
        }
        return object;
    }

    private FileView toView(OSSObjectSummary summary) {
        if (null == summary) {
            return null;
        }
        String key = summary.getKey();
        long size = summary.getSize();
        long mtime = summary.getLastModified().getTime();
        String owner = summary.getOwner().getDisplayName();
        String group = summary.getBucketName();

        return new FileView(key, size, false, mtime, mtime, DEFAULT_PERMISSIONS, owner, group, null);
    }

    private List<String> getKeys(List<OSSObjectSummary> summaries) {
        return Lists.transform(summaries, new Function<OSSObjectSummary, String>() {
            @Override
            public String apply(OSSObjectSummary ossObjectSummary) {
                return ossObjectSummary.getKey();
            }
        });
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }
}
