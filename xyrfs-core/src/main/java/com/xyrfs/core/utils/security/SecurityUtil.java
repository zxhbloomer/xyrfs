package com.xyrfs.core.utils.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * 安全类工具类
 * @author Administrator
 */
public class SecurityUtil {

    /**
     * 获取login的Authentication
     * @return
     */
    public static Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }
    /**
     * 获取Principal
     * @return
     */
    public static Object getPrincipal(){
        return SecurityUtil.getAuthentication().getPrincipal();
    }
}