package com.xyrfs.common.annotations;

import com.xyrfs.common.enums.OperationEnum;
import com.xyrfs.common.enums.ParameterEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 具体的操作，日志中需要保存的内容
 * @author zxh
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogByIdsAnnotion {
	/** 业务名 */
	String name();
	/** 操作类型 */
	OperationEnum type();
	/** 业务操作说明 */
	String oper_info();
	/** 表名 */
	String table_name();
	/** 需要记录的字段 */
	String[] cloums() default {};
	/** id所在参数的位置 */
	ParameterEnum id_position();
	/** id字段的值，默认只识别第一个参数 */
	String ids() default "";
}
