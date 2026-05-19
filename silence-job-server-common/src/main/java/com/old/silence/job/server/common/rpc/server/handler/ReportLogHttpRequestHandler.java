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

    /**
     * 处理日志数组，提取 JOB 和 RETRY 类型的日志任务
     */
    private void processLogArray(JSONArray array, List<JobLogTaskDTO> jobTasks, List<RetryLogTaskDTO> retryTasks) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject node = array.getJSONObject(i);
            String logType = node.getString(SystemConstants.JSON_FILED_LOG_TYPE);

            if (logType == null || LogTypeEnum.JOB.name().equals(logType)) {
                jobTasks.add(node.toJavaObject(JobLogTaskDTO.class));
            } else if (LogTypeEnum.RETRY.name().equals(logType)) {
                retryTasks.add(node.toJavaObject(RetryLogTaskDTO.class));
            }
        }
    }

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

        // 获取日志数据列表
        List<RetryLogTaskDTO> retryTasks = new ArrayList<>();
        List<JobLogTaskDTO> jobTasks = new ArrayList<>();
        
        // 处理 args[0]，可能是各种格式：[{...}], [[{...}]], "[{...}]", "[[{...}]]"
        Object logsData = args[0];
        
        // 如果是字符串，需要解析
        if (logsData instanceof String) {
            String strData = ((String) logsData).trim();
            if (strData.startsWith("[[")) {
                // 双重数组字符串格式：提取内层数组
                int firstBracket = strData.indexOf('[');
                int lastBracket = strData.lastIndexOf(']');
                if (firstBracket >= 0 && lastBracket > firstBracket) {
                    String innerStr = strData.substring(firstBracket, lastBracket + 1);
                    try {
                        JSONArray innerArray = JSON.parseArray(innerStr);
                        processLogArray(innerArray, jobTasks, retryTasks);
                    } catch (Exception e) {
                        SilenceJobLog.LOCAL.warn("Failed to parse double array String: {}", e.getMessage());
                    }
                }
            } else if (strData.startsWith("[")) {
                // 普通数组字符串格式
                try {
                    JSONArray array = JSON.parseArray(strData);
                    processLogArray(array, jobTasks, retryTasks);
                } catch (Exception e) {
                    SilenceJobLog.LOCAL.warn("Failed to parse array String: {}", e.getMessage());
                }
            }
        } else if (logsData instanceof JSONArray jsonArray) {

            if (!jsonArray.isEmpty()) {
                Object firstElement = jsonArray.get(0);
                if (firstElement instanceof JSONArray) {
                    // 双重数组 [[{...}]]
                    JSONArray innerArray = jsonArray.getJSONArray(0);
                    processLogArray(innerArray, jobTasks, retryTasks);
                } else {
                    // 普通数组 [{...}]
                    processLogArray(jsonArray, jobTasks, retryTasks);
                }
            }
        } else {
            SilenceJobLog.LOCAL.warn("Unknown logsData type: [{}]", logsData.getClass().getName());
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
