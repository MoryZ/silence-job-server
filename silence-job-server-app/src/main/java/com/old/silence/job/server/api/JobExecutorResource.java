package com.old.silence.job.server.api;

import java.math.BigInteger;
import java.util.Set;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.domain.model.JobExecutor;
import com.old.silence.job.server.domain.service.JobExecutorService;
import com.old.silence.job.server.dto.JobExecutorQuery;

/**
 * @author moryzang
 */
@RestController
@RequestMapping("/api/v1")
public class JobExecutorResource {

    private final JobExecutorService jobExecutorService;

    public JobExecutorResource(JobExecutorService jobExecutorService) {
        this.jobExecutorService = jobExecutorService;
    }

    @GetMapping(value = "/jobExecutors", params = {"pageNo", "pageSize"})
    public Page<JobExecutor> getJobPage(Page<JobExecutor> page , JobExecutorQuery jobExecutorQuery) {
        var queryWrapper = QueryWrapperConverter.convert(jobExecutorQuery, JobExecutor.class);
        return jobExecutorService.queryPage(page, queryWrapper);
    }

    @GetMapping("/jobExecutors")
    public Set<String> getJobList(JobExecutorQuery jobExecutorQuery) {
        var queryWrapper = QueryWrapperConverter.convert(jobExecutorQuery, JobExecutor.class);
        return jobExecutorService.getJobExecutorList(queryWrapper);
    }

    @GetMapping("/jobExecutors/{id}")
    public JobExecutor findById(@PathVariable BigInteger id) {
        return jobExecutorService.findById(id);
    }

    @DeleteMapping("/jobExecutors/ids")
    public Boolean deleteJobExecutorsById(@RequestBody @NotEmpty Set<Long> ids) {
        return jobExecutorService.deleteJobExecutorByIds(ids);
    }
}
