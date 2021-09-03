package com.xyrfs.quartz.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.sql.DataSource;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import java.lang.reflect.Method;

/**
 * 定时任务配置
 *
 * @author Administrator
 */
@Configuration
public class ScheduleConfig {

    @Bean("fsScheduler")
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);

        // quartz参数
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "FsScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        // 线程池配置
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "20");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        // JobStore配置
        prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        // 集群配置
        prop.put("org.quartz.jobStore.isClustered", "true");
        prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");

        // sqlserver 启用
        // prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        factory.setQuartzProperties(prop);

        factory.setSchedulerName("FsScheduler");
        // 延时启动
        factory.setStartupDelay(1);
        factory.setApplicationContextSchedulerContextKey("applicationContextKey");
        // 可选，QuartzScheduler
        // 启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
        factory.setOverwriteExistingJobs(true);
        // 设置自动启动，默认为true
        factory.setAutoStartup(true);

        return factory;
    }

    /**
     * 定时任务作业类。
     * @author test1248
     *
     */
    public class DetailQuartzJobBean extends QuartzJobBean {

        private String targetObject;
        private String targetMethod;
        private ApplicationContext ctx;

        public void setTargetObject(String targetObject) {
            this.targetObject = targetObject;
        }

        public void setTargetMethod(String targetMethod) {
            this.targetMethod = targetMethod;
        }

        public void setApplicationContext(ApplicationContext ctx) {
            this.ctx = ctx;
        }

        // 配置中设定了
        // ① targetMethod: 指定需要定时执行scheduleInfoAction中的simpleJobTest()方法
        // ② concurrent：对于相同的JobDetail，当指定多个Trigger时, 很可能第一个job完成之前，
        // 第二个job就开始了。指定concurrent设为false，多个job不会并发运行，第二个job将不会在第一个job完成之前开始。
        // ③ cronExpression：0/10 * * * * ?表示每10秒执行一次，具体可参考附表。
        // ④ triggers：通过再添加其他的ref元素可在list中放置多个触发器。 scheduleInfoAction中的simpleJobTest()方法
        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            try {
                Object otargetObject = ctx.getBean(targetObject);
                Method m = null;

                System.out.println(targetObject + " - " + targetMethod + " - " + ((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date())));
                try {
                    m = otargetObject.getClass().getMethod(targetMethod, new Class[] { JobExecutionContext.class });
                    m.invoke(otargetObject, new Object[] { context });
                } catch (SecurityException e) {
                    // Logger.error(e);
                    System.out.println(e.getMessage());
                } catch (NoSuchMethodException e) {
                    // Logger.error(e);
                    System.out.println(e.getMessage());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new JobExecutionException(e);
            }
        }

    }
}