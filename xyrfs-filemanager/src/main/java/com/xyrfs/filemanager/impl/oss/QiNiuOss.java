package com.xyrfs.filemanager.impl.oss;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.base.interfaces.OssFileSystem;

import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class QiNiuOss implements OssFileSystem {
    private static final int DEFAULT_PERMISSIONS = 0600;

    @Override
    public boolean exists(String path) throws FileSystemException {
        return false;
    }

    @Override
    public FileView stat(String path) throws FileSystemException {
        try {
            FileInfo info = getBucketManager().stat(getBucketName(), path);

            String key = null != info.key ? info.key : path;
            long length = info.fsize;
            long mtime = info.putTime;
            String objectType = info.mimeType;
            String owner = info.endUser;
            String group = owner;

            return new FileView(key, length, false, mtime, mtime, DEFAULT_PERMISSIONS, owner, group, null);
        } catch (QiniuException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public FileView[] ls(String path) throws FileSystemException {
        // 七牛没有文件夹的概念
        // getBucketManager().listFiles()
        FileView view = stat(path);
        return null != view ? new FileView[]{view} : new FileView[0];
    }

    @Override
    public boolean mkdirs(String path) throws FileSystemException {
        throw new UnsupportedOperationException("QiNiu is KEY-VALUE System, Unsupported directory.");
    }

    @Override
    public InputStream open(String file) throws FileSystemException {
        // getBucketManager().fetch()
        return null;
    }

    @Override
    public OutputStream create(String file, boolean override) throws FileSystemException {
//        getAuth().uploadToken()
        // getUploadManager().put()
        return null;
    }

    @Override
    public OutputStream create(String file, String contentType, boolean override) throws FileSystemException {
        return null;
    }

    @Override
    public void rm(String path, boolean force, boolean recursive) throws FileSystemException {
        BucketManager bucketManager = getBucketManager();
        // bucketManager.delete();
        // TODO
    }

    @Override
    public void rename(String oldPath, String newPath) throws FileSystemException {
        String bucket = getBucketName();
        String fKey = oldPath;
        String tKey = newPath;
        try {
            getBucketManager().move(bucket, fKey, bucket, tKey);
        } catch (QiniuException e) {
            throw new FileSystemException(e);
        }
    }

    protected String getBucketName() {
        return "qniu";
    }

    protected Auth getAuth() {
        String ak = "1yp6yYZZUYspEY5lL24SFfiCVsq6_i3PQ4vkua4G";
        String sk = "MXDdbuXi8dXjZxAYKPbbGncPmE9EPIzzqIjgwIMS";
        return Auth.create(ak, sk);
    }

    protected BucketManager getBucketManager() {
        Configuration cfg = new Configuration(Region.region0());
        return new BucketManager(getAuth(),cfg);
    }

    protected UploadManager getUploadManager() {
        Configuration cfg = new Configuration(Region.region0());
        return new UploadManager(cfg);
    }

    public static void main(String[] args) {
        QiNiuOss qn = new QiNiuOss();
        FileView view = qn.stat("rabbitmq.txt");
        System.out.println(view);
    }
}
