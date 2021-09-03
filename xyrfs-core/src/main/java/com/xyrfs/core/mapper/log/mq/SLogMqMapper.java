package com.xyrfs.core.mapper.log.mq;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyrfs.bean.entity.log.mq.SLogMqEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: SLogMqMapper
 * @Description: 消息队列日志
 * @Author: zxh
 * @date: 2019/10/18
 * @Version: 1.0
 */
@Repository
public interface SLogMqMapper extends BaseMapper<SLogMqEntity> {

    String common_columns = "               "
        + "  t.id,                          "
        + "  t.`code`,                      "
        + "  t.`name`,                      "
        + "  t.exchange,                    "
        + "  t.routing_key,                 "
        + "  t.mq_data,                     "
        + "  t.construct_id,                "
        + "  t.producer_status,             "
        + "  t.consumer_status,             "
        + "  t.c_id,                        "
        + "  t.c_time,                      "
        + "  t.u_id,                        "
        + "  t.u_time,                      "
        + "  t.dbversion                    "
        + "                                 ";


    /**
     * 按条件获取所有数据，没有分页
     * @return
     */
    @Select("                  "
        + "  select            "
        + common_columns
        + "   from s_log_mq  t "
        + "  where true        "
        + "                    ")
    List<SLogMqEntity> selectAll();

    /**
     * 按条件获取所有数据，没有分页
     * @param id
     * @return
     */
    @Select("    "
        + "  select            "
        + common_columns
        + "   from s_log_mq  t "
        + "  where true        "
        + "    and t.id =  #{p1}"
        + "      ")
    SLogMqEntity selectId(@Param("p1") Long id);

    /**
     * 按条件获取所有数据，没有分页
     * @param config_key
     * @return
     */
    @Select("    "
        + "  select            "
        + common_columns
        + "   from s_log_mq  t "
        + "  where true        "
        + "    and t.construct_id =  #{p1}"
        + "      ")
    SLogMqEntity selectByKey(@Param("p1") String key);
}
