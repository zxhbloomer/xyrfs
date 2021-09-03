package com.xyrfs.bean.result.utils.v1;

import com.xyrfs.bean.pojo.result.DeleteResult;

/**
 * @author zxh
 * @date 2019/9/2
 */
public class DeleteResultUtil {
    /**
     * 没有错误，返回结果
     * @param _data
     * @param <T>
     * @return
     */
    public static <T> DeleteResult<T> OK(T _data) {
        return DeleteResult.<T>builder()
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
    public static <T> DeleteResult<T> NG(T _data, String _message) {
        return DeleteResult.<T>builder()
            .data(_data)
            .message(_message)
            .success(false)
            .build();
    }
}
