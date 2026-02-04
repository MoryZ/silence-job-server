package com.old.silence.job.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.JobArgsType;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 分片参数格式
 * 0=参数1;1=参数2;
 */
@Component
public class ShardingTaskGenerator extends AbstractJobTaskGenerator {
    private static final String TASK_NAME = "SHARDING_TASK";

    protected ShardingTaskGenerator(JobTaskDao jobTaskDao) {
        super(jobTaskDao);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.SHARDING;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollectionUtils.isEmpty(serverNodes)) {
            SilenceJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        String argsStr = context.getArgsStr();
        if (StrUtil.isBlank(argsStr)) {
            SilenceJobLog.LOCAL.error("切片参数为空. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        List<String> argsStrs;
        try {
            argsStrs = JSON.parseArray(argsStr, String.class);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("切片参数解析失败. jobId:[{}]", context.getJobId(), e);
            return Lists.newArrayList();
        }

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);
        List<JobTask> jobTasks = new ArrayList<>(argsStrs.size());
        for (int index = 0; index < argsStrs.size(); index++) {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
            JobArgsHolder jobArgsHolder = new JobArgsHolder();
            jobArgsHolder.setJobParams(argsStrs.get(index));
            jobTask.setArgsStr(JSON.toJSONString(jobArgsHolder));
            jobTask.setArgsType(JobArgsType.JSON);
            jobTask.setTaskStatus(JobTaskStatus.RUNNING);
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setParentId(BigInteger.ZERO);
            jobTask.setRetryCount(0);
            jobTask.setLeaf(true);
            jobTask.setCreatedDate(Instant.now());
            jobTask.setUpdatedDate(Instant.now());
            jobTask.setTaskName(TASK_NAME);
            jobTasks.add(jobTask);
        }

        batchSaveJobTasks(jobTasks);
        return jobTasks;
    }

}
