package com.old.silence.job.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Register;
import com.old.silence.job.server.common.handler.GetHttpRequestHandler;
import com.old.silence.job.server.common.register.ClientRegister;
import com.old.silence.job.server.common.register.RegisterContext;

import static com.old.silence.job.common.constant.SystemConstants.BEAT.PONG;

/**
 * 接收心跳请求
 *
 */
@Component
public class BeatHttpRequestHandler extends GetHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.BEAT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Beat check content:[{}]", content);
        Register register = SilenceSpringContext.getBean(ClientRegister.BEAN_NAME, Register.class);
        RegisterContext registerContext = new RegisterContext();
        registerContext.setGroupName(headers.get(HeadersEnum.GROUP_NAME.getKey()));
        registerContext.setHostPort(Integer.valueOf(headers.get(HeadersEnum.HOST_PORT.getKey())));
        registerContext.setHostIp(headers.get(HeadersEnum.HOST_IP.getKey()));
        registerContext.setHostId(headers.get(HeadersEnum.HOST_ID.getKey()));
        registerContext.setUri(HTTP_PATH.BEAT);
        registerContext.setNamespaceId(headers.get(HeadersEnum.NAMESPACE.getKey()));
        boolean result = register.register(registerContext);
        if (!result) {
            SilenceJobLog.LOCAL.warn("client register error. groupName:[{}]", headers.get(HeadersEnum.GROUP_NAME.getKey()));
        }
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        return new SilenceJobRpcResult(PONG, retryRequest.getReqId());
    }
}
