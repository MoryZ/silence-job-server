package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.cache.CacheConsumerGroup;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.handler.GetHttpRequestHandler;
import com.old.silence.job.server.common.register.ClientRegister;
import com.old.silence.job.server.domain.model.ServerNode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.GET_REG_NODES_AND_REFRESH;
import static com.old.silence.job.server.common.register.ClientRegister.DELAY_TIME;

/**
 * 获取服务端缓存的客户端节点 并刷新本地时间
 */
@Component
public class GetRegNodesPostHttpRequestHandler extends GetHttpRequestHandler {

    public static List<ServerNode> getAndRefreshCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(Instant.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(serverNode);
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }

    @Override
    public boolean supports(String path) {
        return GET_REG_NODES_AND_REFRESH.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Client Callback Request. content:[{}]", content);

        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);


        List<ServerNode> refreshCache = getAndRefreshCache();
        String json = null;
        if (CollectionUtils.isNotEmpty(refreshCache)) {
            json = JSON.toJSONString(refreshCache);
        }
        return new SilenceJobRpcResult(json, retryRequest.getReqId());
    }


}
