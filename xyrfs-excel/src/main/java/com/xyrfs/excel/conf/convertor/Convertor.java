package com.xyrfs.excel.conf.convertor;

/**
 *
 * @author zxh
 * @date 2019/8/10
 */
public interface Convertor {
    public String convert(Object input);

    public Object convertToType(String input);
}
