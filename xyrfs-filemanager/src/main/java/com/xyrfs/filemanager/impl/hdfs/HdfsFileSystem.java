package com.xyrfs.filemanager.impl.hdfs;

import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.permission.FsPermission;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class HdfsFileSystem implements FileSystem {
    private final URI hdfsUri;

    public HdfsFileSystem(final URI hdfsUri) {
        this.hdfsUri = hdfsUri;
    }

    @Override
    public boolean exists(final String path) throws FileSystemException {
        try {
            return getHadoopFileSystem().exists(new Path(path));
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public FileView stat(final String path) throws FileSystemException {
        try {
            return newView(getHadoopFileSystem().getFileStatus(new Path(path)));
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public FileView[] ls(final String path) throws FileSystemException {
        try {
            final List<FileView> views = new ArrayList<FileView>();
            final RemoteIterator<LocatedFileStatus> it = getHadoopFileSystem().listFiles(new Path(path), false);
            while (it.hasNext()) {
                final FileView view = newView(it.next());
                if (null != view) {
                    views.add(view);
                }
            }
            return views.toArray(new FileView[views.size()]);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public boolean mkdirs(final String path) throws FileSystemException {
        try {
            return getHadoopFileSystem().mkdirs(new Path(path));
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public InputStream open(final String file) throws FileSystemException {
        try {
            return getHadoopFileSystem().open(new Path(file));
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public OutputStream create(final String file, final boolean override) throws FileSystemException {
        try {
            return getHadoopFileSystem().create(new Path(file), override);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public OutputStream create(final String file, final String contentType, final boolean override) throws FileSystemException {
        try {
            return getHadoopFileSystem().create(new Path(file), override);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public void rm(final String path, final boolean force, final boolean recursive) throws FileSystemException {
        try {
            getHadoopFileSystem().delete(new Path(path), recursive);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public void rename(final String oldPath, final String newPath) throws FileSystemException {
        try {
            if (!getHadoopFileSystem().rename(new Path(oldPath), new Path(newPath))) {
                throw new FileSystemException("rename failed: " + oldPath + " -> " + newPath);
            }
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }

    public void close() {
        try {
            getHadoopFileSystem().close();
        } catch (final IOException e) {
            // ignore
        }
    }

    private FileView newView(final FileStatus st) {
        if (null == st) {
            return null;
        }

        final String path = st.getPath().toString();
        final long length = st.getLen();
        final boolean isDirectory = st.isDirectory();
        final long mtime = st.getModificationTime();
        final long atime = st.getAccessTime();
        final String owner = st.getOwner();
        final String group = st.getGroup();
        final FsPermission perm = st.getPermission();

        String link = null;
        try {
            final Path symlink = st.getSymlink();
            link = null != symlink ? symlink.toString() : null;
        } catch (final IOException e) {
            // ignore
        }

        return new FileView(path, length, isDirectory, mtime, atime, perm.toOctal(), owner, group, link);
    }

    private org.apache.hadoop.fs.FileSystem getHadoopFileSystem() {
        final Configuration conf = new Configuration();
        try {
            return org.apache.hadoop.fs.FileSystem.get(hdfsUri, conf);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }
}
