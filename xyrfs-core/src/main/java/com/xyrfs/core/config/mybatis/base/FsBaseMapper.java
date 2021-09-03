package com.xyrfs.core.config.mybatis.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * 自定义basemapper
 * @param <T>
 */
public interface FsBaseMapper<T> extends BaseMapper<T> {

    /**
     * 根据 ID 修改，fs自定义
     *
     * @param entity 实体对象
     */
    int fsUpdateById(@Param(Constants.ENTITY) T entity);
}
