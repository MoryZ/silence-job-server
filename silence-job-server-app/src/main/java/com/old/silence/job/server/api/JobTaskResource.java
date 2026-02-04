package com.old.silence.job.server.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.service.JobTaskService;
import com.old.silence.job.server.dto.JobTaskQuery;
import com.old.silence.job.server.vo.JobTaskResponseVO;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class JobTaskResource {
    private final JobTaskService jobTaskService;

    public JobTaskResource(JobTaskService jobTaskService) {
        this.jobTaskService = jobTaskService;
    }

    @GetMapping(value = "/jobTasks", params = {"pageNo", "pageSize"})
    public IPage<JobTaskResponseVO> getJobTaskPage(Page<JobTask> page, JobTaskQuery jobTaskQuery) {
        return jobTaskService.getJobTaskPage(page, jobTaskQuery);
    }

    @GetMapping("/jobTasks/tree")
    public List<JobTaskResponseVO> getTreeJobTask(JobTaskQuery jobTaskQuery) {
        return jobTaskService.getTreeJobTask(jobTaskQuery);
    }

}
