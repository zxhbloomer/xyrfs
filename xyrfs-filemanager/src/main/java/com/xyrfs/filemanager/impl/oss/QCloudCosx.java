package com.xyrfs.filemanager.impl.oss;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.base.interfaces.OssFileSystem;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于腾讯云 COS 的文件系统实现(API和阿里云一样).
 * <p>
 * 1. 开通 COS <br/>
 * 2. 创建 bucket <br/>
 * 3. 为 bucket 绑定域名, eg: img.ponly.com --&gt; ponly.oss-cn-hongkong.aliyuncs.com <br/>
 *
 * @author vacoor
 */
public class QCloudCosx implements OssFileSystem {
    private static final int DEFAULT_PERMISSIONS = 0600;

    private final String region;
    private final String secretId;
    private final String secretKey;

    /**
     * bucket的命名规则为{name}-{appid}.
     */
    private final String bucketName;

    public QCloudCosx(String region, String secretId, String secretKey, String bucketName) {
        this.region = region;
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String path) throws FileSystemException {
        String absPath = resolveAbsolutePath(path);
        String bucketName = getBucketName(absPath);

        COSClient client = null;
        try {
            client = connect();
            return client.doesObjectExist(bucketName, absPath);
        } finally {
            disconnect(client);
        }
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
            COSObject object = getObject(bucketName, absPath);
            FileView stat = null;

            if (null != object) {
                ObjectMetadata metadata = object.getObjectMetadata();
                String key = object.getKey();
                long length = metadata.getContentLength();
                long mtime = metadata.getLastModified().getTime();
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

        COSClient client = null;
        try {
            client = connect();
            ObjectListing listing = client.listObjects(listRequest);
            List<COSObjectSummary> summaries = listing.getObjectSummaries();
            FileView[] views = new FileView[summaries.size()];
            for (int i = 0; i < summaries.size(); i++) {
                views[i] = toView(summaries.get(i));
            }
            return views;
        } finally {
            disconnect(client);
        }
    }

    /**
     * 不支持该操作
     *
     * @throws FileSystemException
     */
    @Override
    public boolean mkdirs(String path) throws FileSystemException {
        throw new UnsupportedOperationException("Aliyun COS is KEY-VALUE System, Unsupported directory.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream open(String file) throws FileSystemException {
        try {
            String absPath = resolveAbsolutePath(file);
            String bucketName = getBucketName(absPath);
            COSObject object = getObject(bucketName, absPath);
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

            Thread thread = new Thread("QCLOUD-COS-UPLOADER") {
                @Override
                public void run() {
                    ObjectMetadata metadata = new ObjectMetadata();
                    // 默认是 application/octet-stream, 对于图片等, 某些浏览器会直接下载
                    if (null != contentType) {
                        metadata.setContentType(contentType);
                    }

                    COSClient client = null;
                    try {
                        client = connect();
                        client.putObject(bucketName, absPath, in, metadata);
                    } finally {
                        disconnect(client);
                    }
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
        COSClient client = null;
        try {
            client = connect();
            boolean deleted = false;

            DeleteObjectsRequest delRequest = new DeleteObjectsRequest(bucketName);
            DeleteObjectsResult ret;

            // 如果可能不是目录, 尝试直接删除
            if (!absPath.endsWith("/")) {
                //
                delRequest.setKeys(Collections.singletonList(new DeleteObjectsRequest.KeyVersion(absPath)));
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
                    delChildRequest.setKeys(Lists.transform(keys, new Function<String, DeleteObjectsRequest.KeyVersion>() {
                        @Override
                        public DeleteObjectsRequest.KeyVersion apply(final String input) {
                            return new DeleteObjectsRequest.KeyVersion(input);
                        }
                    }));
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
        } finally {
            disconnect(client);
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

    protected COSClient connect() {
        final COSCredentials credential = new BasicCOSCredentials(this.secretId, this.secretKey);
        final ClientConfig clientConfig = new ClientConfig(new Region(this.region));
        return new COSClient(credential, clientConfig);
    }

    protected void disconnect(final COSClient client) {
        if (null != client) {
            client.shutdown();
        }
    }

    private COSObject getObject(String bucketName, String path) throws IOException {
        String absPath = resolveAbsolutePath(path);
        COSObject object;

        COSClient client = null;
        try {
            client = connect();
            object = client.getObject(bucketName, absPath);
        } catch (CosServiceException ex) {
            if ("NoSuchKey".equals(ex.getErrorCode()) || 404 == ex.getStatusCode()) {
                object = null;
            } else {
                throw new IOException(ex);
            }
        } finally {
            disconnect(client);
        }
        return object;
    }

    private FileView toView(COSObjectSummary summary) {
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

    private List<String> getKeys(List<COSObjectSummary> summaries) {
        return Lists.transform(summaries, new Function<COSObjectSummary, String>() {
            @Override
            public String apply(COSObjectSummary ossObjectSummary) {
                return ossObjectSummary.getKey();
            }
        });
    }

    public String getRegion() {
        return region;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }
}
