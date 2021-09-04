package com.xyrfs.filemanager;

import java.util.Date;

/**
 * 文件视图
 * <p/>
 * 文件视图定义了文件的基本信息, 包含路径, 大小, 类型, 修改时间, 权限, 拥有者, 所属组等信息
 *
 * @author
 * @see <a href="https://www.ibm.com/developerworks/community/blogs/IBMzOS/entry/20140704?lang=en">USS</a>
 */
public class FileView {
    public static final int SUID = 04000;           // set user ID on execution
    public static final int SGID = 02000;           // set group ID on execution
    public static final int STICKY = 01000;         // sticky bit   ****** NOT DOCUMENTED *****
    public static final int OWNER_READ = 00400;     // read by owner
    public static final int OWNER_WRITE = 00200;    // write by owner
    public static final int OWNER_EXECUTE = 00100;  // execute/search by owner
    public static final int GROUP_READ = 00040;     // read by group
    public static final int GROUP_WRITE = 00020;    // write by group
    public static final int GROUP_EXECUTE = 00010;  // execute/search by group
    public static final int OTHER_READ = 00004;     // read by others
    public static final int OTHER_WRITE = 00002;    // write by others
    public static final int OTHER_EXECUTE = 00001;  // execute/search by others

    private String path;
    private long length;
    private boolean isDirectory;
    private long lastModifiedTime;          // 修改时间, 距离 1970.01.01 00:00:00 毫秒
    private long lastAccessTime;            // 访问时间
    private int permissions;                // set UID + set GID + Sticky + File Permissions
    private String owner;               // 文件所有者
    private String group;               // 文件所有组
    private String symlink;             // 链接真实路径

    /**
     * 构建一个文件视图
     *
     * @param path             文件路径
     * @param length           文件大小
     * @param isDirectory      是否是目录
     * @param lastModifiedTime 最后修改时间
     * @param lastAccessTime   最后访问时间
     * @param permissions      权限掩码
     * @param owner            拥有者
     * @param group            所属组
     * @param symlink          符号链接对应的真实路径
     */
    public FileView(String path, long length, boolean isDirectory, long lastModifiedTime, long lastAccessTime, int permissions, String owner, String group, String symlink) {
        this.path = path;
        this.length = length;
        this.isDirectory = isDirectory;
        this.lastModifiedTime = lastModifiedTime;
        this.lastAccessTime = lastAccessTime;
        this.permissions = permissions;
        this.owner = owner;
        this.group = group;
        this.symlink = symlink;
    }

    /**
     * 当前视图是否是目录
     *
     * @return 如果当前视图是目录则返回true, 否则返回false
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * 获取当前视图对应的文件路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取当前文件的大小
     */
    public long getLength() {
        return length;
    }

    /**
     * 获取当前文件的最后修改时间
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * 获取当前文件的最后昂文时间
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * 获取当前文件的权限掩码
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * 获取当前文件的拥有者
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 获取当前文件的所属组
     */
    public String getGroup() {
        return group;
    }

    /**
     * 获取当前文件的符号链接
     */
    public String getSymlink() {
        return symlink;
    }

    /**
     * 当前文件视图是否是一个符号链接
     *
     * @return 如果是符号链接返回 true, 否则返回 false
     */
    public boolean isSymlink() {
        return null != getSymlink();
    }

    /**
     * 获取权限掩码字符串形式, eg: drwxrw---
     */
    public String getPermissionsString() {
        StringBuffer buf = new StringBuffer(10);
        int m = permissions;

        buf.append(isDirectory() ? 'd' : (isSymlink() ? 'l' : '-'));
        buf.append((m & OWNER_READ) != 0 ? 'r' : '-');
        buf.append((m & OWNER_WRITE) != 0 ? 'w' : '-');
        buf.append((m & SUID) != 0 ? 's' : ((m & OWNER_EXECUTE) != 0 ? 'x' : '-'));
        buf.append((m & GROUP_READ) != 0 ? 'r' : '-');
        buf.append((m & GROUP_WRITE) != 0 ? 'w' : '-');
        buf.append((m & SGID) != 0 ? 's' : ((m & GROUP_EXECUTE) != 0 ? 'x' : '-'));
        buf.append((m & OTHER_READ) != 0 ? 'r' : '-');
        buf.append((m & OTHER_WRITE) != 0 ? 'w' : '-');
        buf.append((m & OTHER_EXECUTE) != 0 ? 'x' : '-');

        return buf.toString();
    }

    /**
     * 获取最后修改时间的字符串形式， 注: 字符串形式取决于操作系统
     */
    public String getAtimeString() {
        return new Date(lastAccessTime).toString();
    }

    /**
     * 获取最后修改时间的字符串形式， 注: 字符串形式取决于操作系统
     */
    public String getMtimeString() {
        return new Date(lastModifiedTime).toString();
    }

    public String toString() {
        return (getPermissionsString() + " " + getOwner() + " " + getGroup() + " " + getLength() + " " + getMtimeString() + " " + path + (isSymlink() ? " -> " + getSymlink() : ""));
    }
}
