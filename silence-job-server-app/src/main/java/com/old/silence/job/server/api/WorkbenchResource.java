package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.job.server.vo.ConfigStatVo;
import com.old.silence.job.server.vo.JobStatVo;
import com.old.silence.job.server.vo.MqStatVo;
import com.old.silence.job.server.vo.WorkBenchVo;


/**
 * @author MurrayZhang
 */
@RestController
@RequestMapping("/api/v1")
public class WorkbenchResource {


    @GetMapping("/workBench/statistics")
    public WorkBenchVo configStatistic() {
        ConfigStatVo configStat = new ConfigStatVo();
        configStat.setNamespaceCount(72);
        configStat.setListenerInstanceCount(796);
        configStat.setComponentCount(2);

        JobStatVo jobStat = new JobStatVo();
        jobStat.setLocalJobCount(4);
        jobStat.setRemoteJobCount(27);

        MqStatVo mqStat = new MqStatVo();
        mqStat.setTopicCount(67);
        mqStat.setPublishRelationCount(200);
        return new WorkBenchVo(configStat,jobStat,mqStat);
    }

}
