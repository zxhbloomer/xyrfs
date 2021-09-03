package com.xyrfs.common.enums.quartz;

import lombok.Getter;
import lombok.Setter;

/**
 * mq重要配置
 */
public enum QuartzEnum {
	TASK_TENANT_ENABLE(
						QuartzInfo.TenantEnableTask.job_name,
						QuartzInfo.TenantEnableTask.job_group_code,
						QuartzInfo.TenantEnableTask.job_group_name,
						QuartzInfo.TenantEnableTask.job_desc,
						QuartzInfo.TenantEnableTask.job_simple_name,
						QuartzInfo.TenantEnableTask.job_serial_type,
						QuartzInfo.TenantEnableTask.ok_msg
						),

	TASK_TENANT_DISABLE(
						QuartzInfo.TenantDisableTask.job_name,
						QuartzInfo.TenantDisableTask.job_group_code,
						QuartzInfo.TenantDisableTask.job_group_name,
						QuartzInfo.TenantDisableTask.job_desc,
						QuartzInfo.TenantDisableTask.job_simple_name,
						QuartzInfo.TenantDisableTask.job_serial_type,
						QuartzInfo.TenantDisableTask.ok_msg
						),
	;

	@Getter @Setter
	private String job_name;
	@Getter @Setter
	private String job_group_code;
	@Getter @Setter
	private String job_group_name;
	@Getter @Setter
	private String job_desc;
	@Getter @Setter
	private String job_simple_name;
	@Getter @Setter
	private String job_serial_type;
	@Getter @Setter
	private String ok_msg;

	/**
	 *
	 * @param job_name
	 * @param job_group_code
	 * @param job_group_name
	 * @param job_desc
	 * @param job_simple_name
	 * @param job_serial_type
	 * @param ok_msg
	 */
	private QuartzEnum(String job_name,
						String job_group_code,
						String job_group_name,
						String job_desc,
						String job_simple_name,
						String job_serial_type,
						String ok_msg) {
		this.job_name = job_name;
		this.job_group_code = job_group_code;
		this.job_group_name = job_group_name;
		this.job_desc = job_desc;
		this.job_simple_name = job_simple_name;
		this.job_serial_type = job_serial_type;
		this.ok_msg = ok_msg;
	}

	public static class QuartzInfo {

		/**
		 * 租户任务：启用
		 *
		 * @author zxh
		 * @date 2019年 10月12日 23:34:54
		 */
		public class TenantEnableTask {
			public static final String job_name = "租户定时任务：启用租户任务";
			public static final String job_group_code = "fs_task_tenant";
			public static final String job_group_name = "租户定时任务";
			public static final String job_desc = "系统自动生产该项任务:租户定时任务，启用任务";
			public static final String job_simple_name = "租户启用任务";
			public static final String job_serial_type = "s_tenant_enable_task";
			public static final String ok_msg = "租户定时任务：启用租户任务——执行成功";
		}

		/**
		 * 租户任务：禁用，关闭
		 *
		 * @author zxh
		 * @date 2019年 10月12日 23:34:54
		 */
		public class TenantDisableTask {
			public static final String job_name = "租户定时任务：停止启用租户任务";
			public static final String job_group_code = "fs_task_tenant";
			public static final String job_group_name = "租户定时任务";
			public static final String job_desc = "系统自动生产该项任务:租户定时任务，停止启用租户";
			public static final String job_simple_name = "租户停止任务";
			public static final String job_serial_type = "s_tenant_disable_task";
			public static final String ok_msg = "租户定时任务：停止启用租户任务——执行成功";
		}
	}
}
