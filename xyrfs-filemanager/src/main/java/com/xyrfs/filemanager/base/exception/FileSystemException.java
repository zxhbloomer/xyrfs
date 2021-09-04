package com.xyrfs.filemanager.base.exception;

/**
 * 文件系统异常
 *
 * @author zxh
 */
public class FileSystemException extends RuntimeException {

    public FileSystemException() {
    }

    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSystemException(Throwable cause) {
        super(cause);
    }

    public static <R> R rethrowFileSystemException(Throwable exception) {
        if (exception instanceof Error) {
            throw (Error) exception;
        }
        if (exception instanceof FileSystemException) {
            throw (FileSystemException) exception;
        }
        throw new FileSystemException(exception);
    }
}
