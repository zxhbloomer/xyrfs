package com.xyrfs.bean.vo.common.condition;

import com.xyrfs.bean.config.base.v1.BaseVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ApiModel(value = "分页条件bean", description = "分页条件bean")
@EqualsAndHashCode(callSuper=false)
public class PageCondition extends BaseVo implements Serializable {

    private static final long serialVersionUID = 7808161515093912080L;

    /** 当前页 */
    private long current;
    /**  每页显示条数*/
    private long size;
    /**  排序*/
    private String sort;
}
