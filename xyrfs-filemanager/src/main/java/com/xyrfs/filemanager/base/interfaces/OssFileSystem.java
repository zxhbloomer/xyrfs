package com.xyrfs.filemanager.base.interfaces;

import com.xyrfs.filemanager.base.interfaces.FileSystem;

/**
 * 开放云存储标识接口
 * <p/>
 * 用于对于云存储进行特殊的逻辑, 例如: 通过URL读取可以做优化(压缩,图片处理等), 禁止通过 SDK 读取
 *
 * @author vacoor
 */
public interface OssFileSystem extends FileSystem {
}
