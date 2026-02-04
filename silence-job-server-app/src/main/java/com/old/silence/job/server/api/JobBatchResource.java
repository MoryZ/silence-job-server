package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.service.JobBatchService;
import com.old.silence.job.server.dto.JobBatchQuery;
import com.old.silence.job.server.vo.JobBatchResponseVO;

import java.math.BigInteger;
import java.util.Set;


@RestController
@RequestMapping("/api/v1")
public class JobBatchResource {
    private final JobBatchService jobBatchService;

    public JobBatchResource(JobBatchService jobBatchService) {
        this.jobBatchService = jobBatchService;
    }

    @GetMapping(value = "/jobBatches", params = {"pageNo","pageSize"})
    public IPage<JobBatchResponseVO> getJobBatchPage(Page<JobTaskBatch> page, JobBatchQuery jobQueryVO) {
        var queryWrapper = QueryWrapperConverter.convert(jobQueryVO, JobTaskBatch.class);
        return jobBatchService.queryPage(page, queryWrapper);
    }

    @GetMapping("/jobBatches/{id}")
    public JobBatchResponseVO getJobBatchDetail(@PathVariable BigInteger id) {
        return jobBatchService.getJobBatchDetail(id);
    }

    @PutMapping("/jobBatches/{taskBatchId}/stop")
    public Boolean stop(@PathVariable BigInteger taskBatchId) {
        return jobBatchService.stop(taskBatchId);
    }


    @PutMapping("/jobBatches/{taskBatchId}/retry")
    public Boolean retry(@PathVariable BigInteger taskBatchId) {
        return jobBatchService.retry(taskBatchId);
    }

    @DeleteMapping("/jobBatches")
    public Boolean deleteJobBatchByIds(@RequestBody Set<BigInteger> ids) {
        return jobBatchService.deleteJobBatchByIds(ids);
    }
}
