package com.xyrfs.quartz.util;

import com.xyrfs.bean.entity.quartz.SJobEntity;
import com.xyrfs.bean.entity.quartz.SJobLogEntity;
import com.xyrfs.common.constant.ScheduleConstants;
import com.xyrfs.common.utils.bean.BeanUtilsSupport;
import com.xyrfs.core.service.quartz.ISJobLogService;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import java.time.LocalDateTime;

/**
 * 抽象quartz调用
 *
 * @author
 */
@Slf4j
@DisallowConcurrentExecution
public abstract class AbstractQuartzJob implements Job {

    /**
     * 线程本地变量
     */
    private static ThreadLocal<LocalDateTime> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SJobEntity sysJob = new SJobEntity();
        BeanUtilsSupport.copyProperties(context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES), sysJob);
        try {
            before(context, sysJob);
            if (sysJob != null) {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     */
    protected void before(JobExecutionContext context, SJobEntity sysJob) {
        threadLocal.set(LocalDateTime.now());
    }

    /**
     * 执行后，更新日志
     *
     */
    protected void after(JobExecutionContext context, SJobEntity sysJob, Exception e) {
        LocalDateTime startTime = threadLocal.get();
        threadLocal.remove();

        final SJobLogEntity sysJobLog = new SJobLogEntity();
        BeanUtilsSupport.copyProperties(sysJob, sysJobLog);
        sysJobLog.setJob_id(sysJob.getId());
        // 写入数据库当中
        BeanUtilsSupport.getBean(ISJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, SJobEntity sysJob) throws Exception;
}
