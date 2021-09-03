package com.xyrfs.excel.upload;

/**
 * 异常类
 * @author zxh
 */
public class FsExcelException extends RuntimeException {

	private static final long serialVersionUID = 1830974553436749465L;

	public FsExcelException() {

	}

	public FsExcelException(String message) {
		super(message);
	}

	public FsExcelException(Throwable cause) {
		super(cause);
	}

	public FsExcelException(String message, Throwable cause) {
		super(message, cause);
	}

	public FsExcelException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
