package com.xyrfs.bean.result.utils.v1;

import com.xyrfs.bean.pojo.result.CheckResult;

/**
 * @author zxh
 * @date 2019/8/30
 */
public class CheckResultUtil {

    /**
     * check无误
     * @return
     */
    public static CheckResult OK() {
        return CheckResult.builder()
            .data(null)
            .message("")
            .success(true)
            .build();
    }

    /**
     * check有错
     * @return
     */
    public static CheckResult NG(String msg) {
        return CheckResult.builder()
            .data(null)
            .message(msg)
            .success(false)
            .build();
    }

    /**
     * check有错
     * @return
     */
    public static CheckResult NG(String msg, Object _data ) {
        return CheckResult.builder()
            .data(_data)
            .message(msg)
            .success(false)
            .build();
    }

}
