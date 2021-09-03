package com.xyrfs.bean.vo;

import com.xyrfs.bean.config.base.v1.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 *
 * @description: 用户登录数据传输对象
 */
@Data
@Builder
@AllArgsConstructor
@ApiModel(value = "登录授权服务器类", description = "作为登录授权服务器数据bean")
@EqualsAndHashCode(callSuper=false)
public class TokenLoginVo extends BaseVo implements Serializable {
    private static final long serialVersionUID = -2008867352253446153L;

    @ApiModelProperty(value = "用户名")
    @NotNull(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码")
    @NotNull(message = "密码不能为空")
    private String password;
}
