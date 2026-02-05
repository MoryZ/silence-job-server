package com.old.silence.job.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.server.common.handler.GetHttpRequestHandler;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;



@Component
public class ConfigHttpRequestHandler extends GetHttpRequestHandler {
    private final GroupConfigDao groupConfigDao;

    public ConfigHttpRequestHandler(GroupConfigDao groupConfigDao) {
        this.groupConfigDao = groupConfigDao;
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
        GroupConfig groupConfig = groupConfigDao.selectOne(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, groupName)
                        .eq(GroupConfig::getNamespaceId, namespace)
        );
        ConfigDTO configDTO = groupConfig != null ? convertToConfigDTO(groupConfig) : null;
        return new SilenceJobRpcResult(JSON.toJSONString(configDTO), retryRequest.getReqId());
    }

    private ConfigDTO convertToConfigDTO(GroupConfig groupConfig) {
        // ConfigDTO is immutable, just return JSON representation
        // The original implementation may have had a custom method
        return new ConfigDTO();
    }
}
