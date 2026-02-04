package com.old.silence.job.server.vo;




/**
* @author MurrayZhang   
*/


public class WorkBenchVo {
    private ConfigStatVo configCenter;
    private JobStatVo jobCenter;
    private MqStatVo mqCenter;

    public WorkBenchVo(ConfigStatVo configCenter, JobStatVo jobCenter, MqStatVo mqCenter) {
        this.configCenter = configCenter;
        this.jobCenter = jobCenter;
        this.mqCenter = mqCenter;
    }

    public ConfigStatVo getConfigCenter() {
        return configCenter;
    }

    public void setConfigCenter(ConfigStatVo configCenter) {
        this.configCenter = configCenter;
    }

    public JobStatVo getJobCenter() {
        return jobCenter;
    }

    public void setJobCenter(JobStatVo jobCenter) {
        this.jobCenter = jobCenter;
    }

    public MqStatVo getMqCenter() {
        return mqCenter;
    }

    public void setMqCenter(MqStatVo mqCenter) {
        this.mqCenter = mqCenter;
    }
}
