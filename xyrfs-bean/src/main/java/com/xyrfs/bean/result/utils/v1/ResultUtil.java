package com.xyrfs.bean.result.utils.v1;

import com.xyrfs.bean.pojo.result.JsonResult;
import com.xyrfs.common.constant.JsonResultTypeConstants;
import com.xyrfs.common.enums.ResultEnum;
import com.xyrfs.common.utils.CommonUtil;
import com.xyrfs.common.utils.DateTimeUtil;
import com.xyrfs.common.utils.ExceptionUtil;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * json返回值工具类
 * @author zxh
 */
public class ResultUtil {

    public static <T> JsonResult<T> OK(T data, String message) {
        return JsonResult.<T>builder()
            .timestamp(DateTimeUtil.getTime())
            .http_status(HttpStatus.OK.value())
            .system_code(ResultEnum.OK.getCode())
            .system_message(ResultEnum.OK.getMsg())
            .message(message)
            .path(CommonUtil.getRequest().getRequestURL().toString())
            .method(CommonUtil.getRequest().getMethod())
            .success(true)
            .data(data)
            .build();
    }

    /**
     * 无错误的返回
     * @param data
     * @param <T>
     * @return
     */
    public static <T>JsonResult<T> OK(T data, Integer json_null_out) {
        return JsonResult.<T>builder()
                .timestamp(DateTimeUtil.getTime())
                .http_status(HttpStatus.OK.value())
                .system_code(ResultEnum.OK.getCode())
                .system_message(ResultEnum.OK.getMsg())
                .message("调用成功")
                .path(CommonUtil.getRequest().getRequestURL().toString())
                .method(CommonUtil.getRequest().getMethod())
                .success(true)
                .json_result_type(json_null_out)
                .data(data)
                .build();
    }

    /**
     * 无错误的返回
     * @param data
     * @param <T>
     * @return
     */
    public static <T>JsonResult<T> OK(T data) {
        return ResultUtil.OK(data, JsonResultTypeConstants.NORMAL);
    }

    /**
     * 含code的无错误的返回
     * @param data
     * @param system_code
     * @param <T>
     * @return
     */
    public static <T>JsonResult<T> OK(T data, ResultEnum system_code) {
        return JsonResult.<T>builder()
            .timestamp(DateTimeUtil.getTime())
            .http_status(HttpStatus.OK.value())
            .system_code(system_code.getCode())
            .system_message(system_code.getMsg())
            .message("调用成功")
            .path(CommonUtil.getRequest().getRequestURL().toString())
            .method(CommonUtil.getRequest().getMethod())
            .success(true)
            .data(data)
            .build();
    }

    public static <T>JsonResult<T> NG(Integer httpStatus,ResultEnum system_code, Exception exception, String message, HttpServletRequest request) {

        return JsonResult.<T>builder()
                .timestamp(DateTimeUtil.getTime())
                .http_status(httpStatus)
                .system_code(system_code.getCode())
                .system_message(system_code.getMsg())
                .message(message)
                .path(request.getRequestURL().toString())
                .method(request.getMethod())
                .success(false)
                .data((T) ExceptionUtil.getException(exception))
                .build();
    }
}
