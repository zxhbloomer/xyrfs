package com.xyrfs.excel.bean.importconfig.template.data;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * excel行模板类
 * @author zxh
 */
@AllArgsConstructor
@NoArgsConstructor
public class DataRow implements Serializable {

    private static final long serialVersionUID = -3512002550272910844L;

    /**
     * 列数据
     */
    @Getter
    @Setter
    @JSONField
    private List<DataCol> dataCols = new ArrayList<DataCol>();

    /**
     * 添加列名
     * @param names
     */
    public void addDataCol(String... names) {
        for (String name : names) {
            addDataCol(new DataCol(name));
        }
    }

    /**
     * 添加列名
     * @param dataCol
     */
    public void addDataCol(DataCol dataCol) {
        dataCol.setIndex(dataCols.size());
        dataCols.add(dataCol);
    }

    /**
     * 列数
     * @return
     */
    public int colSize() {
        return dataCols.size();
    }

}
