package com.xyrfs.filemanager;

import com.xyrfs.filemanager.impl.ftp.FtpFileSystem;
import com.xyrfs.filemanager.impl.local.LocalFileSystem;
import com.xyrfs.filemanager.util.IOUtils;
import com.xyrfs.filemanager.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xyrfs.filemanager.base.interfaces.FileSystem;
import com.xyrfs.filemanager.FileView;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件系统工具类
 * <p>
 * 该类提供通过 url 来获取文件系统的方法, 文件系统 url 格式如下:<br/>
 * schema://[user[:password]@]/path <br/>
 * eg:<br/>
 * local:///mnt/project <br/>
 * sftp://user:passwd@[/mnt]
 * aliyun://access_key_id:access_key_secure@bucket.endpoint
 * <br />
 * 注: 如果 URI 中存在特殊字符需要使用 {@link java.net.URLEncoder#encode(String, String)} 进行转移,
 * eg: passwd=foo@bar 则 URI 中应该是 foo%40bar.
 *
 * @author vacoor
 */
public abstract class FileSystems {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystems.class);
    private static final boolean JSCH_PRESENT = isPresent("com.jcraft.jsch.JSch");
    private static final boolean J2SSH_MAVERICK_PRESENT = isPresent("com.sshtools.ssh.SshClient");
    private static final boolean J2SSH_PRESENT = isPresent("com.sshtools.j2ssh.SftpClient");
    private static final boolean HDFS_PRESENT = isPresent("org.apache.hadoop.fs.FileSystem");

    /**
     * 将文件从 from 文件系统拷贝到 to 文件系统
     * eg:<br/>
     * copy(new URI("ftp://user:passwd@/mnt/a.zip"), new URI("file:///c:/a.zip");
     *
     * @param from 源文件系统的 uri
     * @param to   目标文件系统的 uri
     * @throws IOException
     */
    public static void copy(URI from, URI to) throws IOException {
        FileSystem ffs = getFileSystem(from.toString());
        FileSystem tfs = getFileSystem(to.toString());

        String fRealPath = getRealPath(from);
        String tRealPath = getRealPath(to);

        InputStream in = ffs.open(fRealPath);
        OutputStream out = tfs.create(tRealPath, true);
        IOUtils.flow(in, out, true, true);
    }

    /* *****************************************************************
     *                    Compress As Zip
     * *****************************************************************/

    public static void zip(String fsUri, String archive, String... files) throws IOException {
        fsUri = (null != fsUri) ? fsUri : "file:///";
        zip(getFileSystem(fsUri), archive, files);
    }

    public static void zip(String fsUri, OutputStream out, String... files) throws IOException {
        fsUri = (null != fsUri) ? fsUri : "file:///";
        zip(getFileSystem(fsUri), out, files);
    }

    public static void zip(FileSystem fs, String archive, String... files) throws IOException {
        if (null == fs) {
            throw new NullPointerException("filesystem is null");
        }
        File archivePath = new File(archive);
        File parent = archivePath.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("cannot mkdirs: " + parent);
        }
        zip(fs, new FileOutputStream(archivePath), files);
    }

    public static void zip(FileSystem fs, OutputStream out, String... files) throws IOException {
        Path[] paths = new Path[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = Path.get(files[i]);
        }

        compressTo(fs, out, paths);
    }

    protected static void compressTo(FileSystem fs, OutputStream out, Path[] files) throws IOException {
        // ZipOutputStream zipOut = new ZipOutputStream(out, Charsets.UTF_8);
        ZipOutputStream zipOut = new ZipOutputStream(out);
        try {
            for (Path f : files) {
                doCompressEntry(fs, zipOut, f, f.getParent());
            }
        } finally {
            zipOut.flush();
            zipOut.close();
        }
    }

    private static void doCompressEntry(FileSystem fs, ZipOutputStream zipOut, Path file, Path baseDir) throws IOException {
        FileView view = fs.stat(file.getPath());
        if (null == view) {
            // not exists
            return;
        }

        LOG.debug("compress entry: " + file);

        String relativePath = file.relativize(baseDir).getPath();
        // 如果要添加一个目录,注意使用符号"/"作为添加项名字结尾符
        relativePath = view.isDirectory() && !relativePath.endsWith("/") ? relativePath + "/" : relativePath;
        relativePath = relativePath.startsWith("/") || relativePath.startsWith("\\") ? relativePath.substring(1) : relativePath;

        // 添加一个 entry
        zipOut.putNextEntry(new ZipEntry(relativePath));
        if (!view.isDirectory()) { // 如果是文件, 写入文件内容
            InputStream in = fs.open(file.getPath());
            if (null != in) {
                IOUtils.flow(in, zipOut, true, false);
            }
        }
        zipOut.closeEntry();

        // 如果是真实目录(非符号链接)遍历其下所有文件
        if (view.isDirectory() && !view.isSymlink()) {
            FileView[] views = fs.ls(file.getPath());
            for (FileView f : views) {
                String path = f.getPath();
                path = !path.startsWith("/") ? "/" + path : path;
                doCompressEntry(fs, zipOut, Path.get(path), baseDir);
            }
        }
    }


    private static String getRealPath(URI uri) {
        String path = uri.getPath();
        if (null != path && ("aliyun".equalsIgnoreCase(uri.getScheme()) || "qcloud".equalsIgnoreCase(uri.getScheme()))) {
            String realPath = "";
            int i = path.indexOf("/", 1);
            if (-1 < i && i != path.length() - 1) {
                realPath = path.substring(i + 1);
            }
            return realPath;
        }
        return path;
    }

    /* *****************************************************************
     *                    Get FileSystem
     * *****************************************************************/

    /**
     * 根据给定的文件系统 url 创建文件系统
     *
     * @param url 文件系统 url
     * @return 文件系统实例
     */
    public static FileSystem getFileSystem(String url) {
        return getFileSystem(url, null, null);
    }

    /**
     * 根据给定的文件系统url 和账号密码信息获取 文件系统实例
     *
     * @param url      文件系统 url
     * @param user     用户名
     * @param password 密码
     * @return 文件系统实例
     */
    public static FileSystem getFileSystem(String url, String user, String password) {
        Properties info = new Properties();
        if (null != user) {
            info.put("user", user);
        }
        if (null != password) {
            info.put("password", password);
        }
        return getFileSystem(url, info);
    }

    /**
     * 根据给定的文件系统url 和 配置信息获取 文件系统实例
     *
     * @param url  文件系统 url
     * @param info 配置信息
     * @return 文件系统实例
     */
    public static FileSystem getFileSystem(final String url, final Properties info) {
        try {
            final URI fsUri = new URI(url);
            final String scheme = fsUri.getScheme();
            final String userInfo = fsUri.getUserInfo();
            final String host = fsUri.getHost();
            final String path = fsUri.getPath();
            int port = fsUri.getPort();

            String username = null;
            String password = null;
            if (null != userInfo) {
                String[] split = userInfo.split(":", 2);
                username = split[0];
                password = 1 < split.length ? split[1] : null;
            }

            final String u = info.getProperty("user");
            final String p = info.getProperty("password");
            username = null != u ? u : username;
            password = null != p ? p : password;


            if ("local".equalsIgnoreCase(scheme) || "file".equalsIgnoreCase(scheme)) {
                // local:///path
                assertNotBlank(path, "path must be not blank ('" + url + "').");
                return new LocalFileSystem(path);
            }

            assertNotBlank(host, "host must be not blank ('" + url + "').");

            if ("ftp".equalsIgnoreCase(scheme)) {
                // sftp://user:password@host[/root_path]
                FtpFileSystem ftp = new FtpFileSystem();
                ftp.setHost(host);
                ftp.setPort(port);
                ftp.setUsername(username);
                ftp.setPassword(password);
                if (hasText(path)) {
                    ftp.setChroot(path);
                }
                return ftp;
            }

            if ("sftp".equalsIgnoreCase(scheme)) {
                // sftp://user:password@host[/root_path]
                port = 1 > port ? 22 : port;
                return createSftp(host, port, username, password, path);
            }

            if ("aliyun".equalsIgnoreCase(scheme)) {
                // aliyun://access_key_id:access_key_secure@bucket.host
                final int pos = host.indexOf('.');
                if (1 > pos) {
                    throw new IllegalArgumentException("No bucket found for '" + url + '\'');
                }

                final String bucketName = host.substring(0, pos);
                final String endpoint = host.substring(pos + 1);

                assertNotBlank(endpoint, "endpoint must be not blank ('" + url + "').");
                assertNotBlank(bucketName, "bucket must be not blank ('" + url + "').");
                assertNotBlank(username, "access key id not found, from '" + url + "' or '" + info + "'.");
                assertNotBlank(password, "access key secure not found, from '" + url + "' or '" + info + "'.");

                return new com.xyrfs.filemanager.impl.oss.AliyunOss(endpoint, username, password, bucketName);
            }

            if ("qcloud".equalsIgnoreCase(scheme)) {
                // qcloud://secret_id:secret_key@bucket.region
                final int pos = host.indexOf('.');
                if (1 > pos) {
                    throw new IllegalArgumentException("No bucket found for '" + url + '\'');
                }

                final String bucketName = host.substring(0, pos);
                final String region = host.substring(pos + 1);

                assertNotBlank(region, "region must be not blank ('" + url + "').");
                assertNotBlank(bucketName, "bucket must be not blank ('" + url + "').");
                assertNotBlank(username, "secret id not found, from '" + url + "' or '" + info + "'.");
                assertNotBlank(password, "secret key secure not found, from '" + url + "' or '" + info + "'.");

                return new com.xyrfs.filemanager.impl.oss.QCloudCosx(region, username, password, bucketName);
            }

            if ("hdfs".equalsIgnoreCase(scheme)) {
                return new com.xyrfs.filemanager.impl.hdfs.HdfsFileSystem(fsUri);
            }

            throw new IllegalArgumentException("schema is unsupported: " + scheme);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileSystem createSftp(String host, int port, String username, String password, String chroot) {
        if (JSCH_PRESENT) {
            com.xyrfs.filemanager.impl.sftp.JschSftpFileSystem sftpFileSystem = new com.xyrfs.filemanager.impl.sftp.JschSftpFileSystem();
            sftpFileSystem.setHost(host);
            sftpFileSystem.setPort(port);
            sftpFileSystem.setUsername(username);
            sftpFileSystem.setPassword(password);

            if (hasText(chroot)) {
                sftpFileSystem.setChroot(chroot);
            }

            return sftpFileSystem;
        } else if (J2SSH_MAVERICK_PRESENT) {
            com.xyrfs.filemanager.impl.sftp.J2sshMaverickSftpFileSystem sftpFileSystem = new com.xyrfs.filemanager.impl.sftp.J2sshMaverickSftpFileSystem();
            sftpFileSystem.setHost(host);
            sftpFileSystem.setUsername(username);
            sftpFileSystem.setPassword(password);

            if (hasText(chroot)) {
                sftpFileSystem.setChroot(chroot);
            }

            return sftpFileSystem;
        } else if (J2SSH_PRESENT) {
            com.xyrfs.filemanager.impl.sftp.J2sshSftpFileSystem sftpFileSystem = new com.xyrfs.filemanager.impl.sftp.J2sshSftpFileSystem();
            sftpFileSystem.setHost(host);
            sftpFileSystem.setPort(port);
            sftpFileSystem.setUsername(username);
            sftpFileSystem.setPassword(password);

            if (hasText(chroot)) {
                sftpFileSystem.setChroot(chroot);
            }

            return sftpFileSystem;
        }
        throw new IllegalStateException("J2SSH or JSCH not find at classpath");
    }

    private static void assertNotBlank(final String text, final String message) {
        if (!hasText(text)) {
            throw new IllegalStateException(message);
        }
    }

    private static boolean hasText(CharSequence charseq) {
        int len;
        if (charseq != null && (len = charseq.length()) != 0) {
            for (int i = 0; i < len; ++i) {
                if (!Character.isWhitespace(charseq.charAt(i))) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }
}
