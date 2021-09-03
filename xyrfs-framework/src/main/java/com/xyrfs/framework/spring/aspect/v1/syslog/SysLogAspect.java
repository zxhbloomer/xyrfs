package com.xyrfs.framework.spring.aspect.v1.syslog;

import com.alibaba.fastjson.JSON;
import com.xyrfs.bean.bo.log.sys.SysLogBo;
import com.xyrfs.bean.config.base.v1.BaseVo;
import com.xyrfs.bean.entity.log.sys.SLogSysEntity;
import com.xyrfs.common.annotations.SysLogAnnotion;
import com.xyrfs.common.constant.FsConstant;
import com.xyrfs.common.properies.FsConfigProperies;
import com.xyrfs.common.utils.ExceptionUtil;
import com.xyrfs.common.utils.IPUtil;
import com.xyrfs.common.utils.servlet.ServletUtil;
import com.xyrfs.core.service.log.sys.ISLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zxh
 */
@Aspect
@Component
@Slf4j
public class SysLogAspect {

    @Autowired
    private FsConfigProperies fsConfigProperies;

    @Autowired
    private ISLogService iSLogService;

    @Pointcut("@annotation(com.xyrfs.common.annotations.SysLogAnnotion)")
    public void sysLogAspect(){}

    /**
     * 环绕通知 @Around  ， 当然也可以使用 @Before (前置通知)  @After (后置通知)
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("sysLogAspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        BigDecimal time =  new BigDecimal(System.currentTimeMillis() - beginTime);
        SLogSysEntity entity = new SLogSysEntity();
        try {
            SysLogBo sysLogBo = printLog(point, time.longValue());
            if (fsConfigProperies.isLogSaveDb()){
                entity.setOperation(sysLogBo.getRemark());
                entity.setUrl(sysLogBo.getUrl());
                entity.setTime(sysLogBo.getExecTime());
                entity.setHttp_method(sysLogBo.getHttpMethod());
                entity.setClass_name(sysLogBo.getClassName());
                entity.setClass_method(sysLogBo.getClassMethod());
                entity.setIp(sysLogBo.getIp());
                entity.setParams(sysLogBo.getParams());
                entity.setC_time(sysLogBo.getCreateDate());
                entity.setType(FsConstant.LOG_FLG.OK);
                // 获取session
                Object session = ServletUtil.getUserSession();
                String userSessionJson = null;
                if(session != null){
//                    UserSessionBo userSession = (UserSessionBo)session;
//                    userSessionJson = JSON.toJSONString(userSession);
//                    entity.setUser_name(userSession.getStaff_info().getName());
                }
                entity.setSession(userSessionJson);
                entity.setException(null);
            }
        } catch (Exception e) {
            entity.setException(e.getMessage());
            entity.setType(FsConstant.LOG_FLG.NG);
            log.error("发生异常");
            log.error(e.getMessage());

        }
        iSLogService.save(entity);
        return result;
    }

    /**
     *  异常通知
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "sysLogAspect()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        // 获取request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        SLogSysEntity entity = new SLogSysEntity();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLogAnnotion sysLog = method.getAnnotation(SysLogAnnotion.class);
        if (fsConfigProperies.isLogSaveDb()){
            entity.setOperation(sysLog.value());
            entity.setUrl(request.getRequestURL().toString());
            entity.setTime(null);
            entity.setHttp_method(request.getMethod());
            entity.setClass_name(joinPoint.getTarget().getClass().getName());
            entity.setClass_method(((MethodSignature) joinPoint.getSignature()).getName());
            entity.setIp(IPUtil.getIpAdd());
            entity.setParams(convertArgsToJsonString(joinPoint.getArgs()));
            entity.setC_time(LocalDateTime.now());
            entity.setType(FsConstant.LOG_FLG.NG);
            // 获取session
            Object session = ServletUtil.getUserSession();
            String userSessionJson = null;
            if(session != null){
//                UserSessionBo userSession = (UserSessionBo)session;
//                userSessionJson = JSON.toJSONString(userSession);
//                entity.setUser_name(userSession.getStaff_info().getName());
            }
            entity.setSession(userSessionJson);
            entity.setException(ExceptionUtil.getException(e));
        }
        iSLogService.save(entity);
    }

    /**
     * 打印日志
     * @param joinPoint
     * @param time
     */
    private SysLogBo printLog(ProceedingJoinPoint joinPoint, Long time) {
        // 获取request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SysLogAnnotion sysLog = method.getAnnotation(SysLogAnnotion.class);
        SysLogBo sysLogBo = SysLogBo.builder()
            .className(joinPoint.getTarget().getClass().getName())
            .httpMethod(request.getMethod())
            .classMethod(((MethodSignature) joinPoint.getSignature()).getName())
            .params( convertArgsToJsonString(joinPoint.getArgs()))
            .execTime(time)
            .remark(sysLog.value())
            .createDate(LocalDateTime.now())
            .url(request.getRequestURL().toString())
            .ip(IPUtil.getIpAdd())
            .build();
        if(fsConfigProperies.isSysLog()){
            log.debug("======================日志开始================================");
            log.debug("日志名称         : " + sysLogBo.getRemark());
            log.debug("URL             : " + sysLogBo.getUrl());
            log.debug("HTTP方法         : " + sysLogBo.getHttpMethod());
            log.debug("IP               : " + sysLogBo.getIp());
            log.debug("类名             : " + sysLogBo.getClassName());
            log.debug("类方法           : " + sysLogBo.getClassMethod());
            log.debug("执行时间         : " + new BigDecimal(sysLogBo.getExecTime()).divide(BigDecimal.valueOf(1000)).toString() + "秒");
            log.debug("执行日期         : " + sysLogBo.getCreateDate());
            log.debug("参数             : " + sysLogBo.getParams());
            log.debug("======================日志结束================================");
        }
        return sysLogBo;
    }

    /**
     * 转换成json
     * @param args
     * @return
     */
    private String convertArgsToJsonString(Object[] args){
        if (args == null) {
            return null;
        }
        for (Object o : args) {
            if(o instanceof BaseVo){
                return JSON.toJSONString(o);
            }
        }
        return null;
    }
}