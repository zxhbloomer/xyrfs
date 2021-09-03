package com.xyrfs.bean.result.utils.v1;

import com.xyrfs.bean.pojo.result.InsertResult;

/**
 * @author zxh
 * @date 2019/9/2
 */
public class InsertResultUtil {

    /**
     * 没有错误，返回结果
     * @param _data
     * @param <T>
     * @return
     */
    public static <T> InsertResult<T> OK(T _data) {
        return InsertResult.<T>builder()
            .data(_data)
            .message("")
            .success(true)
            .build();
    }

    /**
     * 返回错误
     * @param _data
     * @param _message
     * @param <T>
     * @return
     */
    public static <T> InsertResult<T> NG(T _data, String _message) {
        return InsertResult.<T>builder()
            .data(_data)
            .message(_message)
            .success(false)
            .build();
    }
}
