package com.xyrfs.excel.conf.validator;

import com.xyrfs.excel.bean.importconfig.template.title.TitleRow;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zxh
 * @date 2016/1/21
 */
public class RowValidateResult implements Serializable {

    private static final long serialVersionUID = 6112948316876785169L;

    @Getter
    @Setter
    private int rowIndex;
    @Setter
    private String error;

    public String getErrors(List<TitleRow> titleRows) {
        String rtn = "";
        for (ColValidateResult colValidateResult : colValidateResults) {
            String rowTitle = titleRows.get(0).getCol(colValidateResult.getDataCol().getIndex()).getTitle();
            String error = colValidateResult.getErrorMsg();
            rtn = rtn + String.format("第%s列 %s ：%s ", colValidateResult.getDataCol().getIndex(), rowTitle, error);
        }

        return rtn;
    }

    private List<ColValidateResult> colValidateResults = new ArrayList<ColValidateResult>();

    public List<ColValidateResult> getColValidateResults() {
        return colValidateResults;
    }

    public void addColValidateResult(ColValidateResult colValidateResult) {
        colValidateResults.add(colValidateResult);
    }

}
