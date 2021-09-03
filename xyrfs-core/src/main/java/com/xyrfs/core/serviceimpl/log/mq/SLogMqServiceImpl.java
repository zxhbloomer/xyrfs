package com.xyrfs.core.serviceimpl.log.mq;

import com.xyrfs.bean.entity.log.mq.SLogMqEntity;
import com.xyrfs.bean.pojo.result.InsertResult;
import com.xyrfs.bean.pojo.result.UpdateResult;
import com.xyrfs.bean.result.utils.v1.InsertResultUtil;
import com.xyrfs.bean.result.utils.v1.UpdateResultUtil;
import com.xyrfs.core.mapper.log.mq.SLogMqMapper;
import com.xyrfs.core.service.base.v1.BaseServiceImpl;
import com.xyrfs.core.service.log.mq.ISLogMqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jobob
 * @since 2019-07-04
 */
@Service
public class SLogMqServiceImpl extends BaseServiceImpl<SLogMqMapper, SLogMqEntity> implements ISLogMqService {

    @Autowired
    private SLogMqMapper mapper;

    /**
     * 获取列表，查询所有数据
     *
     * @return
     */
    @Override
    public List<SLogMqEntity> selectAll() {
        // 查询 数据
        List<SLogMqEntity> list = mapper.selectAll();
        return list;
    }

    /**
     * 查询by id，返回结果
     *
     * @param id
     * @return
     */
    @Override
    public SLogMqEntity selectByid(Long id) {
        // 查询 数据
        return mapper.selectId(id);
    }

    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param entity 实体对象
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public InsertResult<Integer> insert(SLogMqEntity entity) {
        // 插入逻辑保存
        return InsertResultUtil.OK(mapper.insert(entity));
    }

    /**
     * 更新一条记录（选择字段，策略更新）
     *
     * @param entity 实体对象
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UpdateResult<Integer> update(SLogMqEntity entity) {
        // 更新逻辑保存
        entity.setC_id(null);
        entity.setC_time(null);
        return UpdateResultUtil.OK(mapper.updateById(entity));
    }

    /**
     * 获取列表，查询所有数据
     *
     * @param key
     * @return
     */
    @Override
    public SLogMqEntity selectByKey(String key) {
        // 查询 数据
        SLogMqEntity list = mapper.selectByKey(key);
        return list;
    }
}
