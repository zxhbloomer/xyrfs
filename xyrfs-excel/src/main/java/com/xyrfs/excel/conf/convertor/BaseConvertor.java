package com.xyrfs.excel.conf.convertor;

import com.xyrfs.common.utils.string.StringUtil;

/**
 *
 * @author zxh
 * @date 2019/8/10
 */
public abstract class BaseConvertor implements Convertor {
    @Override
    public String convert(Object input) {
        if (isEmptyObj(input)) {
            return "";
        }
        return doConvert(input);
    }

    protected abstract String doConvert(Object input);

    private boolean isEmptyObj(Object input) {
        return input == null || "".equals(input.toString());
    }

    @Override
    public Object convertToType(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        return doConvertToType(input);
    }

    protected abstract Object doConvertToType(String input);
}
