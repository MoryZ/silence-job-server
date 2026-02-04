package com.old.silence.job.server.common.alarm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.common.convert.AlarmInfoConverter;
import com.old.silence.job.server.common.dto.WorkflowAlarmInfo;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.vo.WorkflowBatchResponseDO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractWorkflowAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, WorkflowAlarmInfo> {

    private final WorkflowTaskBatchDao workflowTaskBatchDao;

    protected AbstractWorkflowAlarm(WorkflowTaskBatchDao workflowTaskBatchDao) {
        this.workflowTaskBatchDao = workflowTaskBatchDao;
    }

    @Override
    protected Map<BigInteger, List<WorkflowAlarmInfo>> convertAlarmDTO(List<WorkflowAlarmInfo> workflowAlarmInfoList, Set<Integer> notifyScene) {

        Map<BigInteger, List<WorkflowAlarmInfo>> workflowAlarmInfoMap = new HashMap<>();
        workflowAlarmInfoList.forEach(i -> notifyScene.add(Integer.valueOf(i.getNotifyScene().getValue())));

        Map<BigInteger, WorkflowAlarmInfo> workflowAlarmInfoGroupMap = StreamUtils.toIdentityMap(workflowAlarmInfoList, WorkflowAlarmInfo::getId);
        // 查询数据库
        List<WorkflowBatchResponseDO> workflowBatchResponseDOList = workflowTaskBatchDao.selectWorkflowBatchList(
                new QueryWrapper<WorkflowTaskBatch>()
                        .in("batch.id", workflowAlarmInfoList.stream().map(WorkflowAlarmInfo::getId).collect(Collectors.toSet()))
                        .eq("batch.deleted", 0));

        for (WorkflowBatchResponseDO workflowBatchResponseDO : workflowBatchResponseDOList) {
            Set<BigInteger> workflowNotifyIds = StringUtils.isBlank(workflowBatchResponseDO.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JSON.parseArray(workflowBatchResponseDO.getNotifyIds(), BigInteger.class));
            for (BigInteger workflowNotifyId : workflowNotifyIds) {

                WorkflowAlarmInfo workflowAlarmInfo = AlarmInfoConverter.INSTANCE.toWorkflowAlarmInfo(workflowBatchResponseDO);
                WorkflowAlarmInfo alarmInfo = workflowAlarmInfoGroupMap.get(workflowAlarmInfo.getId());
                workflowAlarmInfo.setReason(alarmInfo.getReason());
                workflowAlarmInfo.setNotifyScene(alarmInfo.getNotifyScene());

                List<WorkflowAlarmInfo> workflowAlarmInfos = workflowAlarmInfoMap.getOrDefault(workflowNotifyId, new ArrayList<>());
                workflowAlarmInfos.add(workflowAlarmInfo);
                workflowAlarmInfoMap.put(workflowNotifyId, workflowAlarmInfos);
            }
        }

        return workflowAlarmInfoMap;
    }
}
