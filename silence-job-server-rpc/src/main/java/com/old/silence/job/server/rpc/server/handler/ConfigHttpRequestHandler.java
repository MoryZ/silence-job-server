package com.old.silence.job.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.server.common.handler.GetHttpRequestHandler;
import com.old.silence.job.server.domain.service.AccessTemplate;


@Component
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {
    private final AccessTemplate accessTemplate;

    public ConfigHttpRequestHandler(AccessTemplate accessTemplate) {
        this.accessTemplate = accessTemplate;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.SYNC_CONFIG.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespace);
        return new SilenceJobRpcResult(JSON.toJSONString(configDTO), retryRequest.getReqId());
    }
}
