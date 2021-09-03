package com.xyrfs.excel.conf.validator;

/**
 * Created by gordian on 2016/1/5.
 */
public class MobileValidator extends RegexValidator {

    public MobileValidator() {
        setRegex("^1[3-8][0-9]\\d{8}$");
        defaultMsg = "不是正确的手机号";
    }

}
