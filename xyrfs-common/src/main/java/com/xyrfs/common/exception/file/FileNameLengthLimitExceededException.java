package com.xyrfs.common.exception.file;

/**
 * 文件名称超长限制异常类
 * 
 * @author ruoyi
 */
public class FileNameLengthLimitExceededException extends FileException
{

    private static final long serialVersionUID = 3394766898025866593L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength)
    {
//        super("upload.filename.exceed.length", new Object[] { defaultFileNameLength });
    }
}
