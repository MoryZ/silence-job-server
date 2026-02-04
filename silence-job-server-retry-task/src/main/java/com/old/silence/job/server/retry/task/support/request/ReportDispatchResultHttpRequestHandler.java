package com.old.silence.job.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.client.dto.request.DispatchRetryResultRequest;
import com.old.silence.job.common.enums.RetryOperationReason;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.dto.RetryExecutorResultDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.REPORT_RETRY_DISPATCH_RESULT;

/**
 * 上报处理结果
 *
 */
@Component

public class ReportDispatchResultHttpRequestHandler extends PostHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return REPORT_RETRY_DISPATCH_RESULT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    @Transactional
    public SilenceJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();

        try {
            DispatchRetryResultRequest request = JSON.parseObject(JSON.toJSONString(args[0]), DispatchRetryResultRequest.class);
            RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(request);
            RetryTaskStatus statusEnum = RetryTaskStatus.getByStatus(request.getTaskStatus());
            Assert.notNull(statusEnum, () -> new SilenceJobServerException("task status code is invalid"));
            executorResultDTO.setIncrementRetryCount(true);
            if (RetryTaskStatus.FAIL.equals(statusEnum)) {
                executorResultDTO.setOperationReason(RetryOperationReason.RETRY_FAIL);
            } else if (RetryTaskStatus.STOP.equals(statusEnum)) {
                executorResultDTO.setOperationReason(RetryOperationReason.CLIENT_TRIGGER_RETRY_STOP);
            }

            ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
            actorRef.tell(executorResultDTO, actorRef);

            return new SilenceJobRpcResult(200, "Report dispatch result processed successfully", Boolean.TRUE, retryRequest.getReqId());
        } catch (Exception e) {
            return new SilenceJobRpcResult(200, e.getMessage(), Boolean.FALSE, retryRequest.getReqId());
        }
    }

}
