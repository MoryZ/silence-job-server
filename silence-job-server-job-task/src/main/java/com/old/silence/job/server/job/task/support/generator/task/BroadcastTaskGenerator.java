package com.old.silence.job.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.model.JobArgsHolder;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.util.ClientInfoUtils;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.support.JobTaskConverter;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class BroadcastTaskGenerator extends AbstractJobTaskGenerator {
    private static final String TASK_NAME = "BROADCAST_TASK";

    protected BroadcastTaskGenerator(JobTaskDao jobTaskDao) {
        super(jobTaskDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.BROADCAST;
    }

    @Override
    @Transactional
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollectionUtils.isEmpty(serverNodes)) {
            SilenceJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        Set<String> clientInfoSet = new HashSet<>(serverNodes.size());
        List<JobTask> jobTasks = new ArrayList<>(serverNodes.size());
        for (RegisterNodeInfo serverNode : serverNodes) {
            // 若存在相同的IP信息则去重
            String address = serverNode.address();
            if (clientInfoSet.contains(address)) {
                continue;
            }

            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(serverNode));
            JobArgsHolder jobArgsHolder = new JobArgsHolder();
            jobArgsHolder.setJobParams(context.getArgsStr());
            jobTask.setArgsStr(JSON.toJSONString(jobArgsHolder));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setTaskStatus(JobTaskStatus.RUNNING);
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setParentId(BigInteger.ZERO);
            jobTask.setLeaf(true);
            jobTask.setRetryCount(0);
            jobTask.setTaskName(TASK_NAME);
            jobTask.setCreatedDate(Instant.now());
            jobTask.setUpdatedDate(Instant.now());
            clientInfoSet.add(address);
            jobTasks.add(jobTask);
        }

        batchSaveJobTasks(jobTasks);

        return jobTasks;
    }

}
