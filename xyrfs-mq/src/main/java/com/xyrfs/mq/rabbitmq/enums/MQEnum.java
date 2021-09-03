package com.xyrfs.mq.rabbitmq.enums;

/**
 * mq重要配置
 */
public enum MQEnum {
	MQ_TASK_Tenant_ENABLE(MqInfo.TenantEnableTask.queueCode,
		MqInfo.TenantEnableTask.name,
		MqInfo.TenantEnableTask.exchange,
		MqInfo.TenantEnableTask.routing_key),
	MQ_TASK_Tenant_Disable(MqInfo.TenantDisableTask.queueCode,
		MqInfo.TenantDisableTask.name,
		MqInfo.TenantDisableTask.exchange,
		MqInfo.TenantDisableTask.routing_key)
	;

	private String queueCode;
	private String name;
	private String exchange;
	private String routing_key;

	private MQEnum(String queueCode, String name, String exchange, String routing_key) {
		this.queueCode = queueCode;
		this.name = name;
		this.exchange = exchange;
		this.routing_key = routing_key;
	}

	public String getQueueCode() {
		return queueCode;
	}

	public void setQueueCode(String queueCode) {
		this.queueCode = queueCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getRouting_key() {
		return routing_key;
	}

	public void setRouting_key(String routing_key) {
		this.routing_key = routing_key;
	}

	public static class MqInfo {

		/**
		 * 平台任务类，需要在quartz中实现
		 *
		 * @author zxh
		 * @date 2019年 10月12日 23:34:54
		 */
		public class Task {
			public static final String queueCode = "xyrfs-task";
			public static final String name = "平台任务类";
			public static final String exchange = "xyrfs-task-exchange";
			public static final String routing_key = "xyrfs-task.#";
		}

		/**
		 * 租户任务消息队列,需要在quartz中实现：启用
		 *
		 * @author zxh
		 * @date 2019年 10月12日 23:34:54
		 */
		public class TenantEnableTask {
			public static final String queueCode = "xyrfs-task-tenant-enable";
			public static final String name = "租户任务消息队列：启用";
			public static final String exchange = "xyrfs-task-tenant-enable-exchange";
			public static final String routing_key = "xyrfs-task-tenant-enable.#";
		}

		/**
		 * 租户任务消息队列,需要在quartz中实现：禁用
		 *
		 * @author zxh
		 * @date 2019年 10月12日 23:34:54
		 */
		public class TenantDisableTask {
			public static final String queueCode = "xyrfs-task-tenant-disable";
			public static final String name = "租户任务消息队列：关闭";
			public static final String exchange = "xyrfs-task-tenant-disable-exchange";
			public static final String routing_key = "xyrfs-task-tenant-disable.#";
		}
	}
}
