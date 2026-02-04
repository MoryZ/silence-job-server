package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.client.dto.request.DispatchJobResultRequest;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.job.task.support.ClientCallbackHandler;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackContext;
import com.old.silence.job.server.job.task.support.callback.ClientCallbackFactory;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.REPORT_JOB_DISPATCH_RESULT;


@Component
public class ReportDispatchResultPostHttpRequestHandler extends PostHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return REPORT_JOB_DISPATCH_RESULT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Client Callback Request. content:[{}]", content);

        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();

        DispatchJobResultRequest dispatchJobResultRequest = JSON.parseObject(JSON.toJSONString(args[0]), DispatchJobResultRequest.class);

        ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(dispatchJobResultRequest.getTaskType());

        ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(dispatchJobResultRequest);
        context.setNamespaceId(headers.getAsString(HeadersEnum.NAMESPACE.getKey()));
        clientCallback.callback(context);

        return new SilenceJobRpcResult(200, "Report Dispatch Result Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }
}
