package com.old.silence.job.server.api;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.domain.service.WorkflowBatchService;
import com.old.silence.job.server.dto.WorkflowBatchQuery;
import com.old.silence.job.server.vo.WorkflowBatchResponseVO;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigInteger;
import java.util.Set;


@RestController
@RequestMapping("/api/v1")
public class WorkflowBatchResource {
    private final WorkflowBatchService workflowBatchService;

    public WorkflowBatchResource(WorkflowBatchService workflowBatchService) {
        this.workflowBatchService = workflowBatchService;
    }

    @GetMapping(value = "/workflowBatches", params = {"pageNo", "pageSize"})
    public IPage<WorkflowBatchResponseVO> queryPage(Page<WorkflowTaskBatch> page, WorkflowBatchQuery queryVO) {
        return workflowBatchService.queryPage(page, queryVO);
    }

    
    @GetMapping("/workflowBatches/{id}")
    public WorkflowDetailResponseVO getWorkflowBatchDetail(@PathVariable BigInteger id) {
        return workflowBatchService.getWorkflowBatchDetail(id);
    }

    @PostMapping("/workflowBatches/{id}/stop")
    public Boolean stop(@PathVariable BigInteger id) {
        return workflowBatchService.stop(id);
    }

    @DeleteMapping("/workflowBatches/ids")
    public Boolean deleteByIds(@RequestBody
                               @NotEmpty(message = "ids不能为空")
                               @Size(max = 100, message = "最多删除 {max} 个")
                               Set<BigInteger> ids) {
        return workflowBatchService.deleteByIds(ids);
    }
}
