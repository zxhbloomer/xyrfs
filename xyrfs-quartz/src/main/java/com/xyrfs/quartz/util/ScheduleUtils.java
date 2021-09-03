package com.xyrfs.quartz.util;

import com.xyrfs.bean.entity.quartz.SJobEntity;
import com.xyrfs.common.annotations.SysLogAnnotion;
import com.xyrfs.common.constant.ScheduleConstants;
import com.xyrfs.common.exception.job.TaskException;
import com.xyrfs.common.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.LocalDateTime;

/**
 * 定时任务工具类
 *
 * @author ruoyi
 */
@Slf4j
public class ScheduleUtils {

    /**
     * 构建任务触发对象
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 创建定时任务:SimpleTrigger
     */
    @SysLogAnnotion("创建定时任务SimpleTrigger")
    public static boolean createScheduleJobSimpleTrigger(Scheduler scheduler, SJobEntity job, Class<? extends Job> jobClass) throws SchedulerException, TaskException {
        log.debug("创建定时任务开始SimpleTrigger");
        // 构建job信息
        Long jobId = job.getId();
        String jobGroup = job.getJob_group_code();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();
        // 设置一个用于触发的时间
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder = handleSimpleScheduleMisfirePolicy(simpleScheduleBuilder);
        // 新的Simpletrigger
        SimpleTrigger trigger ;
        if (job.getNext_run_time().isBefore(LocalDateTime.now())) {
            trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(simpleScheduleBuilder)
                .startNow()
                .build();
        } else {
            trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(simpleScheduleBuilder)
                .startAt(LocalDateTimeUtils.convertLDTToDate(job.getNext_run_time()))
                .build();
        }

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, job);

        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            log.debug("定时任务已存在，进行删除--jobname:" + job.getJob_name());
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
            log.debug("定时任务已存在，删除成功--jobname:" + job.getJob_name());
        }

        log.debug("创建定时任务：启动--jobname:" + job.getJob_name());
        scheduler.scheduleJob(jobDetail, trigger);
        log.debug("创建定时任务：成功--jobname:" + job.getJob_name());

        // 暂停任务
        if (job.getIs_effected() != null && job.getIs_effected() == ScheduleConstants.Status.PAUSE.getValue()) {
            log.debug("定时任务，进行暂停：开始--jobname:" + job.getJob_name());
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            log.debug("定时任务，进行暂停：成功--jobname:" + job.getJob_name());
        } else {
            log.debug("定时任务，暂停恢复：开始--jobname:" + job.getJob_name());
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            log.debug("定时任务，暂停恢复：结束--jobname:" + job.getJob_name());
        }
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建定时任务:Cron表达式
     */
    @SysLogAnnotion("创建定时任务CroTrigger")
    public static boolean createScheduleJobCron(Scheduler scheduler, SJobEntity job, Class<? extends Job> jobClass) throws SchedulerException, TaskException {
        log.debug("创建定时任务开始CronTrigger");
        // 构建job信息
        Long jobId = job.getId();
        String jobGroup = job.getJob_group_code();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();

        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCron_expression());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);

        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger =
            TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobId, jobGroup)).withSchedule(cronScheduleBuilder)
                .build();

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, job);

        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            log.debug("定时任务已存在，进行删除--jobname:" + job.getJob_name());
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
            log.debug("定时任务已存在，删除成功--jobname:" + job.getJob_name());
        }
        log.debug("创建定时任务：启动--jobname:" + job.getJob_name());
        scheduler.scheduleJob(jobDetail, trigger);
        log.debug("创建定时任务：成功--jobname:" + job.getJob_name());

        // 暂停任务
        if (job.getIs_effected() == ScheduleConstants.Status.PAUSE.getValue()) {
            log.debug("定时任务，需要进行暂停：开始");
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            log.debug("定时任务，需要进行暂停：成功");
        } else {
            log.debug("定时任务，暂停恢复：开始--jobname:" + job.getJob_name());
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            log.debug("定时任务，暂停恢复：结束--jobname:" + job.getJob_name());
        }
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置定时任务策略：Cron
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(SJobEntity job, CronScheduleBuilder cb)
        throws TaskException {
        switch (job.getMisfire_policy()) {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new TaskException(
                    "The task misfire policy '" + job.getMisfire_policy() + "' cannot be used in cron schedule tasks",
                    TaskException.Code.CONFIG_ERROR);
        }
    }

    /**
     * 设置定时任务策略：Cron
     */
    public static SimpleScheduleBuilder handleSimpleScheduleMisfirePolicy(SimpleScheduleBuilder sb) {
        return sb.withMisfireHandlingInstructionIgnoreMisfires();
    }
}