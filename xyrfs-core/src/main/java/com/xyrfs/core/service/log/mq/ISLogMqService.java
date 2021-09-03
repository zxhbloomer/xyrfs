package com.xyrfs.core.service.log.mq;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyrfs.bean.entity.log.mq.SLogMqEntity;
import com.xyrfs.bean.pojo.result.InsertResult;
import com.xyrfs.bean.pojo.result.UpdateResult;

import java.util.List;

/**
 * <p>
 * 消息队列 服务类
 * </p>
 *
 * @author zxh
 * @since 2019-08-23
 */
public interface ISLogMqService extends IService<SLogMqEntity> {

    /**
     * 获取所有数据
     */
    List<SLogMqEntity> selectAll() ;

    /**
     * 查询by id，返回结果
     *
     * @param id
     * @return
     */
    SLogMqEntity selectByid(Long id);

    /**
     * 插入一条记录（选择字段，策略插入）
     * @param entity 实体对象
     * @return
     */
    InsertResult<Integer> insert(SLogMqEntity entity);

    /**
     * 更新一条记录（选择字段，策略更新）
     * @param entity 实体对象
     * @return
     */
    UpdateResult<Integer> update(SLogMqEntity entity);

    /**
     * 通过key查询
     *
     */
    SLogMqEntity selectByKey(String key);
}
