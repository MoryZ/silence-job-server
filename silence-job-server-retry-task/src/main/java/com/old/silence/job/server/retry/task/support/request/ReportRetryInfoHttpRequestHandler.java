package com.old.silence.job.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.github.rholder.retry.*;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.enums.TaskGeneratorSceneEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.retry.task.service.TaskContextConverter;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskContext;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.BATCH_REPORT;

/**
 * 处理上报数据
 *
 */
@Component
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {
    private final List<TaskGenerator> taskGenerators;

    public ReportRetryInfoHttpRequestHandler(List<TaskGenerator> taskGenerators) {
        this.taskGenerators = taskGenerators;
    }

    @Override
    public boolean supports(String path) {
        return BATCH_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    @Transactional
    public SilenceJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Batch Report Retry Data. content:[{}]", content);

        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();

        try {

            TaskGenerator taskGenerator = taskGenerators.stream()
                    .filter(t -> t.supports(TaskGeneratorSceneEnum.CLIENT_REPORT.getScene()))
                    .findFirst().orElseThrow(() -> new SilenceJobServerException("没有匹配的任务生成器"));

            Assert.notEmpty(args, () -> new SilenceJobServerException("上报的数据不能为空. reqId:[{}]", retryRequest.getReqId()));
            List<RetryTaskDTO> retryTaskList = JSON.parseArray(JSON.toJSONString(args[0]), RetryTaskDTO.class);

            SilenceJobLog.LOCAL.info("begin handler report data. <|>{}<|>", JSON.toJSONString(retryTaskList));

            Set<String> set = StreamUtils.toSet(retryTaskList, RetryTaskDTO::getGroupName);
            Assert.isTrue(set.size() <= 1, () -> new SilenceJobServerException("批量上报数据,同一批次只能是相同的组. reqId:[{}]", retryRequest.getReqId()));

            Map<String, List<RetryTaskDTO>> map = StreamUtils.groupByKey(retryTaskList, RetryTaskDTO::getSceneName);

            Retryer<Object> retryer = RetryerBuilder.newBuilder()
                    .retryIfException(throwable -> {
                        // 若是数据库异常则重试
                        return throwable instanceof DuplicateKeyException
                                || throwable instanceof TransactionSystemException
                                || throwable instanceof ConcurrencyFailureException
                                || throwable instanceof IOException;
                    })
                    .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                    .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                    .withRetryListener(new RetryListener() {
                        @Override
                        public <V> void onRetry(Attempt<V> attempt) {
                            if (attempt.hasException()) {
                                SilenceJobLog.LOCAL.error("数据上报发生异常执行重试. reqId:[{}] count:[{}]",
                                        retryRequest.getReqId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                            }
                        }
                    })
                    .build();

            String namespaceId = headers.getAsString(HeadersEnum.NAMESPACE.getKey());

            retryer.call(() -> {
                map.forEach(((sceneName, retryTaskDTOS) -> {
                    TaskContext taskContext = new TaskContext();
                    taskContext.setSceneName(sceneName);
                    taskContext.setNamespaceId(namespaceId);
                    taskContext.setGroupName(set.stream().findFirst().get());
                    taskContext.setTaskInfos(retryTaskDTOS.stream().map(TaskContextConverter.INSTANCE::toTaskContextInfo).collect(Collectors.toList()));

                    // 生成任务
                    taskGenerator.taskGenerator(taskContext);
                }));

                return null;
            });

            return new SilenceJobRpcResult(200, "Batch Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
        } catch (Exception e) {

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            SilenceJobLog.LOCAL.error("Batch Report Retry Data Error. <|>{}<|>", args[0], throwable);
            return new SilenceJobRpcResult(200, throwable.getMessage(), Boolean.FALSE, retryRequest.getReqId());
        }
    }

}
