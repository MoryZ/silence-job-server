package com.old.silence.job.server.job.task.support;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.common.client.dto.request.DispatchJobRequest;
import com.old.silence.job.common.client.dto.request.DispatchJobResultRequest;
import com.old.silence.job.common.client.dto.request.MapTaskRequest;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.dto.JobLogTaskDTO;
import com.old.silence.job.common.dto.LogTaskDTO;
import com.old.silence.job.server.common.dto.JobAlarmInfo;
import com.old.silence.job.server.common.dto.JobLogMetaDTO;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.job.task.dto.*;
import com.old.silence.job.server.job.task.support.block.job.BlockStrategyContext;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackContext;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorContext;
import com.old.silence.job.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.old.silence.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.old.silence.job.server.job.task.support.result.job.JobExecutorResultContext;
import com.old.silence.job.server.job.task.support.stop.TaskStopJobContext;

import java.util.List;


@Mapper
public interface JobTaskConverter {

    JobTaskConverter INSTANCE = Mappers.getMapper(JobTaskConverter.class);

    @Mapping(source = "id", target = "jobId")
    JobTaskPrepareDTO toJobTaskPrepare(JobPartitionTaskDTO job);

    @Mapping(source = "id", target = "jobId")
    JobTaskPrepareDTO toJobTaskPrepare(Job job);

    @Mapping(source = "job.id", target = "jobId")
    @Mapping(source = "job.groupName", target = "groupName")
    JobTaskPrepareDTO toJobTaskPrepare(Job job, WorkflowExecutorContext context);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(JobTaskPrepareDTO jobTaskPrepareDTO);

    JobTaskBatchGeneratorContext toJobTaskGeneratorContext(BlockStrategyContext context);

    @Mapping(source = "id", target = "jobId")
    JobTaskGenerateContext toJobTaskInstanceGenerateContext(Job job);

    JobTaskGenerateContext toJobTaskInstanceGenerateContext(MapTaskRequest request);

    JobTask toJobTaskInstance(JobTaskGenerateContext context);

    BlockStrategyContext toBlockStrategyContext(JobTaskPrepareDTO prepareDTO);

    TaskStopJobContext toStopJobContext(BlockStrategyContext context);

    TaskStopJobContext toStopJobContext(JobExecutorResultContext context);

    @Mapping(source = "id", target = "jobId")
    TaskStopJobContext toStopJobContext(Job job);

    TaskStopJobContext toStopJobContext(JobTaskPrepareDTO context);

    JobLogMessage toJobLogMessage(JobLogDTO jobLogDTO);

    JobLogMessage toJobLogMessage(LogTaskDTO logTaskDTO);

    default JobLogMessage toJobLogMessage(JobLogTaskDTO jobLogTaskDTO) {
        return toJobLogMessage((LogTaskDTO) jobLogTaskDTO);
    }

    JobLogMetaDTO toJobLogDTO(BaseDTO baseDTO);

    ClientCallbackContext toClientCallbackContext(DispatchJobResultRequest request);

    ClientCallbackContext toClientCallbackContext(RealJobExecutorDTO request);

    @Mapping(source = "id", target = "jobId")
    ClientCallbackContext toClientCallbackContext(Job job);

    @Mapping(target = "taskType", expression = "java(realJobExecutorDTO.getTaskType().getValue())")
    @Mapping(target = "mrStage", expression = "java(toMrStage(realJobExecutorDTO))")
    @Mapping(target = "executorType", expression = "java(realJobExecutorDTO.getExecutorType().getValue())")
    DispatchJobRequest toDispatchJobRequest(RealJobExecutorDTO realJobExecutorDTO);

    default Byte toMrStage(RealJobExecutorDTO realJobExecutorDTO) {
        if (realJobExecutorDTO.getExecutorType() != null) {
            return realJobExecutorDTO.getExecutorType().getValue();
        }
        return null;
    }

    @Mapping(source = "jobTask.groupName", target = "groupName")
    @Mapping(source = "jobTask.jobId", target = "jobId")
    @Mapping(source = "jobTask.taskBatchId", target = "taskBatchId")
    @Mapping(source = "jobTask.id", target = "taskId")
    @Mapping(source = "jobTask.argsStr", target = "argsStr")
    @Mapping(source = "jobTask.argsType", target = "argsType")
    @Mapping(source = "jobTask.extAttrs", target = "extAttrs")
    @Mapping(source = "jobTask.taskName", target = "taskName")
    @Mapping(source = "jobTask.mrStage", target = "mrStage")
    @Mapping(source = "context.wfContext", target = "wfContext")
    @Mapping(source = "jobTask.namespaceId", target = "namespaceId")
    RealJobExecutorDTO toRealJobExecutorDTO(JobExecutorContext context, JobTask jobTask);

    @Mapping(source = "id", target = "jobId")
    JobExecutorContext toJobExecutorContext(Job job);

    JobExecutorResultDTO toJobExecutorResultDTO(ClientCallbackContext context);

    @Mapping(source = "id", target = "taskId")
    JobExecutorResultDTO toJobExecutorResultDTO(JobTask jobTask);

    JobExecutorResultDTO toJobExecutorResultDTO(RealJobExecutorDTO realJobExecutorDTO);

    RealStopTaskInstanceDTO toRealStopTaskInstanceDTO(TaskStopJobContext context);

    JobPartitionTaskDTO toJobPartitionTask(Job job);

    List<JobPartitionTaskDTO> toJobTaskBatchPartitionTasks(List<JobTaskBatch> jobTaskBatches);

    JobTaskBatch toJobTaskBatch(JobTaskBatchGeneratorContext context);

    CompleteJobBatchDTO toCompleteJobBatchDTO(JobExecutorResultDTO jobExecutorResultDTO);

    CompleteJobBatchDTO completeJobBatchDTO(JobTaskPrepareDTO jobTaskPrepareDTO);

    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    JobLogMessage toJobLogMessage(JobLogMessage jobLogMessage);

    ReduceTaskDTO toReduceTaskDTO(JobExecutorResultContext context);

    JobExecutorResultContext toJobExecutorResultContext(CompleteJobBatchDTO completeJobBatchDTO);

    List<JobAlarmInfo> toJobTaskFailAlarmEventDTO(List<JobTaskFailAlarmEventDTO> jobTaskFailAlarmEventDTOList);

    @Mapping(source = "jobTaskBatchId", target = "id")
    JobAlarmInfo toJobAlarmInfo(JobTaskFailAlarmEventDTO jobTaskFailAlarmEventDTO);
}
