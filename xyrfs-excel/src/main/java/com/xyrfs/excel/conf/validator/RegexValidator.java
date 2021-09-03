package com.xyrfs.excel.conf.validator;

/**
 * Created by gordian on 2016/1/5.
 */
public class RegexValidator extends Validator {

    private String regex;

    public RegexValidator() {
        defaultMsg = "格式不匹配";
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean validate(String input) {
        return input.matches(regex);
    }

}
