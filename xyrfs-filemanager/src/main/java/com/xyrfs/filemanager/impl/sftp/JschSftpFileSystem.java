package com.xyrfs.filemanager.impl.sftp;

import com.jcraft.jsch.*;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.util.MonitorInputStream;
import com.xyrfs.filemanager.util.Path;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于 JSCH 的 SFTP 文件系统
 *
 * @author vacoor
 */
public class JschSftpFileSystem implements FileSystem {
    private static final String CURRENT_DIR = ".";
    private static final String PARENT_DIR = "..";

    public static final int DEFAULT_PORT = 22;

    private String host;
    private int port = DEFAULT_PORT;
    private String username;
    private String password;
    private String chroot;

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
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return stat(sftp, absPath);
            } finally {
                disconnect(sftp);
            }
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileView[] ls(String path) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return ls(sftp, absPath);
            } finally {
                disconnect(sftp);
            }
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdirs(String path) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return mkdirs(sftp, absPath);
            } finally {
                disconnect(sftp);
            }
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(String file) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, file);

            try {
                final FileView stat = stat(absPath);

                if (null == stat) {
                    disconnect(sftp);
                    return null;
                }
                if (stat.isDirectory()) {
                    disconnect(sftp);
                    throw new FileSystemException("Path " + file + " is a directory.");
                }

                return new MonitorInputStream(sftp.get(absPath)) {
                    @Override
                    public void onClosed() throws IOException {
                        disconnect(sftp);
                    }
                };
            } catch (Throwable e) {
                disconnect(sftp);
                throw e;
            }
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
    public OutputStream create(String file, String contentType, boolean override) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, file);
            final int offset = 0;

            try {
                try {
                    sftp.stat(absPath);
                    if (!override) {
                        disconnect(sftp);
                        throw new FileSystemException("File already exists: " + file);
                    }
                    try {
                        rm(sftp, absPath, true, true);
                    } catch (IOException ex) {
                        disconnect(sftp);
                        throw new FileSystemException("Can't override file: " + file);
                    }
                } catch (SftpException e) {
                    int i = absPath.lastIndexOf("/");
                    if (0 >= i) {
                        disconnect(sftp);
                        throw new FileSystemException(e);
                    }
                    String absoluteParent = absPath.substring(0, i);
                    if (!mkdirs(sftp, absoluteParent)) {
                        disconnect(sftp);
                        throw new FileSystemException("create(): Mkdirs failed to create: " + absoluteParent);
                    }
                }

                return (sftp.put(absPath, new SftpProgressMonitorAdapter() {
                    @Override
                    public void end() {
                        disconnect(sftp);
                    }
                }, ChannelSftp.OVERWRITE, offset));
            } catch (Throwable e) {
                disconnect(sftp);
                throw e;
            }
        } catch (Throwable ex) {
            return rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rm(String path, boolean force, boolean recursive) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                rm(sftp, absPath, force, recursive);
            } finally {
                disconnect(sftp);
            }
        } catch (Throwable ex) {
            rethrowFileSystemException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(String oldPath, String newPath) throws FileSystemException {
        try {
            final ChannelSftp sftp = connect();
            final String absSrc = resolveRemoteAbsolutePath(sftp, oldPath);
            final String absDst = resolveRemoteAbsolutePath(sftp, newPath);
            try {
                sftp.rename(absSrc, absDst);
            } finally {
                disconnect(sftp);
            }
        } catch (Throwable ex) {
            rethrowFileSystemException(ex);
        }
    }

    protected String resolveRemoteAbsolutePath(ChannelSftp sftp, String path) throws IOException {
        String normalized = Path.normalize(path);
        if (normalized.startsWith("../")) {
            throw new IllegalArgumentException("Illegal path argument: " + path);
        }
        Path p = Path.get(path);
        String chrootDir = getRootPath(sftp);
        if (null == chrootDir) {
            return p.isAbsolute() ? p.getPath() : Path.get("/", p).getPath();
        }
        Path chroot = Path.get(chrootDir);
        return p.startsWith(chroot) ? p.getPath() : chroot.resolve(p).getPath();
    }

    protected String toRelativePath(ChannelSftp sftp, String path) {
        String rootPath = getRootPath(sftp);
        if (null == rootPath) {
            return path;
        }
        String related = Path.get(path).relativize(rootPath);
        return ("./".equals(related) || ".".equals(related)) ? "/" : related;
    }

    protected String getRootPath(ChannelSftp sftp) {
        try {
            String root = getChroot();
            return null != root ? root : sftp.getHome();
        } catch (Exception e) {
            return null;
        }
    }

    /* *******************************************
     *                SFTP Method
     * *******************************************/

    /**
     * 创建一个新的 SFTP 通道
     *
     * @return SFTP 通道
     * @throws IOException
     */
    protected ChannelSftp connect() throws IOException {
        try {
            JSch jsch = new JSch();
            // jsch.addIdentity(); 私钥
            Session session = jsch.getSession(getUsername(), getHost(), getPort());

            // 第一次登陆时候提示, (ask|yes|no)
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("compression.s2c", "zlib,none");
            session.setConfig("compression.c2s", "zlib,none");
            // session.setTimeout(3 * 1000);
            session.setPassword(getPassword());

            // session.connect(10 * 1000);
            session.connect();

            // ChannelShell shell = (ChannelShell) session.createSftp("shell");
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            return sftp;
        } catch (JSchException e) {
            throw new IOException("Server - " + host + " refused connection on port - " + port);
        }
    }

    /**
     * 断开给定的 SFTP 通道
     *
     * @param sftp SFTP 通道
     */
    protected void disconnect(ChannelSftp sftp) {
        if (null == sftp) {
            return;
        }
        try {
            try {
                if (!sftp.isClosed()) {
                    sftp.disconnect();
                }
            } finally {
                Session session = sftp.getSession();
                if (session.isConnected()) {
                    session.disconnect();
                }
            }
        } catch (JSchException e) {
            // ignore
        }
    }

    /**
     * 获取路径的文件状态
     *
     * @param sftp SFTP 通道
     * @param path 文件系统路径
     * @return 返回文件状态, 如果文件不存在返回 null
     * @throws IOException
     */
    protected FileView stat(ChannelSftp sftp, String path) throws IOException {
        try {
            /*- TODO HERE
            final ChannelSftp.LsEntry[] found = {null};
            if (null != path && !"/".equals(path)) {
                String parent = null;
                sftp.ls(parent, new ChannelSftp.LsEntrySelector() {

                    @Override
                    public int select(ChannelSftp.LsEntry entry) {
                        String name = entry.getFilename();
                        if (name.equals(path)) {
                            found[0] = entry;
                            return BREAK;
                        }
                        return CONTINUE;
                    }
                });
                if (null != found[0]) {
                    String filename = found[0].getFilename();
                    String longname = found[0].getLongname();
                    SftpATTRS attrs = found[0].getAttrs();
                    return toView(sftp, filename, longname, attrs);
                }
            }
            */

            SftpATTRS attrs = sftp.stat(path);
            return toView(sftp, path, null, attrs);
        } catch (SftpException e) {
            return null;
        }
    }

    /**
     * 获取路径的文件状态
     *
     * @param sftp SFTP 通道
     * @param path 文件或目录路径
     * @return 文件或目录的状态, 如果目录下没有文件或文件不存在返回空数组
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected FileView[] ls(final ChannelSftp sftp, String path) throws IOException {
        FileView stat = stat(sftp, path);
        if (null == stat) {
            return new FileView[0];
        }
        if (!stat.isDirectory()) {
            return new FileView[]{stat};
        }
        try {
            final Vector<ChannelSftp.LsEntry> entries = new Vector<ChannelSftp.LsEntry>();
            sftp.ls(path, new ChannelSftp.LsEntrySelector() {
                @Override
                public int select(ChannelSftp.LsEntry entry) {
                    String filename = entry.getFilename();
                    if (!CURRENT_DIR.equals(filename) && !PARENT_DIR.equalsIgnoreCase(filename)) {
                        entries.add(entry);
                    }
                    return CONTINUE;
                }
            });
            // 不能在未读取完前再次读取
            FileView[] views = new FileView[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                ChannelSftp.LsEntry entry = entries.get(i);
                String filename = entry.getFilename();
                String longname = entry.getLongname();
                SftpATTRS attrs = entry.getAttrs();
                views[i] = (toView(sftp, Path.get(path, filename).getPath(), longname, attrs));
            }
            return views;
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    /**
     * 创建给定路径的目录, 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @param sftp SFTP 通道
     * @param path 目录路径
     * @throws FileSystemException
     */
    protected boolean mkdirs(ChannelSftp sftp, String path) throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        for (String pos = path.startsWith("/") ? "/" : ""; tokenizer.hasMoreElements(); pos = pos + "/") {
            pos = pos + tokenizer.nextElement();
            try {
                sftp.stat(pos);
            } catch (SftpException ex1) {
                try {
                    sftp.mkdir(pos);
                } catch (SftpException ex2) {
                    // ignore
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 删除给定路径
     *
     * @param sftp      SFTP 通道
     * @param path      文件或目录路径
     * @param force     如果文件夹中存在文件是否强制删除
     * @param recursive 如果文件夹中存在目录是否递归删除
     * @throws FileSystemException
     */
    @SuppressWarnings("unchecked")
    protected void rm(ChannelSftp sftp, String path, boolean force, boolean recursive) throws IOException {
        try {
            SftpATTRS attrs = sftp.stat(path);
            if (attrs.isDir()) {
                Vector<ChannelSftp.LsEntry> files = sftp.ls(path);

                if (!force && 0 < files.size()) {
                    throw new IOException("You cannot delete non-empty directory, use force=true to overide");
                }

                for (ChannelSftp.LsEntry f : files) {
                    String name = f.getFilename();
                    if (".".equals(name) || "..".equals(name)) {
                        continue;
                    }
                    if (!recursive && f.getAttrs().isDir()) {
                        throw new IOException("Directory has contents, cannot delete without recurse=true");
                    }
                    rm(sftp, path + "/" + name, force, recursive);
                }

                sftp.rmdir(path);
            } else {
                sftp.rm(path);
            }
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    /* *******************************************
     *                Helper Method
     * *******************************************/

    protected FileView toView(ChannelSftp sftp, String absPath, String longname, SftpATTRS attrs) {
        long length = attrs.getSize();
        boolean dir = attrs.isDir();
        long lastModifiedTime = attrs.getMTime() * 1000L;   // 转换为毫秒
        long lastAccessTime = attrs.getATime() * 1000L;
        int permissions = attrs.getPermissions();
        int uid = attrs.getUId();
        int gid = attrs.getGId();
        boolean isLink = attrs.isLink();
        String symlink = null;

        if (isLink) {
            try {
                // sftp.readlink(path);    // 链接内容, 可能是相对路径
                symlink = sftp.realpath(absPath);    // 链接的真实绝对路径
            } catch (SftpException e) {
                symlink = "Unknown";
            }
        }
        String[] info = parseUserAndGroup(longname);
        String owner = 1 < info.length ? info[0] : String.valueOf(uid);
        String group = 1 < info.length ? info[1] : String.valueOf(gid);

        return new FileView(toRelativePath(sftp, absPath), length, dir, lastModifiedTime, lastAccessTime, permissions, owner, group, symlink);
    }

    private String[] parseUserAndGroup(String longname) {
        if (null != longname) {
            String[] ug = new String[2];
            // lrwxr-xr-x    1 root     wheel          11 Oct 11 16:09 etc
            StringTokenizer tokenizer = new StringTokenizer(longname);
            for (int i = 0; i < 4 && tokenizer.hasMoreTokens(); i++) {
                String token = tokenizer.nextToken();
                if (i == 2) {
                    ug[0] = token;
                } else if (i == 3) {
                    ug[1] = token;
                    return ug;
                }
            }
        }
        return new String[0];
    }


    protected static class SftpProgressMonitorAdapter implements SftpProgressMonitor {
        @Override
        public void init(int i, String s, String s1, long l) {
        }

        /**
         * 返回 false 取消处理
         *
         * @param readed 读取的字节数
         */
        @Override
        public boolean count(long readed) {
            return true;
        }

        @Override
        public void end() {
        }
    }

    /* *******************************************
     *             Getter / Setter
     * *******************************************/

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChroot() {
        return chroot;
    }

    public void setChroot(String chroot) {
        this.chroot = chroot;
    }
}
