package com.old.silence.job.server.domain.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.api.assembler.JobTaskResponseVOMapper;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.dto.JobTaskQuery;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.vo.JobTaskResponseVO;
import com.old.silence.core.util.CollectionUtils;


@Service
public class JobTaskService {
    private final JobTaskDao jobTaskDao;
    private final JobTaskResponseVOMapper jobTaskResponseVOMapper;

    public JobTaskService(JobTaskDao jobTaskDao, JobTaskResponseVOMapper jobTaskResponseVOMapper) {
        this.jobTaskDao = jobTaskDao;
        this.jobTaskResponseVOMapper = jobTaskResponseVOMapper;
    }

    public IPage<JobTaskResponseVO> getJobTaskPage(Page<JobTask> pageDTO, JobTaskQuery queryVO) {

        Page<JobTask> selectPage = jobTaskDao.selectPage(pageDTO,
                new LambdaQueryWrapper<JobTask>()
                        .eq(Objects.nonNull(queryVO.getJobId()), JobTask::getJobId, queryVO.getJobId())
                        .eq(Objects.nonNull(queryVO.getTaskBatchId()), JobTask::getTaskBatchId, queryVO.getTaskBatchId())
                        .eq(Objects.nonNull(queryVO.getTaskStatus()), JobTask::getTaskStatus, queryVO.getTaskStatus())
                        .eq(JobTask::getParentId, 0)
                        .orderByAsc(JobTask::getId));


        var jobTaskResponseVOS = this.convertJobTaskList(selectPage.getRecords());
        var bigIntegerJobTaskResponseVOMap = CollectionUtils.transformToMap(jobTaskResponseVOS, JobTaskResponseVO::getId, Function.identity());
        return selectPage.convert(jobTask -> bigIntegerJobTaskResponseVOMap.get(jobTask.getId()));
    }

    public List<JobTaskResponseVO> getTreeJobTask(JobTaskQuery queryVO) {
        List<JobTask> taskList = jobTaskDao.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .eq(Objects.nonNull(queryVO.getParentId()), JobTask::getParentId, queryVO.getParentId())
                        .eq(Objects.nonNull(queryVO.getJobId()), JobTask::getJobId, queryVO.getJobId())
                        .eq(Objects.nonNull(queryVO.getTaskBatchId()), JobTask::getTaskBatchId, queryVO.getTaskBatchId())
                        .orderByAsc(JobTask::getJobId));

        return convertJobTaskList(taskList);
    }

    private List<JobTaskResponseVO> convertJobTaskList(List<JobTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return List.of();
        }

        List<JobTaskResponseVO> jobTaskResponseVOs = CollectionUtils.transformToList(tasks, jobTaskResponseVOMapper::convert);

        Set<BigInteger> parentIds = StreamUtils.toSet(jobTaskResponseVOs, JobTaskResponseVO::getId);
        List<JobTask> jobTasks = jobTaskDao.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getParentId).in(JobTask::getParentId, parentIds));
        Set<BigInteger> jobTaskParentIds = StreamUtils.toSet(jobTasks, JobTask::getParentId);
        jobTaskResponseVOs.forEach(jobTask -> jobTask.setChildNode(!jobTaskParentIds.contains(jobTask.getId())));

        return jobTaskResponseVOs;
    }

}
