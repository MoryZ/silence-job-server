package com.old.silence.job.server.domain.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.old.silence.job.log.constant.LogFieldConstants;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.dto.JobLogQuery;
import com.old.silence.job.server.infrastructure.persistence.dao.JobLogMessageDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.vo.JobLogResponseVO;
import com.old.silence.core.util.CollectionUtils;


@Service
public class JobLogService {
    private final JobLogMessageDao jobLogMessageDao;
    private final JobTaskBatchDao jobTaskBatchDao;

    public JobLogService(JobLogMessageDao jobLogMessageDao, JobTaskBatchDao jobTaskBatchDao) {
        this.jobLogMessageDao = jobLogMessageDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
    }

    public JobLogResponseVO getJobLogPage(JobLogQuery queryVO) {

        PageDTO<JobLogMessage> pageDTO = new PageDTO<>(1, 10);

        PageDTO<JobLogMessage> selectPage = jobLogMessageDao.selectPage(pageDTO,
                new LambdaQueryWrapper<JobLogMessage>()
                        .select(JobLogMessage::getId, JobLogMessage::getLogNum)
                        .ge(JobLogMessage::getId, queryVO.getStartId())
                        .ge(JobLogMessage::getTaskBatchId, queryVO.getTaskBatchId())
                        .ge(JobLogMessage::getJobId, queryVO.getJobId())
                        .eq(JobLogMessage::getTaskId, queryVO.getTaskId())
                        .orderByAsc(JobLogMessage::getId).orderByAsc(JobLogMessage::getRealTime));
        List<JobLogMessage> records = selectPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {

            JobTaskBatch jobTaskBatch = jobTaskBatchDao.selectOne(
                    new LambdaQueryWrapper<JobTaskBatch>()
                            .eq(JobTaskBatch::getId, queryVO.getTaskBatchId())
            );

            JobLogResponseVO jobLogResponseVO = new JobLogResponseVO();

            if (Objects.isNull(jobTaskBatch)
                    || jobTaskBatch.getUpdatedDate().plusSeconds(15).isBefore(Instant.now())
            ) {
                jobLogResponseVO.setFinished(Boolean.TRUE);
            }

            jobLogResponseVO.setNextStartId(queryVO.getStartId());
            jobLogResponseVO.setFromIndex(0);
            return jobLogResponseVO;
        }

        Integer fromIndex = Optional.ofNullable(queryVO.getFromIndex()).orElse(0);
        JobLogMessage firstRecord = records.get(0);
        List<BigInteger> ids = Lists.newArrayList(firstRecord.getId());
        int total = firstRecord.getLogNum() - fromIndex;
        for (int i = 1; i < records.size(); i++) {
            JobLogMessage record = records.get(i);
            if (total + record.getLogNum() > 10) {
                break;
            }

            total += record.getLogNum();
            ids.add(record.getId());
        }

        BigInteger nextStartId = BigInteger.ZERO;
        List<Map<String, String>> messages = Lists.newArrayList();
        List<JobLogMessage> jobLogMessages = jobLogMessageDao.selectList(
                new LambdaQueryWrapper<JobLogMessage>()
                        .in(JobLogMessage::getId, ids)
                        .orderByAsc(JobLogMessage::getId)
                        .orderByAsc(JobLogMessage::getRealTime)
        );

        for (JobLogMessage jobLogMessage : jobLogMessages) {

            List<Map<String, String>> originalList = JSON.parseObject(jobLogMessage.getMessage(), List.class);
            int size = originalList.size() - fromIndex;
            List<Map<String, String>> pageList = originalList.stream().skip(fromIndex).limit(10)
                    .collect(Collectors.toList());

            if (messages.size() + size >= 10) {
                messages.addAll(pageList);
                nextStartId = jobLogMessage.getId();
                fromIndex = Math.min(fromIndex + 10, originalList.size() - 1) + 1;
                break;
            }

            messages.addAll(pageList);
            nextStartId = jobLogMessage.getId().add(BigInteger.ONE);
            fromIndex = 0;
        }

        messages = messages.stream()
                .sorted(Comparator.comparingLong(o -> Long.parseLong(o.get(LogFieldConstants.TIME_STAMP))))
                .collect(Collectors.toList());

        JobLogResponseVO jobLogResponseVO = new JobLogResponseVO();
        jobLogResponseVO.setMessage(messages);
        jobLogResponseVO.setNextStartId(nextStartId);
        jobLogResponseVO.setFromIndex(fromIndex);
        return jobLogResponseVO;
    }
}
