package com.xyrfs.filemanager.impl.ftp;

import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;
import com.xyrfs.filemanager.util.MonitorInputStream;
import com.xyrfs.filemanager.util.Path;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;

import static com.xyrfs.filemanager.base.exception.FileSystemException.rethrowFileSystemException;

/**
 * 基于 FTP 的文件系统实现
 *
 * @author vacoor
 */
public class FtpFileSystem implements FileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(FtpFileSystem.class);

    private static final String CURRENT_DIR = ".";                      // 当前目录
    private static final String PARENT_DIR = "..";                      // 上级目录

    private static final String DEFAULT_CONTROL_ENCODING = "utf-8";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    public static final int DEFAULT_PORT = FTP.DEFAULT_PORT;

    private String host;        // FTP host
    private int port;           // FTP port
    private String username;    // FTP username
    private String password;    // FTP password
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
            final FTPClient ftp = connect();
            try {
                return stat(ftp, path);
            } finally {
                disconnect(ftp);
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
            final FTPClient ftp = connect();
            try {
                return ls(ftp, path);
            } finally {
                disconnect(ftp);
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
            final FTPClient ftp = connect();
            try {
                return mkdirs(ftp, path);
            } finally {
                disconnect(ftp);
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
        return open(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 打开给定路径的文件, 如果给定路径不存在返回null, 如果不是文件将抛出异常
     * <p/>
     * 注: 关闭输入流时才会关闭相关资源, 因此读取完毕后务必关闭输入流
     *
     * @param file       文件路径
     * @param bufferSize 缓冲区大小
     * @return 文件输入流或null
     * @throws FileSystemException
     */
    public InputStream open(String file, int bufferSize) throws FileSystemException {
        try {
            final FTPClient ftp = connect();
            String absolutePath = resolveRemoteAbsolutePath(ftp, file);

            try {
                FileView stat = stat(ftp, absolutePath);
                if (null == stat) {
                    disconnect(ftp);
                    return null;
                }
                if (stat.isDirectory()) {
                    disconnect(ftp);
                    throw new FileSystemException("Path " + file + " is a directory.");
                }

                ftp.allocate(bufferSize);

                Path absPath = Path.get(absolutePath);
                String pathName = absPath.getName();
                String absoluteParent = absPath.getParent().getPath();
                // Change to parent directory on the
                // server. Only then can we read the
                // file
                // on the server by opening up an InputStream. As a side effect the working
                // directory on the server is changed to the parent directory of the file.
                // The FTP client connection is closed when close() is called on the
                // FSDataInputStream.
                ftp.changeWorkingDirectory(absoluteParent);
                InputStream is = ftp.retrieveFileStream(pathName);
                InputStream fis = new MonitorInputStream(is) {

                    @Override
                    protected void onClosed() throws IOException {
                        if (!ftp.isConnected()) {
                            throw new IOException("FTPClient not connected");
                        }
                        boolean completed = ftp.completePendingCommand();
                        disconnect(ftp);
                        if (!completed) {
                            throw new IOException("Could not complete transfer, Reply Code - " + ftp.getReplyCode());
                        }
                    }
                };

                if (!FTPReply.isPositivePreliminary(ftp.getReplyCode())) {
                    // The ftpClient is an inconsistent state. Must close the stream
                    // which in turn will logout and disconnect from FTP server
                    fis.close();
                    throw new FileSystemException("Unable to open file: " + file + ", Aborting");
                }
                return fis;
            } catch (IOException ex) {
                disconnect(ftp);
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
    public OutputStream create(String file, boolean override) throws FileSystemException {
        return create(file, null, override);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream create(String file, String contentType, boolean override) throws FileSystemException {
        return create(file, override, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 获取一个创建文件的输出流并自动创建相关目录, 当 override = false, 如果文件已经存在则抛出异常
     *
     * @param file       文件路径
     * @param override   是否覆盖
     * @param bufferSize 缓冲区大小
     * @throws FileSystemException
     */
    public OutputStream create(String file, boolean override, int bufferSize) throws FileSystemException {
        try {
            final FTPClient ftp = connect();
            String absolutePath = resolveRemoteAbsolutePath(ftp, file);

            if (null != stat(ftp, absolutePath)) {
                if (!override) {
                    disconnect(ftp);
                    throw new FileSystemException("File already exists: " + file);
                }
                try {
                    rm(ftp, absolutePath, true, true);
                } catch (IOException ex) {
                    disconnect(ftp);
                    throw new FileSystemException("Can't override file: " + file);
                }
            }

            Path absPath = Path.get(absolutePath);
            String pathName = absPath.getName();
            String absoluteParent = absPath.getParent().getPath();
            if (null == absoluteParent || !mkdirs(ftp, absoluteParent)) {
                absoluteParent = (absoluteParent == null) ? "/" : absolutePath;
                disconnect(ftp);
                throw new FileSystemException("create(): Mkdirs failed to create: " + absoluteParent);
            }

            ftp.allocate(bufferSize);
            // Change to parent directory on the server. Only then can we write to the
            // file on the server by opening up an OutputStream. As a side effect the
            // working directory on the server is changed to the parent directory of the
            // file. The FTP client connection is closed when close() is called on the
            // FSDataOutputStream.
            ftp.changeWorkingDirectory(absoluteParent);
            OutputStream fos = new DataOutputStream(ftp.storeFileStream(pathName)) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        if (!ftp.isConnected()) {
                            throw new SocketException("Client not connected");
                        }
                        boolean completed = ftp.completePendingCommand();
                        disconnect(ftp);
                        if (!completed) {
                            throw new SocketException("Could not complete transfer, Reply Code - " + ftp.getReplyCode());
                        }
                    }
                }
            };
            if (!FTPReply.isPositivePreliminary(ftp.getReplyCode())) {
                // The ftpClient is an inconsistent state. Must close the stream
                // which in turn will logout and disconnect from FTP server
                fos.close();
                throw new FileSystemException("Unable to create file: " + file + ", Aborting");
            }
            return fos;
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
            FTPClient ftp = connect();
            try {
                rm(ftp, path, force, recursive);
            } finally {
                disconnect(ftp);
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
            final FTPClient ftp = connect();
            try {
                if (!rename(ftp, oldPath, newPath)) {
                    throw new FileSystemException("rename failed: " + oldPath + " --> " + newPath);
                }
            } finally {
                disconnect(ftp);
            }
        } catch (Throwable ex) {
            rethrowFileSystemException(ex);
        }
    }


    /* ******************************************************
     *                     FTP  Method
     * *******************************************************/

    /**
     * 使用配置参数连接到 FTP 服务器
     *
     * @return FTPClient 新实例
     * @throws IOException
     */
    protected FTPClient connect() throws IOException {
        String host = this.host;
        int port = 0 < this.port ? this.port : DEFAULT_PORT;
        String username = this.username;
        String password = this.password;

        FTPClient client = new FTPClient();
        client.connect(host, port);
        client.enterLocalPassiveMode();

        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new IOException("Server - " + host + " refused connection on port - " + port);
        } else if (client.login(username, password)) {
            client.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setControlEncoding(DEFAULT_CONTROL_ENCODING);
            client.setBufferSize(DEFAULT_BUFFER_SIZE);
        } else {
            throw new IOException("Login failed on server - " + host + ", port - " + port);
        }
        return client;
    }

    /**
     * 登录并断开给定的 FTPClient
     *
     * @param ftp FTP 客户端
     * @throws IOException
     */
    protected void disconnect(FTPClient ftp) throws IOException {
        if (null != ftp) {
            if (!ftp.isConnected()) {
                return;
            }
            boolean logoutSuccess = ftp.logout();
            ftp.disconnect();
            if (!logoutSuccess) {
                LOG.warn("Logout failed while disconnecting, error code - " + ftp.getReplyCode());
            }
        }
    }

    /**
     * 获取给定路径状态, 如果不存在返回 null
     *
     * @param ftp  FTP 客户端
     * @param path 路径
     * @return 路径状态或 null
     * @throws IOException
     */
    protected FileView stat(FTPClient ftp, String path) throws IOException {
        String absolutePath = resolveRemoteAbsolutePath(ftp, path);

        // path is root
        if (null == absolutePath || "/".equals(absolutePath)) {
            return new FileView("/", -1, true, 0, 0, 0, null, null, null);
        }

        final Path absPath = Path.get(absolutePath);
        final String absParent = absPath.getParent().getPath();
        final String name = absPath.getName();

        FileView stat = null;
        FTPFile[] ftpFiles = ftp.listFiles(absParent);
        if (null != ftpFiles) {
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().equals(name)) {
                    stat = toView(ftp, absParent, ftpFile);
                    break;
                }
            }
        }
        return stat;
    }

    /**
     * 获取路径的文件状态
     *
     * @param ftp  FTP 客户端
     * @param path 文件或目录路径
     * @return 文件或目录的状态, 如果目录下没有文件或文件不存在返回空数组
     * @throws FileSystemException
     */
    protected FileView[] ls(FTPClient ftp, String path) throws IOException {
        String absolutePath = resolveRemoteAbsolutePath(ftp, path);
        FileView stat = stat(ftp, absolutePath);

        if (null == stat) {     // not exists
            return new FileView[0];
        }
        if (!stat.isDirectory()) {    // is file
            return new FileView[]{stat};
        }
        FTPFile[] ftpFiles = ftp.listFiles(absolutePath);
        FileView[] stats = new FileView[ftpFiles.length];
        for (int i = 0; i < ftpFiles.length; i++) {
            stats[i] = toView(ftp, absolutePath, ftpFiles[i]);
        }
        return stats;
    }

    /**
     * 创建给定路径的目录, 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @param path 目录路径
     * @throws IOException
     */
    private boolean mkdirs(FTPClient ftp, String path) throws IOException {
        final Path absPath = Path.get(resolveRemoteAbsolutePath(ftp, path));

        boolean created = true;
        String pathName = absPath.getName();
        FileView stat = stat(ftp, absPath.getPath());

        if (null == stat) {     // not exists
            String absParent = absPath.getParent().getPath();
            created = (absParent == null || mkdirs(ftp, absParent));
            if (created) {
                if (!ftp.changeWorkingDirectory(absParent)) {
                    throw new IOException("Can't change working directory to " + absParent);
                }
                created = created && ftp.makeDirectory(pathName);
            }
        } else if (!stat.isDirectory()) {
            throw new IOException(String.format("Can't make directory for path %s since it is a file.", absPath));
        }
        return created;
    }

    /**
     * 删除给定路径
     *
     * @param ftp       FTP 客户端
     * @param path      文件或目录路径
     * @param force     如果文件夹中存在文件是否强制删除
     * @param recursive 如果文件夹中存在目录是否递归删除
     * @throws IOException
     */
    protected void rm(FTPClient ftp, String path, boolean force, boolean recursive) throws IOException {
        String absolutePath = resolveRemoteAbsolutePath(ftp, path);
        FileView stat = stat(ftp, absolutePath);

        if (null == stat) {     // not exists
            return;
        }

        if (stat.isDirectory()) {
            FTPFile[] ftpFiles = ftp.listFiles(absolutePath);
            if (!force && 0 < ftpFiles.length) {
                throw new IOException("You cannot delete non-empty directory, use force=true to overide");
            }
            for (FTPFile ftpFile : ftpFiles) {
                String name = ftpFile.getName();
                if (PARENT_DIR.equals(name) || CURRENT_DIR.equals(name)) {
                    continue;
                }
                if (!recursive && ftpFile.isDirectory()) {
                    throw new IOException("Directory has contents, cannot delete without recurse=true");
                }
                String childDir = absolutePath.endsWith("/") ? absolutePath : absolutePath + "/";
                rm(ftp, childDir + ftpFile.getName(), force, recursive);
            }
            if (!ftp.removeDirectory(absolutePath)) {
                throw new IOException("cannot delete directory: " + absolutePath);
            }
        } else { // is file
            if (!ftp.deleteFile(absolutePath)) {
                throw new IOException("cannot delete file: " + absolutePath);
            }
        }
    }

    /**
     * 重命名文件
     *
     * @param ftp FTP 客户端
     * @param src 源路径
     * @param dst 目标路径
     * @return 是否重命名成功
     * @throws IOException
     */
    protected boolean rename(FTPClient ftp, String src, String dst) throws IOException {
        final Path absSrc = Path.get(resolveRemoteAbsolutePath(ftp, src));
        final Path absDst = Path.get(resolveRemoteAbsolutePath(ftp, dst));

        if (null == stat(ftp, absSrc.getPath())) {
            throw new FileNotFoundException("Source path " + src + " does not exist");
        }
        if (null != stat(ftp, absDst.getPath())) {
            throw new IOException("Destination path " + dst + " already exist, cannot rename!");
        }

        String parentSrc = absSrc.getParent().getPath();
        String parentDst = absDst.getParent().getPath();
        String from = absSrc.getName();
        String to = absDst.getName();

        if (parentSrc == parentDst || (null != parentSrc && !parentSrc.equals(parentDst))) {
            throw new IOException("Cannot rename parent(source): " + parentSrc + ", parent(destination):  " + parentDst);
        }
        ftp.changeWorkingDirectory(parentSrc);
        return ftp.rename(from, to);
    }

    /* *********************************************
     *                Helper Method
     * *********************************************/

    private FileView toView(FTPClient ftp, String absParent, FTPFile ftpFile) {
        long length = ftpFile.getSize();
        boolean isDir = ftpFile.isDirectory();
        long lastModifiedTime = ftpFile.getTimestamp().getTimeInMillis();
        long lastAccessTime = 0;
        int permissions = getPermissions(ftpFile);
        String user = ftpFile.getUser();
        String group = ftpFile.getGroup();
        String link = ftpFile.getLink();
        String name = toRelativePath(ftp, Path.get(absParent, ftpFile.getName()).getPath());

        return new FileView(name, length, isDir, lastModifiedTime, lastAccessTime, permissions, user, group, link);
    }

    private int getPermissions(FTPFile ftpFile) {
        int permissions = 0;
        if (ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
            permissions |= FileView.OWNER_READ;
        }
        if (ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) {
            permissions |= FileView.OWNER_WRITE;
        }
        if (ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
            permissions |= FileView.OWNER_EXECUTE;
        }

        if (ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) {
            permissions |= FileView.GROUP_READ;
        }
        if (ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION)) {
            permissions |= FileView.GROUP_WRITE;
        }
        if (ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
            permissions |= FileView.GROUP_EXECUTE;
        }

        if (ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION)) {
            permissions |= FileView.OTHER_READ;
        }
        if (ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION)) {
            permissions |= FileView.OTHER_WRITE;
        }
        if (ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)) {
            permissions |= FileView.OTHER_EXECUTE;
        }
        return permissions;
    }

    /* *********************************************
     *
     * *********************************************/

    protected String resolveRemoteAbsolutePath(FTPClient ftp, String path) throws IOException {
        String normalized = Path.normalize(path);
        if (normalized.startsWith("../")) {
            throw new IllegalArgumentException("Illegal path argument: " + path);
        }
        Path p = Path.get(path);
        String chrootDir = getRootPath(ftp);
        if (null == chrootDir) {
            return p.isAbsolute() ? p.getPath() : Path.get("/", p).getPath();
        }
        Path chroot = Path.get(chrootDir);
        return p.startsWith(chroot) ? p.getPath() : chroot.resolve(p).getPath();
    }

    protected String toRelativePath(FTPClient ftp, String path) {
        String rootPath = getRootPath(ftp);
        if (null == rootPath) {
            return path;
        }
        String related = Path.get(path).relativize(rootPath);
        return ("./".equals(related) || ".".equals(related)) ? "/" : related;
    }

    protected String getRootPath(FTPClient ftp) {
        try {
            String root = getChroot();
            return null != root ? root : ftp.printWorkingDirectory();
        } catch (IOException ex) {
            return null;
        }
    }


    /* *********************************************
     *              Getter / Setter
     * *********************************************/

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
