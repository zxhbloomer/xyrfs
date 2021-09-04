package com.xyrfs.filemanager.base.interfaces;

import com.xyrfs.filemanager.FileView;
import com.xyrfs.filemanager.base.exception.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件系统
 * <p/>
 * 该接口定义一个与具体实现无关的文件系统接口
 *
 * @author vacoor
 */
public interface FileSystem {

    /**
     * 给定路径是否存在
     *
     * @param path 文件系统路径
     * @throws FileSystemException
     */
    boolean exists(String path) throws FileSystemException;

    /**
     * 获取路径的文件状态
     *
     * @param path 文件系统路径
     * @return 返回文件状态, 如果文件不存在返回 null
     * @throws FileSystemException
     */
    FileView stat(String path) throws FileSystemException;

    /**
     * 获取路径的文件状态
     *
     * @param path 文件或目录路径
     * @return 文件或目录的状态, 如果目录下没有文件或文件不存在返回空数组
     * @throws FileSystemException
     */
    FileView[] ls(String path) throws FileSystemException;

    /**
     * 创建给定路径的目录, 如果目录已经创建成功或已经存在返回 true, 创建失败返回 false
     * 如果已经存在但不是一个目录则抛出异常
     *
     * @param path 目录路径
     * @throws FileSystemException
     */
    boolean mkdirs(String path) throws FileSystemException;

    /**
     * 打开给定路径的文件, 如果给定路径不存在返回null, 如果不是文件将抛出异常
     * <p/>
     * 注: 关闭输入流时才会关闭相关资源, 因此读取完毕后务必关闭输入流
     *
     * @param file 文件路径
     * @return 文件输入流或null
     * @throws FileSystemException
     */
    InputStream open(String file) throws FileSystemException;

    /**
     * 获取一个创建文件的输出流并自动创建相关目录, 当 override = false, 如果文件已经存在则抛出异常
     *
     * @param file     要创建的文件路径
     * @param override 如果文件已经存在是否覆盖
     * @return 新建文件的输出流
     * @throws FileSystemException
     */
    OutputStream create(String file, boolean override) throws FileSystemException;

    /**
     * 获取一个创建文件的输出流并自动创建相关目录, 当 override = false, 如果文件已经存在则抛出异常
     *
     * @param file        要创建的文件路径
     * @param contentType MIME 类型, 某些文件系统会记录该值, 例如 OSS 在返回时使用
     * @param override    如果文件已经存在是否覆盖
     * @return 新建文件的输出流
     * @throws FileSystemException
     */
    OutputStream create(String file, String contentType, boolean override) throws FileSystemException;

    /**
     * 删除给定路径
     *
     * @param path      文件或目录路径
     * @param force     如果文件夹中存在文件是否强制删除
     * @param recursive 如果文件夹中存在目录是否递归删除
     * @throws FileSystemException
     */
    void rm(String path, boolean force, boolean recursive) throws FileSystemException;

    /**
     * 重命名路径
     *
     * @param oldPath 源路径
     * @param newPath 目标路径
     * @throws FileSystemException
     */
    void rename(String oldPath, String newPath) throws FileSystemException;

}
