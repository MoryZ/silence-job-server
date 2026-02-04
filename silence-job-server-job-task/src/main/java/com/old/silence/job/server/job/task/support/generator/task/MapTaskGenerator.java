package com.old.silence.job.server.job.task.support.generator.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.server.common.handler.ClientNodeAllocateHandler;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskDao;
import com.old.silence.job.server.job.task.dto.MapReduceArgsStrDTO;

import java.util.List;


@Component
public class MapTaskGenerator extends MapReduceTaskGenerator {

    public MapTaskGenerator(JobTaskDao jobTaskDao,
                            TransactionTemplate transactionTemplate,
                            ClientNodeAllocateHandler clientNodeAllocateHandler) {
        super(jobTaskDao, transactionTemplate, clientNodeAllocateHandler);
    }

    @Override
    public JobTaskType getTaskInstanceType() {
        return JobTaskType.MAP;
    }

    @Override
    protected List<JobTask> doGenerate(final JobTaskGenerateContext context) {
        return super.doGenerate(context);
    }

    @Override
    protected MapReduceArgsStrDTO getJobParams(JobTaskGenerateContext context) {
        // 这里复用map reduce的参数能力
        MapReduceArgsStrDTO mapReduceArgsStrDTO = new MapReduceArgsStrDTO();
        mapReduceArgsStrDTO.setArgsStr(context.getArgsStr());
        return mapReduceArgsStrDTO;
    }
}
