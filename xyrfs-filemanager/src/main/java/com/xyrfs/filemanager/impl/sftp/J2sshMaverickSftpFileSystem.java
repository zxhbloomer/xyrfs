package com.xyrfs.filemanager.impl.sftp;

import com.sshtools.net.SocketTransport;
import com.sshtools.sftp.*;
import com.sshtools.ssh.*;
import com.sshtools.ssh2.Ssh2Client;
import com.sshtools.ssh2.Ssh2Session;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.util.MonitorInputStream;
import com.xyrfs.filemanager.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import static com.sshtools.sftp.SftpSubsystemChannel.*;
import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于 com.sshtools:j2ssh-maverick 实现的 SFTP 文件系统
 * <p/>
 *
 * @author vacoor
 */
public class J2sshMaverickSftpFileSystem implements FileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(J2sshMaverickSftpFileSystem.class);

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
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return stat(sftp, absPath);
            } finally {
                closeChannel(sftp, true);
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
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return ls(sftp, absPath);
            } finally {
                closeChannel(sftp, true);
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
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                return mkdirs(sftp, absPath);
            } finally {
                closeChannel(sftp, true);
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
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, file);
            try {
                final FileView stat = stat(sftp, absPath);

                if (null == stat) {
                    closeChannel(sftp, true);
                    return null;
                }
                if (stat.isDirectory()) {
                    closeChannel(sftp, true);
                    throw new FileSystemException("Path " + file + " is a directory.");
                }

                final SftpFileInputStream is = new SftpFileInputStream(sftp.openFile(absPath, OPEN_READ));
                return new MonitorInputStream(is) {
                    @Override
                    public void onClosed() throws IOException {
                        closeChannel(sftp, true);
                    }
                };
            } catch (Throwable e) {
                closeChannel(sftp, true);
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
    public OutputStream create(final String file, final String contentType, final boolean override) throws FileSystemException {
        try {
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, file);

            try {
                OutputStream out;
                try {
                    sftp.getAttributes(absPath);
                    // exists
                    if (!override) {
                        closeChannel(sftp, true);
                        throw new IOException("File already exists: " + file);
                    }
                    out = new SftpFileOutputStream(sftp.openFile(absPath, OPEN_TRUNCATE | OPEN_CREATE | OPEN_WRITE));
                } catch (IOException notExistsEx) {
                    int i = absPath.lastIndexOf("/");
                    if (0 >= i) {
                        closeChannel(sftp, true);
                        throw notExistsEx;
                    }
                    String absoluteParent = absPath.substring(0, i);
                    if (!mkdirs(sftp, absoluteParent)) {
                        closeChannel(sftp, true);
                        throw new IOException("create(): Mkdirs failed to create: " + absoluteParent);
                    }

                    out = new SftpFileOutputStream(sftp.openFile(absPath, OPEN_CREATE | OPEN_WRITE));
                }

                return new DataOutputStream(out) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            closeChannel(sftp, true);
                        }
                    }

                    @Override
                    protected void finalize() throws Throwable {
                        super.finalize();
                        close();
                    }
                };
            } catch (Throwable ex) {
                closeChannel(sftp, true);
                throw ex;
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
            final SftpChannel sftp = openSftpChannel();
            final String absPath = resolveRemoteAbsolutePath(sftp, path);
            try {
                rm(sftp, absPath, force, recursive);
            } finally {
                closeChannel(sftp, true);
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
            final SftpChannel sftp = openSftpChannel();
            final String absSrc = resolveRemoteAbsolutePath(sftp, oldPath);
            final String absDst = resolveRemoteAbsolutePath(sftp, newPath);

            try {
                sftp.renameFile(absSrc, absDst);
            } finally {
                closeChannel(sftp, true);
            }
        } catch (Throwable ex) {
            rethrowFileSystemException(ex);
        }
    }

    protected String resolveRemoteAbsolutePath(SftpSubsystemChannel sftp, String path) throws IOException {
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

    protected String toRelativePath(SftpSubsystemChannel sftp, String path) {
        String rootPath = getRootPath(sftp);
        if (null == rootPath) {
            return path;
        }
        String related = Path.get(path).relativize(rootPath);
        return ("./".equals(related) || ".".equals(related)) ? "/" : related;
    }

    protected String getRootPath(SftpSubsystemChannel sftp) {
        try {
            String root = getChroot();
            return null != root ? root : sftp.getDefaultDirectory();
        } catch (Exception e) {
            return null;
        }
    }



    /* ******************************************
     *               JSCH Method
     * ******************************************/

    private class SftpChannel extends SftpSubsystemChannel {
        private final SshClient ssh;

        SftpChannel(SshSession session) throws SshException {
            super(session);
            this.ssh = session.getClient();
        }
    }

    /**
     * 打开一个 SFTP 通道
     *
     * @throws IOException
     */
    private SftpChannel openSftpChannel() throws IOException {
        final SshClient ssh = connect();
        try {
            LOG.info("open sftp channel...");

            Ssh2Session session = (Ssh2Session) ssh.openSessionChannel();
            SftpChannel sftp = new SftpChannel(session);

            /**
             * Start the SFTP server
             */
            if (!session.startSubsystem("sftp")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The SFTP subsystem failed to start, attempting to execute provider " + ssh.getContext().getSFTPProvider());
                }
                // We could not start the subsystem try to fallback to the
                // provider specified by the user
                if (!session.executeCommand(ssh.getContext().getSFTPProvider())) {
                    session.close();
                    throw new IOException("Failed to start SFTP subsystem or SFTP provider " + ssh.getContext().getSFTPProvider());
                }
            }
            sftp.initialize();
            return sftp;
        } catch (Throwable e) {
            disconnect(ssh);
            if (e instanceof Error) {
                throw (Error) e;
            }
            throw new IOException("The SFTP Subsystem could not be initialized");
        }
    }

    /**
     * 关闭 SFTP 通道
     *
     * @param sftp             SFTP 通道
     * @param disconnectClient 是否断开 SSH 客户端
     * @throws IOException
     */
    private void closeChannel(SftpChannel sftp, boolean disconnectClient) throws IOException {
        if (null != sftp) {
            try {
                if (!sftp.isClosed()) {
                    sftp.close();
                }
            } finally {
                if (disconnectClient) {
                    if (!sftp.ssh.isConnected()) {
                        disconnect(sftp.ssh);
                    }
                }
            }
        }
    }

    /**
     * 获取 SSH 客户端
     *
     * @throws IOException
     */
    protected SshClient connect() throws IOException {
        try {
            SshConnector connector = SshConnector.createInstance();
            SocketTransport transport = new SocketTransport(getHost(), getPort());
            Ssh2Client ssh = connector.connect(transport, getUsername());

            PasswordAuthentication pac = new PasswordAuthentication();
            pac.setUsername(getUsername());
            pac.setPassword(getPassword());

            int code = ssh.authenticate(pac);
            if (SshAuthentication.COMPLETE != code) {
                LOG.debug("authentication failed");
                throw new IOException("authentication failed, state:" + code);
            }

            LOG.debug("connect...");
            return ssh;
        } catch (SshException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * 断开 SSH 客户端链接
     *
     * @param ssh SSH 客户端
     */
    protected void disconnect(SshClient ssh) {
        if (null != ssh && ssh.isConnected()) {
            ssh.disconnect();
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
    protected FileView stat(SftpSubsystemChannel sftp, String path) throws IOException {
        try {
            SftpFileAttributes attr = sftp.getAttributes(path);
            return toView(sftp, path, null, attr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取路径的文件状态
     *
     * @param sftp STP 通道
     * @param path 文件或目录路径
     * @return 文件或目录的状态, 如果目录下没有文件或文件不存在返回空数组
     * @throws IOException
     */
    protected FileView[] ls(SftpSubsystemChannel sftp, String path) throws IOException {
        FileView stat = stat(sftp, path);
        if (null == stat) {
            return new FileView[0];
        }
        if (!stat.isDirectory()) {
            return new FileView[]{stat};
        }
        try {
            SftpFile dir = sftp.openDirectory(path);
            Vector<SftpFile> files = new Vector<SftpFile>();
            sftp.listChildren(dir, files);

            FileView[] views = new FileView[files.size()];
            for (int i = 0; i < files.size(); i++) {
                SftpFile f = files.get(i);
                views[i] = toView(sftp, f.getAbsolutePath(), f.getLongname(), f.getAttributes());
            }
            return views;
        } catch (Exception ex) {
            return rethrowIOException(ex);
        }
    }

    /**
     * 创建给定路径的目录, 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @param sftp STP 通道
     * @param path 目录路径
     * @throws IOException
     */
    protected boolean mkdirs(SftpSubsystemChannel sftp, String path) throws IOException {
        if (path.length() > 0) {
            SftpFile dir;
            try {
                dir = sftp.openDirectory(path);
                dir.close();
            } catch (Exception notExistsEx) {
                StringTokenizer tokenizer = new StringTokenizer(path, "/", true);
                String p = "";

                while (tokenizer.hasMoreElements()) {
                    p = p + tokenizer.nextElement();

                    try {
                        dir = sftp.openDirectory(p);
                        dir.close();
                    } catch (Exception ex) {
                        // log.info("Creating " + var5);
                        try {
                            sftp.makeDirectory(p);
                        } catch (Exception e) {
                            throw new IOException(e);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除给定路径
     *
     * @param sftp      SFTP 通道
     * @param path      文件或目录路径
     * @param force     如果文件夹中存在文件是否强制删除
     * @param recursive 如果文件夹中存在目录是否递归删除
     * @throws IOException
     */
    protected void rm(SftpSubsystemChannel sftp, String path, boolean force, boolean recursive) throws IOException {
        try {
            SftpFileAttributes file = sftp.getAttributes(path);
            if (file.isDirectory()) {
                Vector<SftpFile> files = new Vector<SftpFile>();
                SftpFile sftpFile = sftp.openDirectory(path);
                sftp.listChildren(sftpFile, files);

                if (!force && 0 < files.size()) {
                    throw new IOException("You cannot delete non-empty directory, use force=true to overide");
                }

                for (SftpFile f : files) {
                    String name = f.getFilename();
                    if (CURRENT_DIR.equals(name) || PARENT_DIR.equals(name)) {
                        continue;
                    }
                    if (!recursive && f.getAttributes().isDirectory()) {
                        throw new IOException("Directory has contents, cannot delete without recurse=true");
                    }
                    rm(sftp, path + "/" + name, force, recursive);
                }

                sftp.removeDirectory(path);
            } else {
                sftp.removeFile(path);
            }
        } catch (Exception e) {
            rethrowIOException(e);
        }
    }

    /* ******************************************
     *               Helper Method
     * ******************************************/

    protected FileView toView(SftpSubsystemChannel sftp, String path, String longname, SftpFileAttributes attrs) throws IOException {
        long length = attrs.getSize().longValue();
        boolean dir = attrs.isDirectory();
        long lastModifiedTime = attrs.getModifiedTime().longValue() * 1000; // 毫秒
        long lastAccessTime = attrs.getAccessedTime().longValue() * 1000;
        int permissions = attrs.getPermissions().intValue();
        String uid = attrs.getUID();
        String gid = attrs.getGID();
        boolean isLink = attrs.isLink();
        String symlink = null;
        if (isLink) {
            try {
                symlink = sftp.getSymbolicLinkTarget(path);
            } catch (Exception ex) {
                symlink = "Unknown";
            }
        }
        String[] info = parseUserAndGroup(longname);
        String owner = 1 < info.length ? info[0] : String.valueOf(uid);
        String group = 1 < info.length ? info[1] : String.valueOf(gid);

        return new FileView(toRelativePath(sftp, path), length, dir, lastModifiedTime, lastAccessTime, permissions, owner, group, symlink);
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

    protected <V> V rethrowIOException(Throwable ex) throws IOException {
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        if (ex instanceof IOException) {
            throw (IOException) (ex);
        }
        throw new IOException(ex);
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
