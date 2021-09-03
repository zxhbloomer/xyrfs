package com.xyrfs.core.serviceimpl.log.sys;

import com.xyrfs.bean.entity.log.sys.SLogSysEntity;
import com.xyrfs.core.mapper.log.sys.SLogMapper;
import com.xyrfs.core.service.base.v1.BaseServiceImpl;
import com.xyrfs.core.service.log.sys.ISLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zxh
 * @since 2019-07-04
 */
@Service
public class SLogServiceImpl extends BaseServiceImpl<SLogMapper, SLogSysEntity> implements ISLogService {

}
