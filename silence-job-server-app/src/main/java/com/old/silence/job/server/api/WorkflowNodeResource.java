package com.old.silence.job.server.api;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

import com.old.silence.job.server.domain.service.WorkflowNodeService;


@RestController
@RequestMapping("/api/v1")
public class WorkflowNodeResource {
    private final WorkflowNodeService workflowNodeService;

    public WorkflowNodeResource(WorkflowNodeService workflowNodeService) {
        this.workflowNodeService = workflowNodeService;
    }

    @PutMapping("/workflowNodes/{nodeId}/{workflowTaskBatchId}/stop")
     public Boolean stop(@PathVariable BigInteger nodeId, @PathVariable BigInteger workflowTaskBatchId) {
        return workflowNodeService.stop(nodeId, workflowTaskBatchId);
    }

    @PutMapping("/workflowNodes/{nodeId}/{workflowTaskBatchId}")
    public Boolean retry(@PathVariable BigInteger nodeId,
                         @PathVariable BigInteger workflowTaskBatchId) {
        return workflowNodeService.retry(nodeId, workflowTaskBatchId);
    }
}
