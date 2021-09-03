package com.xyrfs.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author zxh
 * @date 2019/9/27
 */
public class FsInvalidSessionStrategyException extends AuthenticationException {

    private static final long serialVersionUID = -1468881768452376477L;

    public FsInvalidSessionStrategyException(String message) {
        super(message);
    }
}
