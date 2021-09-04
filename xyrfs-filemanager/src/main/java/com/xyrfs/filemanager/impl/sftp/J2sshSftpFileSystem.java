package com.xyrfs.filemanager.impl.sftp;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import static com.sshtools.j2ssh.sftp.SftpSubsystemClient.*;
import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于 sshtools:j2ssh-core 实现的 SFTP 文件系统
 * <p/>
 * sshtools:j2ssh-core:0.29 登录 OSX 10.9 ssh 无法认证, Linux 暂时没有发现问题
 * 建议更新使用 com.sshtools:j2ssh-maverick
 *
 * @author vacoor
 */
public class J2sshSftpFileSystem implements FileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(J2sshSftpFileSystem.class);
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

    protected String resolveRemoteAbsolutePath(SftpSubsystemClient sftp, String path) throws IOException {
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

    protected String toRelativePath(SftpSubsystemClient sftp, String path) {
        String rootPath = getRootPath(sftp);
        if (null == rootPath) {
            return path;
        }
        String related = Path.get(path).relativize(rootPath);
        return ("./".equals(related) || ".".equals(related)) ? "/" : related;
    }

    protected String getRootPath(SftpSubsystemClient sftp) {
        try {
            String root = getChroot();
            return null != root ? root : sftp.getDefaultDirectory();
        } catch (Exception e) {
            return null;
        }
    }


    /* **********************************************
     *              J2SSH SFTP Method
     * **********************************************/

    private class SftpChannel extends SftpSubsystemClient {
        private final SshClient ssh;

        SftpChannel(SshClient ssh) {
            this.ssh = ssh;
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
            // SftpSubsystemClient sftp = ssh.openSftpChannel();
            // return new SftpChannel(ssh, sftp);

            SftpChannel sftpChannel = new SftpChannel(ssh);

            if (!ssh.openChannel(sftpChannel)) {
                throw new SshException("The SFTP subsystem failed to start");
            } else if (!sftpChannel.initialize()) {
                throw new SshException("The SFTP Subsystem could not be initialized");
            }

            return sftpChannel;
        } catch (IOException ioe) {
            disconnect(ssh);
            throw ioe;
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
                closeChannel(sftp);
            } finally {
                if (disconnectClient) {
                    disconnect(sftp.ssh);
                }
            }
        }
    }

    /**
     * 获取 SSH 客户端
     *
     * @throws IOException
     */
    private SshClient connect() throws IOException {
        SshClient ssh = new SshClient();
        ssh.connect(getHost(), getPort(), new YesHostKeyVerificationImpl());

        // PublicKeyAuthenticationClient pkc = new PublicKeyAuthenticationClient();
        // pkc.setKey(new SshRsaPrivateKey());

        PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
        pac.setUsername(getUsername());
        pac.setPassword(getPassword());

        int code = ssh.authenticate(pac);
        if (AuthenticationProtocolState.COMPLETE != code) {
            LOG.debug("authentication failed");
            throw new IOException("authentication failed, state:" + code);
        }

        LOG.debug("connect...");
        return ssh;
    }

    protected void closeChannel(Channel channel) throws IOException {
        if (null != channel && channel.isClosed()) {
            channel.close();
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
     * @param path 文件系统路径
     * @return 返回文件状态, 如果文件不存在返回 null
     * @throws IOException
     */
    protected FileView stat(SftpSubsystemClient sftp, String path) throws IOException {
        try {
            FileAttributes attrs = sftp.getAttributes(path);
            return toView(sftp, path, null, attrs);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取路径的文件状态
     *
     * @param path 文件或目录路径
     * @return 文件或目录的状态, 如果目录下没有文件或文件不存在返回空数组
     * @throws IOException
     */
    protected FileView[] ls(SftpSubsystemClient sftp, String path) throws IOException {
        FileView stat = stat(sftp, path);
        if (null == stat) {
            return new FileView[0];
        }
        if (!stat.isDirectory()) {
            return new FileView[]{stat};
        }
        SftpFile dir = sftp.openDirectory(path);
        Vector<SftpFile> files = new Vector<SftpFile>();
        sftp.listChildren(dir, files);

        List<FileView> views = new ArrayList<FileView>(files.size());
        for (SftpFile f : files) {
            String filename = f.getFilename();
            if (CURRENT_DIR.equals(filename) || PARENT_DIR.equals(filename)) {
                continue;
            }
            views.add(toView(sftp, f.getAbsolutePath(), f.getLongname(), f.getAttributes()));
        }
        return views.toArray(new FileView[views.size()]);
    }

    /**
     * 创建给定路径的目录, 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @param sftp STP 通道
     * @param path 目录路径
     * @throws IOException
     */
    protected boolean mkdirs(SftpSubsystemClient sftp, String path) throws IOException {
        if (path.length() > 0) {
            SftpFile dir;
            try {
                dir = sftp.openDirectory(path);
                dir.close();
            } catch (IOException notExistsEx) {
                StringTokenizer tokenizer = new StringTokenizer(path, "/", true);
                String p = "";

                while (tokenizer.hasMoreElements()) {
                    p = p + tokenizer.nextElement();

                    try {
                        dir = sftp.openDirectory(p);
                        dir.close();
                    } catch (IOException ex) {
                        // log.info("Creating " + var5);
                        sftp.makeDirectory(p);
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
    @SuppressWarnings("unchecked")
    protected void rm(SftpSubsystemClient sftp, String path, boolean force, boolean recursive) throws IOException {
        FileAttributes file = sftp.getAttributes(path);
        if (file.isDirectory()) {
            List<SftpFile> files = new ArrayList<SftpFile>();
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
    }

    /* ********************************************************
     *                  Helper Method
     * *******************************************************/

    private static class YesHostKeyVerificationImpl implements HostKeyVerification {
        @Override
        public boolean verifyHost(String s, SshPublicKey sshPublicKey) throws TransportProtocolException {
            return true;
        }
    }

    protected FileView toView(SftpSubsystemClient sftp, String absPath, String longname, FileAttributes attrs) throws IOException {
        long length = attrs.getSize().longValue();
        boolean dir = attrs.isDirectory();
        long lastModifiedTime = attrs.getModifiedTime().longValue() * 1000; // 毫秒
        long lastAccessTime = attrs.getAccessedTime().longValue() * 1000;
        int permissions = attrs.getPermissions().intValue();
        int uid = attrs.getUID().intValue();
        int gid = attrs.getGID().intValue();
        boolean isLink = attrs.isLink();
        String symlink = null;

        if (isLink) {
            try {
                symlink = sftp.getSymbolicLinkTarget(absPath);
            } catch (Exception ex) {
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
