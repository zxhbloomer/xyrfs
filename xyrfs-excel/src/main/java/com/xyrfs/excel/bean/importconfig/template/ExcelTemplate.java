package com.xyrfs.excel.bean.importconfig.template;

import com.alibaba.fastjson.annotation.JSONField;
import com.xyrfs.excel.bean.importconfig.template.data.DataCol;
import com.xyrfs.excel.bean.importconfig.template.data.DataRow;
import com.xyrfs.excel.bean.importconfig.template.title.TitleRow;
import com.xyrfs.excel.conf.validator.Validator;
import com.xyrfs.excel.conf.validator.ValidatorUtil;
import com.xyrfs.excel.upload.FsExcelException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * excel模板配置类
 * 
 * @author zxh
 */
public class ExcelTemplate implements Serializable {

    private static final long serialVersionUID = 4795280514712199351L;

    @Getter
    @Setter
    private List<TitleRow> titleRows = new ArrayList<TitleRow>();


    @JSONField(serialize = false)
    @Getter
    private boolean isInit = false;

    @Setter
    @Getter
    @JSONField
    private DataRow dataRows;

    @Setter
    @Getter
    @JSONField
    private String name;

    /**
     * 往数组中，添加titleRow
     * 
     * @param titleRow
     */
    public void addTitleRow(TitleRow titleRow) {
        titleRows.add(titleRow);
    }

    /**
     * 获取数据行长度，条数
     * 
     * @return
     */
    @JSONField(serialize = false)
    public int getDataRowIndex() {
        return titleRows.size();
    }

    /**
     * 获取列数
     * 
     * @return
     */
    @JSONField(serialize = false)
    public int getColSize() {
        return titleRows.get(0).colSize();
    }

    /**
     * 获取列对象
     * 
     * @return
     */
    @JSONField(serialize = false)
    public List<DataCol> getDataCols() {
        return dataRows.getDataCols();
    }

    /**
     * 根据json字符串，反向生成的Bean，进行添加Validator对象验证的处理
     */
    public void initValidator() {
        // dataCols:数据列，存在多条情况
        DataRow dr = this.dataRows;
        dr.getDataCols().forEach(
            bean -> {
                // 添加验证validator，存在多条情况
                if(bean.getListValiDator() == null){
                    return;
                }
                bean.getListValiDator().forEach(
                    v -> {
                        // 获取validator 验证类
                        Validator validator = ValidatorUtil.getValidator(v.getValidtorName());

                        if(v.getParam() != null){
                            // 获取validator 验证类，对应的参数，存在多个参数情况
                            v.getParam().forEach(
                                p -> {
                                    // 设置validator 验证类的参数
                                    try {
//                                        MethodUtils.invokeMethod(validator, p.getName(), p.getValue());
                                        BeanUtils.setProperty(validator, p.getName(), p.getValue());
                                    } catch (IllegalAccessException e) {
                                        throw new FsExcelException(e);
                                    } catch (InvocationTargetException e) {
                                        throw new FsExcelException(e);
                                    }
                                }
                            );
                        }
                        // 添加验证，validator
                        try {
                            MethodUtils.invokeMethod(bean, "addValidator", validator);
                        } catch (NoSuchMethodException e) {
                            throw new FsExcelException(e);
                        } catch (IllegalAccessException e) {
                            throw new FsExcelException(e);
                        } catch (InvocationTargetException e) {
                            throw new FsExcelException(e);
                        }
                    }

                );
                // converter为动态自动判断，此处无需考虑
            }
        );
        isInit = true;
    }

}
