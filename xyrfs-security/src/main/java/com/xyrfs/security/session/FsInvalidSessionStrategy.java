package com.xyrfs.security.session;

import com.xyrfs.bean.result.utils.v1.ResponseResultUtil;
import com.xyrfs.common.enums.ResultEnum;
import com.xyrfs.common.exception.FsInvalidSessionStrategyException;
import com.xyrfs.security.properties.FsSecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理 session 失效
 */
public class FsInvalidSessionStrategy implements InvalidSessionStrategy {

    private FsSecurityProperties fsSecurityProperties;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private boolean createNewSession = true;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // session过期后自动获取判断
        if (this.fsSecurityProperties.getCreateNewSession()) {
            request.getSession();
        }
        // if (CommonUtil.isAjaxRequest(request)) {

        ResponseResultUtil.responseWriteError(request,
            response,
            new FsInvalidSessionStrategyException("您的会话已过期，请重新登录！"),
            HttpStatus.UNAUTHORIZED.value(),
            ResultEnum.USER_SESSION_TIME_OUT_ERROR,
            "您的会话已过期，请重新登录！");

        // }
        // redirectStrategy.sendRedirect(request, response, fsSecurityProperties.getLogoutUrl());
    }

    public void setFsSecurityProperties(FsSecurityProperties fsSecurityProperties) {
        this.fsSecurityProperties = fsSecurityProperties;
    }
}
