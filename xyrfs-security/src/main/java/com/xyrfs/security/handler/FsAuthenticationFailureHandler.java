package com.xyrfs.security.handler;

import com.xyrfs.bean.result.utils.v1.ResponseResultUtil;
import com.xyrfs.common.enums.ResultEnum;
import com.xyrfs.common.exception.CredentialException;
import com.xyrfs.common.exception.ValidateCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理器
 */
@Component
public class FsAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String message;
        if (exception instanceof UsernameNotFoundException) {
            message = "用户不存在！";
        } else if (exception instanceof BadCredentialsException) {
            message = "登录名或登录密码不正确！";
        } else if (exception instanceof LockedException) {
            message = "用户已被锁定！";
        } else if (exception instanceof DisabledException) {
            message = "用户不可用！";
        } else if (exception instanceof AccountExpiredException) {
            message = "账户已过期！";
        } else if (exception instanceof CredentialsExpiredException) {
            message = "用户密码已过期！";
        } else if (exception instanceof ValidateCodeException || exception instanceof CredentialException) {
            message = exception.getMessage();
        } else {
            message = "认证失败，请联系网站管理员！";
        }
        ResponseResultUtil.responseWriteError(request,
            response,
            exception,
            HttpStatus.NOT_ACCEPTABLE.value(),
            ResultEnum.USER_AUTHENTICATION_ERROR,
            message);
    }
}

