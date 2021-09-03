package com.xyrfs.excel.conf.convertor;

import com.xyrfs.common.utils.DateTimeUtil;
import com.xyrfs.excel.upload.FsExcelException;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author zxh
 * @date 2019/8/10
 */
public class DateTimeConvertor extends BaseConvertor {

    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

    @Override
    public String doConvert(Object input) {
        return DateFormatUtils.format((Date) input, DATETIME);
    }

    @Override
    public Object doConvertToType(String input) {
        try {
            return DateTimeUtil.parseDate(input, new String[]{DATETIME});
        } catch (ParseException e) {
            throw new FsExcelException(e);
        }
    }
}
