package com.xyrfs.bean.pojo.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zxh
 * @date 2019/8/30
 */
@Data
@Builder
@AllArgsConstructor
public class CheckResult implements Serializable {

    /**
     * check的区分
     */
    public static final String INSERT_CHECK_TYPE = "INSERT_CHECK_TYPE";
    public static final String UPDATE_CHECK_TYPE = "UPDATE_CHECK_TYPE";
    public static final String DELETE_CHECK_TYPE = "DELETE_CHECK_TYPE";
    // 删除复原
    public static final String UNDELETE_CHECK_TYPE = "UNDELETE_CHECK_TYPE";
    public static final String SELECT_CHECK_TYPE = "SELECT_CHECK_TYPE";
    public static final String COPY_INSERT_CHECK_TYPE = "COPY_INSERT_CHECK_TYPE";
    public static final String OTHER_CHECK_TYPE = "OTHER_CHECK_TYPE";

    private static final long serialVersionUID = 1505396357464554154L;

    /** 返回消息：返回的消息 */
    private String message;

    /** 是否成功[true:成功;false:失败]，默认失败 */
    private boolean success;

    /** 返回数据 */
    private Object data ;
}
