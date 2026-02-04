package com.old.silence.job.server.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.api.assembler.JobMapper;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.service.JobService;
import com.old.silence.job.server.dto.ExportJobVO;
import com.old.silence.job.server.dto.JobCommand;
import com.old.silence.job.server.dto.JobQuery;
import com.old.silence.job.server.dto.JobTriggerVO;
import com.old.silence.job.server.util.ExportUtils;
import com.old.silence.job.server.util.ImportUtils;
import com.old.silence.job.server.vo.JobResponseVO;

import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Set;



@RestController
@RequestMapping("/api/v1")
public class JobResource {

    private final JobService jobService;
    private final JobMapper jobMapper;

    public JobResource(JobService jobService, JobMapper jobMapper) {
        this.jobService = jobService;
        this.jobMapper = jobMapper;
    }

    @GetMapping(value = "/jobs", params = {"pageNo", "pageSize"})
    public IPage<JobResponseVO> queryPage(Page<Job> page, JobQuery jobQuery) {
        var queryWrapper = QueryWrapperConverter.convert(jobQuery, Job.class);
        return jobService.queryPage(page, queryWrapper);
    }

    @GetMapping("/jobs")
    public List<JobResponseVO> findList(JobQuery jobQuery) {
        var queryWrapper = QueryWrapperConverter.convert(jobQuery, Job.class);
        return jobService.getJobList(queryWrapper);
    }

    @GetMapping("/jobs/{id}")
    public JobResponseVO findById(@PathVariable BigInteger id) {
        return jobService.findById(id);
    }

    @GetMapping("/jobs/cron")
    public List<Instant> getTimeByCron(@RequestParam String cron) {
        return jobService.getTimeByCron(cron);
    }

    @PostMapping("/jobs")
    public Boolean create(@RequestBody @Validated JobCommand jobCommand) {
        var job = jobMapper.convert(jobCommand);
        return jobService.create(job);
    }

    @PutMapping("/jobs/{id}")
    public Boolean update(@PathVariable BigInteger id, @RequestBody @Validated JobCommand jobCommand) {
        var job = jobMapper.convert(jobCommand);
        job.setId(id);
        return jobService.update(job);
    }

    @PutMapping("/jobs/{id}/run")
    public void run(@PathVariable BigInteger id) {
        jobService.updateJobStatus(id, true);
    }

    @PutMapping("/jobs/{id}/stop")
    public void stop(@PathVariable BigInteger id) {
        jobService.updateJobStatus(id, false);
    }

    @DeleteMapping("/jobs")
    public Boolean bulkDelete(@RequestBody @NotEmpty Set<BigInteger> ids) {
        return jobService.deleteJobByIds(ids);
    }

    @PostMapping("/jobs/{id}/trigger")
    public Boolean trigger(@PathVariable BigInteger id, @RequestBody @Validated JobTriggerVO jobTrigger) {
        return jobService.trigger(id, jobTrigger);
    }

    @PostMapping(value = "/jobs/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        var jobCommands = ImportUtils.parseList(file, JobCommand.class);
        var jobs = CollectionUtils.transformToList(jobCommands, jobMapper::convert);
        jobService.importJobs(jobs);
    }

    @PostMapping("/jobs/export")
    public ResponseEntity<String> exportGroup(@RequestBody ExportJobVO exportJobVO) {
        return ExportUtils.doExport(jobService.exportJobs(exportJobVO));
    }

}
