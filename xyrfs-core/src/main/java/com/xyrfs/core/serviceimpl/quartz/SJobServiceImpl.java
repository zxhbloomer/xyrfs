package com.xyrfs.core.serviceimpl.quartz;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyrfs.bean.entity.quartz.SJobEntity;
import com.xyrfs.bean.pojo.result.InsertResult;
import com.xyrfs.bean.pojo.result.UpdateResult;
import com.xyrfs.bean.result.utils.v1.InsertResultUtil;
import com.xyrfs.bean.result.utils.v1.UpdateResultUtil;
import com.xyrfs.bean.vo.quartz.SJobVo;
import com.xyrfs.common.constant.ScheduleConstants;
import com.xyrfs.common.exception.job.TaskException;
import com.xyrfs.common.utils.quartz.CronUtil;
import com.xyrfs.common.utils.string.convert.Convert;
import com.xyrfs.core.mapper.quartz.SJobMapper;
import com.xyrfs.core.service.base.v1.BaseServiceImpl;
import com.xyrfs.core.service.quartz.ISJobService;
import com.xyrfs.core.utils.mybatis.PageUtil;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author jobob
 * @since 2019-07-04
 */
@Service public class SJobServiceImpl extends BaseServiceImpl<SJobMapper, SJobEntity> implements ISJobService {

    @Autowired
    private SJobMapper jobMapper;

    /**
     * 项目启动时，初始化定时器
     * 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        List<SJobEntity> jobList = jobMapper.selectJobAll();
        for (SJobEntity job : jobList) {
            updateSchedulerJob(job, job.getJob_group_code());
        }
    }

    /**
     * 获取quartz调度器的计划任务列表
     *
     * @param searchCondition 调度信息
     * @return
     */
    @Override
    public IPage<SJobEntity> selectJobList(SJobVo searchCondition) {
        // 分页条件
        Page<SJobEntity> pageCondition =
            new Page(searchCondition.getPageCondition().getCurrent(), searchCondition.getPageCondition().getSize());
        // 通过page进行排序
        PageUtil.setSort(pageCondition, searchCondition.getPageCondition().getSort());
        return jobMapper.selectJobList(pageCondition, searchCondition);
    }

    /**
     * 通过调度任务ID查询调度信息
     *
     * @param jobId 调度任务ID
     * @return 调度任务对象信息
     */
    @Override
    public SJobEntity selectJobById(Long jobId) {
        return jobMapper.selectJobById(jobId);
    }

    /**
     * 暂停任务
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseJob(SJobEntity job) {
        job.setIs_effected(ScheduleConstants.Status.PAUSE.getValue());
        job.setC_id(null);
        job.setC_time(null);
        int rows = jobMapper.updateById(job);
        return rows;
    }

    /**
     * 恢复任务
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeJob(SJobEntity job) {
        job.setIs_effected(ScheduleConstants.Status.NORMAL.getValue());

        job.setC_id(null);
        job.setC_time(null);
        int rows = jobMapper.updateById(job);
        return rows;
    }

    /**
     * 删除任务后，所对应的trigger也将被删除
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteJob(SJobEntity job) {
        job.setIs_effected(ScheduleConstants.Status.PAUSE.getValue());
        job.setIs_del(true);
        job.setC_id(null);
        job.setC_time(null);
        int rows = jobMapper.updateById(job);
        return rows;
    }

    /**
     * 批量删除调度信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(String ids) {
        Long[] jobIds = Convert.toLongArray(ids);
        for (Long jobId : jobIds) {
            SJobEntity job = jobMapper.selectJobById(jobId);
            deleteJob(job);
        }
    }

    /**
     * 任务调度状态修改
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(SJobEntity job) throws SchedulerException {
        int rows = 0;
        boolean status = job.getIs_effected();
        if (ScheduleConstants.Status.NORMAL.getValue() == status) {
            rows = resumeJob(job);
        } else if (ScheduleConstants.Status.PAUSE.getValue() == status) {
            rows = pauseJob(job);
        }
        return rows;
    }

    /**
     * 立即运行任务
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(SJobEntity job) throws SchedulerException {
        Long jobId = job.getId();
        String jobGroup = job.getJob_group_code();
        SJobEntity properties = selectJobById(job.getId());
        // 参数
//        JobDataMap dataMap = new JobDataMap();
//        dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
//        scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup), dataMap);
    }

    /**
     * 新增任务
     *
     * @param job 调度信息 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertJob(SJobEntity job) {
        job.setIs_effected(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.insert(job);
        return rows;
    }

    /**
     * 更新任务的时间表达式
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateJob(SJobEntity job) throws SchedulerException, TaskException {
        SJobEntity properties = selectJobById(job.getId());

        job.setC_id(null);
        job.setC_time(null);
        int rows = jobMapper.updateById(job);
        if (rows > 0) {
            updateSchedulerJob(job, properties.getJob_group_code());
        }
        return rows;
    }

    /**
     * 更新任务
     *
     * @param job      任务对象
     * @param jobGroup 任务组名
     */
    public void updateSchedulerJob(SJobEntity job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getId();
        // 判断是否存在
//        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
//        if (scheduler.checkExists(jobKey)) {
//            // 防止创建时存在数据问题 先移除，然后在执行创建操作
//            scheduler.deleteJob(jobKey);
//        }
//        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    /**
     * 校验cron表达式是否有效
     *
     * @param cronExpression 表达式
     * @return 结果
     */
    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtil.isValid(cronExpression);
    }

    /**
     * 查询调度任务
     *
     * @param serialId 编号
     * @param serialType 类型
     * @return 调度任务对象信息
     */
    @Override
    public SJobEntity selectJobBySerialId(Long serialId, String serialType) {
        return jobMapper.selectJobBySerialId(serialId, serialType);
    }

    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param entity 实体对象
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public InsertResult<Integer> insert(SJobEntity entity) {
        // 插入逻辑保存
        return InsertResultUtil.OK(jobMapper.insert(entity));
    }

    /**
     * 更新一条记录（选择字段，策略更新）
     *
     * @param entity 实体对象
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UpdateResult<Integer> update(SJobEntity entity) {
        // 更新逻辑保存
        entity.setC_id(null);
        entity.setC_time(null);
        return UpdateResultUtil.OK(jobMapper.updateById(entity));
    }
}
