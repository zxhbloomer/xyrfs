package com.xyrfs.filemanager.impl.local;

import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于本地文件系统的文件系统实现
 *
 * @author vacoor
 */
public class LocalFileSystem implements FileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystem.class);
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    private final String chrootDirectory;

    /**
     * 创建一个不限制根目录的本地文件系统
     */
    public LocalFileSystem() {
        this(null);
    }

    /**
     * 创建一个限制根目录的本地文件系统
     *
     * @param chrootDirectory 虚拟根目录
     */
    public LocalFileSystem(String chrootDirectory) {
        if (WINDOWS && "/".equals(chrootDirectory)) {
            chrootDirectory = null;
        }
        if (null != chrootDirectory) {
            File chroot = new File(chrootDirectory);
            if (!chroot.isAbsolute()) {
                throw new IllegalArgumentException("chrootDirectory must be is a absolute path");
            }
            if (!chroot.exists() && !chroot.mkdirs()) {
                throw new IllegalArgumentException("chrootDirectory is not exists");
            }
            if (!chroot.isDirectory()) {
                throw new IllegalArgumentException("chrootDirectory must be is a directory");
            }
        }
        this.chrootDirectory = chrootDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String path) throws FileSystemException {
        return null != stat(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileView stat(String path) throws FileSystemException {
        String absPath = resolveNativeAbsolutePath(path);
        File file = new File(absPath);
        if (!file.exists()) {
            return null;
        }
        return newView(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileView[] ls(String path) throws FileSystemException {
        String absPath = resolveNativeAbsolutePath(path);
        FileView stat = stat(absPath);
        /*- windows 磁盘会返回 null
        if (null == stat) {
            return new FileView[0];
        }
        */
        if (null != stat && !stat.isDirectory()) {
            return new FileView[]{stat};
        }
        File[] files = new File(absPath).listFiles();
        files = null != files ? files : new File[0];
        FileView[] stats = new FileView[files.length];
        for (int i = 0; i < files.length; i++) {
            stats[i] = newView(files[i]);
        }
        return stats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(String file) throws FileSystemException {
        try {
            String absPath = resolveNativeAbsolutePath(file);
            FileView view = stat(absPath);
            if (null == view) {
                return null;
            }
            if (view.isDirectory()) {
                throw new FileSystemException("Path " + file + " is a directory.");
            }

            return new FileInputStream(absPath);
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdirs(String path) throws FileSystemException {
        String absPath = resolveNativeAbsolutePath(path);
        return mkdirs(new File(absPath));
    }

    /**
     * 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @throws FileSystemException
     */
    protected boolean mkdirs(File f) throws FileSystemException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mkdirs {}", f);
        }
        if (f.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("path is exists: {}", f);
            }

            if (!f.isDirectory()) {
                throw new FileSystemException("File already exists and is not directory: " + f);
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("file#mkdirs: {}", f);
        }
        return f.mkdirs();
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
    public OutputStream create(String file, String contentType, boolean override) throws FileSystemException {
        try {
            String absPath = resolveNativeAbsolutePath(file);

            if (LOG.isDebugEnabled()) {
                LOG.debug("resolve native absolute path: {} -> {}", file, absPath);
            }

            File f = new File(absPath);
            if (!override && f.exists()) {
                throw new FileSystemException("File already exists: " + file);
            }

            // 如果目录不存在且创建失败
            if (!f.getParentFile().exists() && !mkdirs(f.getParentFile())) {
                throw new FileSystemException("create: mkdirs failed to create: " + f.getParent());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("path exists: {}", f.getParentFile());
                LOG.debug("create output stream on {}", f);
            }

            return new FileOutputStream(f);
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    @Override
    public void rm(String path, boolean force, boolean recursive) throws FileSystemException {
        String absPath = resolveNativeAbsolutePath(path);
        File pFile = new File(absPath);
        if (!pFile.exists()) {
            return;
        }
        if (pFile.isDirectory()) {
            File[] files = pFile.listFiles();
            files = null != files ? files : new File[0];
            if (!force && 0 < files.length) {
                throw new FileSystemException("You cannot delete non-empty directory, use force=true to overide");
            }

            for (File f : files) {
                String name = f.getName();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                if (!recursive && f.isDirectory()) {
                    throw new FileSystemException("Directory has contents, cannot delete without recurse=true");
                }
                rm(path + "/" + name, force, recursive);
            }
        }
        if (!pFile.delete()) {
            throw new FileSystemException("cannot delete file: " + pFile);
        }
    }

    @Override
    public void rename(String oldPath, String newPath) throws FileSystemException {
        String oldAbsPath = resolveNativeAbsolutePath(oldPath);
        String newAbsPath = resolveNativeAbsolutePath(newPath);
        /*-
         * windows 下只要有打开这个文件夹就会重命名失败
         */
        if (!new File(oldAbsPath).renameTo(new File(newAbsPath))) {
            throw new FileSystemException("rename failed: " + oldAbsPath + " -> " + newAbsPath);
        }
    }

    protected String resolveNativeAbsolutePath(String path) {
        String normalized = Path.normalize(path);
        if (normalized.startsWith("../")) {
            throw new IllegalArgumentException("Illegal path argument: " + path);
        }
        Path p = Path.get(path);
        if (null == chrootDirectory) {
            return p.isAbsolute() ? p.getPath() : Path.get("/", p).getPath();
        }
        Path chroot = Path.get(chrootDirectory);
        return p.startsWith(chroot) ? p.getPath() : chroot.resolve(p).getPath();
    }

    protected String toRelativePath(String nativeAbsPath) {
        if (null == chrootDirectory) {
            return nativeAbsPath;
        }
        Path chroot = Path.get(chrootDirectory);
        Path path = Path.get(nativeAbsPath);
        String related = path.relativize(chroot).getPath();
        return ("./".equals(related) || ".".equals(related)) ? "/" : related;
    }

    /* *********************************************
     *                Helper Method
     * *********************************************/

    private static final boolean NIO_PRESENT = isPresent("java.nio.file.Path");

    FileView newView(final File file) {
        if (!file.exists()) {
            return null;
        }

        FileView view = null;
        if (NIO_PRESENT) {
            view = !WINDOWS ? newUnixView(file) : newDosView(file);
        }
        if (null == view) {
            String path = toRelativePath(file.getAbsolutePath());
            long length = file.length();
            boolean isDir = file.isDirectory();
            long atime = 0;
            long mtime = 0;
            int mode = 0;
            mode |= file.canRead() ? FileView.OWNER_READ : 0;
            mode |= file.canWrite() ? FileView.OWNER_WRITE : 0;
            mode |= file.canExecute() ? FileView.OWNER_EXECUTE : 0;

            return new FileView(path, length, isDir, mtime, atime, mode, null, null, null);
        }
        return view;
    }

    private FileView newUnixView(File file) {
        FileView view = null;
        if (file.exists() && !WINDOWS && NIO_PRESENT) {
            try {
                final java.nio.file.Path path = file.toPath();
                Map<String, ?> attrsMap = Files.readAttributes(path, "unix:*");

                boolean isDirectory = Files.isDirectory(path);
                long atime = ((FileTime) attrsMap.get("lastAccessTime")).toMillis();
                long mtime = ((FileTime) attrsMap.get("lastModifiedTime")).toMillis();
                String owner = ((UserPrincipal) attrsMap.get("owner")).getName();
                String group = ((GroupPrincipal) attrsMap.get("group")).getName();
                int mode = (Integer) attrsMap.get("mode");
                String symlink = null;

                if (Files.isSymbolicLink(path)) {
                    try {
                        symlink = Files.readSymbolicLink(path).toAbsolutePath().toString();
                    } catch (IOException e) {
                        // ignore
                    }
                }

                view = new FileView(toRelativePath(file.getAbsolutePath()), file.length(), isDirectory, mtime, atime, mode, owner, group, symlink);
            } catch (IOException ex) {
                view = null;
            }
        }
        return view;
    }

    @SuppressWarnings("unchecked")
    private FileView newDosView(File f) {
        FileView view = null;
        if (f.exists() && WINDOWS && NIO_PRESENT) {
            try {
                final java.nio.file.Path path = f.toPath();
                Map<String, ?> attrsMap = Files.readAttributes(path, "dos:*");

                boolean isDirectory = Files.isDirectory(path);
                long atime = ((FileTime) attrsMap.get("lastAccessTime")).toMillis();
                long mtime = ((FileTime) attrsMap.get("lastModifiedTime")).toMillis();

                attrsMap = Files.readAttributes(path, "acl:*");

                String owner = ((UserPrincipal) attrsMap.get("owner")).getName();
                String group = null;
                int mode = 0;
                String symlink = null;

                mode |= f.canRead() ? FileView.OWNER_READ : 0;
                mode |= f.canWrite() ? FileView.OWNER_WRITE : 0;
                mode |= f.canExecute() ? FileView.OWNER_EXECUTE : 0;

                List<AclEntry> acl = (List<AclEntry>) attrsMap.get("acl");
                if (null != acl) {
                    Set<String> groupNames = new HashSet<String>();
                    for (AclEntry entry : acl) {
                        AclEntryType type = entry.type();
                        if (!AclEntryType.ALLOW.equals(type)) {
                            continue;
                        }

                        int entryMode = 0;
                            /*
                            Set<AclEntryPermission> permissions = entry.permissions();
                            if (permissions.contains(AclEntryPermission.READ_DATA)) {
                                entryMode |= FileView.GROUP_READ;
                            }
                            if (permissions.contains(AclEntryPermission.WRITE_DATA)) {
                                entryMode |= FileView.GROUP_WRITE;
                            }
                            if (permissions.contains(AclEntryPermission.EXECUTE)) {
                                entryMode |= FileView.GROUP_EXECUTE;
                            }
                            if (0 < entryMode) {
                                mode &= entryMode;
                            }
                            */
                        groupNames.add(entry.principal().getName());
                    }
                    group = groupNames.toString();
                }
                if (Files.isSymbolicLink(path)) {
                    try {
                        symlink = Files.readSymbolicLink(path).toAbsolutePath().toString();
                    } catch (IOException e) {
                        // ignore
                    }
                }

                view = new FileView(toRelativePath(f.getAbsolutePath()), f.length(), isDirectory, mtime, atime, mode, owner, group, symlink);
            } catch (IOException ex) {
                view = null;
            }
        }
        return view;
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
