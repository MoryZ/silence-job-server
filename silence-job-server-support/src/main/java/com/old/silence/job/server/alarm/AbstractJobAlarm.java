package com.old.silence.job.server.common.alarm;

import cn.hutool.core.util.StrUtil;

import org.springframework.context.ApplicationEvent;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.common.convert.AlarmInfoConverter;
import com.old.silence.job.server.common.dto.JobAlarmInfo;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.vo.JobBatchResponseDO;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo> {

    private final JobTaskBatchDao jobTaskBatchDao;

    protected AbstractJobAlarm(JobTaskBatchDao jobTaskBatchDao) {
        this.jobTaskBatchDao = jobTaskBatchDao;
    }

    @Override
    protected Map<BigInteger, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> jobAlarmInfoList, Set<Integer> notifyScene) {

        Map<BigInteger, List<JobAlarmInfo>> jobAlarmInfoMap = new HashMap<>();
        jobAlarmInfoList.forEach(i -> notifyScene.add(Integer.valueOf(i.getNotifyScene().getValue())));

        Map<BigInteger, JobAlarmInfo> jobAlarmInfoGroupMap = StreamUtils.toIdentityMap(jobAlarmInfoList, JobAlarmInfo::getId);
        // 查询数据库
        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .in("batch.id", StreamUtils.toSet(jobAlarmInfoList, JobAlarmInfo::getId));

        List<JobBatchResponseDO> jobBatchResponseDOList = jobTaskBatchDao.selectJobBatchListByIds(wrapper);
        for (JobBatchResponseDO jobBatchResponseDO : jobBatchResponseDOList) {
            Set<BigInteger> jobNotifyIds = StrUtil.isBlank(jobBatchResponseDO.getNotifyIds()) ? new HashSet<>() :
                    new HashSet<>(JSON.parseArray(jobBatchResponseDO.getNotifyIds(), BigInteger.class));
            for (BigInteger jobNotifyId : jobNotifyIds) {

                JobAlarmInfo jobAlarmInfo = AlarmInfoConverter.INSTANCE.toJobAlarmInfo(jobBatchResponseDO);
                JobAlarmInfo alarmInfo = jobAlarmInfoGroupMap.get(jobBatchResponseDO.getId());
                jobAlarmInfo.setReason(alarmInfo.getReason());
                jobAlarmInfo.setNotifyScene(alarmInfo.getNotifyScene());

                List<JobAlarmInfo> jobAlarmInfos = jobAlarmInfoMap.getOrDefault(jobNotifyId, new ArrayList<>());
                jobAlarmInfos.add(jobAlarmInfo);
                jobAlarmInfoMap.put(jobNotifyId, jobAlarmInfos);
            }
        }

        return jobAlarmInfoMap;
    }
}
