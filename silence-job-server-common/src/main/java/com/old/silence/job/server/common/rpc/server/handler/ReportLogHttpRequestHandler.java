package com.old.silence.job.server.common.rpc.server.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.server.dto.JobLogTaskDTO;
import com.old.silence.job.common.server.dto.RetryLogTaskDTO;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.common.pekko.ActorGenerator;

import java.util.ArrayList;
import java.util.List;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.BATCH_LOG_REPORT;

/**
 * 处理日志上报数据
 *
 */
@Component
public class ReportLogHttpRequestHandler extends PostHttpRequestHandler {
    @Override
    public boolean supports(String path) {
        return BATCH_LOG_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {

        SilenceJobLog.LOCAL.debug("Begin Handler Log Report Data. [{}]", content);
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();

        Assert.notEmpty(args, () -> new SilenceJobServerException("日志上报的数据不能为空. ReqId:[{}]", retryRequest.getReqId()));

        JSONArray jsonArray = JSON.parseArray(args[0].toString());
        List<RetryLogTaskDTO> retryTasks = new ArrayList<>();
        List<JobLogTaskDTO> jobTasks = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject node = jsonArray.getJSONObject(i);
            // 直接获取字段值并转换为字符串（支持非字符串类型自动转换）
            String logType = node.getString(SystemConstants.JSON_FILED_LOG_TYPE);

            // 处理空值及JOB类型
            if (logType == null || LogTypeEnum.JOB.name().equals(logType)) {
                jobTasks.add(node.toJavaObject(JobLogTaskDTO.class));
                continue;
            }

            // 处理RETRY类型
            if (LogTypeEnum.RETRY.name().equals(logType)) {
                retryTasks.add(node.toJavaObject(RetryLogTaskDTO.class));
            }
        }

        // 批量新增日志数据
        if (CollectionUtils.isNotEmpty(jobTasks)) {
            ActorRef actorRef = ActorGenerator.jobLogActor();
            actorRef.tell(jobTasks, actorRef);
        }

        if (CollectionUtils.isNotEmpty(retryTasks)) {
            ActorRef actorRef = ActorGenerator.logActor();
            actorRef.tell(retryTasks, actorRef);
        }

        return new SilenceJobRpcResult(200, "Batch Log Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }

}
