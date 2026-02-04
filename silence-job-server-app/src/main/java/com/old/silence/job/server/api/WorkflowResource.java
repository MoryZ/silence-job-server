package com.old.silence.job.server.api;

import cn.hutool.core.lang.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.api.assembler.WorkflowMapper;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.domain.service.WorkflowService;
import com.old.silence.job.server.dto.CheckDecisionVO;
import com.old.silence.job.server.dto.ExportWorkflowVO;
import com.old.silence.job.server.dto.WorkflowCommand;
import com.old.silence.job.server.dto.WorkflowQuery;
import com.old.silence.job.server.dto.WorkflowTriggerVO;
import com.old.silence.job.server.util.ExportUtils;
import com.old.silence.job.server.util.ImportUtils;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;
import com.old.silence.job.server.vo.WorkflowResponseVO;


import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/v1")
public class WorkflowResource {

    private final WorkflowService workflowService;
    private final WorkflowMapper workflowMapper;

    public WorkflowResource(WorkflowService workflowService, WorkflowMapper workflowMapper) {
        this.workflowService = workflowService;
        this.workflowMapper = workflowMapper;
    }


    @GetMapping("/workflows/{id}")
    public WorkflowDetailResponseVO getWorkflowDetail(@PathVariable BigInteger id) {
        return workflowService.getWorkflowDetail(id);
    }

    @GetMapping(path = "/workflows", params={"!pageNo", "!pageSize"})
    public List<WorkflowResponseVO> getWorkflowNameList(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) BigInteger workflowId,
            @RequestParam(required = false) String groupName) {
        return workflowService.getWorkflowNameList(keywords, workflowId, groupName);
    }

    @GetMapping(value = "/workflows", params = {"pageNo", "pageSize"})
    public IPage<WorkflowResponseVO> listPage(Page<Workflow> page, WorkflowQuery queryVO) {
        return workflowService.queryPage(page, queryVO);
    }

    @PostMapping("/workflows")
    public Boolean create(@RequestBody @Validated WorkflowCommand workflowCommand) throws Exception {
        var workflow = workflowMapper.convert(workflowCommand);
        return workflowService.create(workflow, workflowCommand.getNodeConfig());
    }

    @PostMapping("/workflows/trigger")
    public Boolean trigger(@RequestBody @Validated WorkflowTriggerVO triggerVO) {
        return workflowService.trigger(triggerVO);
    }


    @PostMapping("/workflows/check-node-expression")
    public Pair<Integer, Object> checkNodeExpression(@RequestBody @Validated CheckDecisionVO checkDecisionVO) {
        return workflowService.checkNodeExpression(checkDecisionVO);
    }


    @PostMapping(value = "/workflows/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart("file") MultipartFile file) throws Exception {
        // 写入数据
        var workflowCommands = ImportUtils.parseList(file, WorkflowCommand.class);
        var workflows = CollectionUtils.transformToList(workflowCommands, workflowMapper::convert);
        workflowService.importWorkflowTask(workflows);
    }


    @PostMapping("/workflows/export")
    public ResponseEntity<String> export(@RequestBody ExportWorkflowVO exportWorkflowVO) {
        return ExportUtils.doExport(workflowService.exportWorkflowTask(exportWorkflowVO));
    }

    @PutMapping("/workflows/{id}")
    public Boolean update(@PathVariable BigInteger id, @RequestBody @Validated WorkflowCommand workflowCommand) {
        var workflow = workflowMapper.convert(workflowCommand);
        workflow.setId(id);
        return workflowService.update(workflow, workflowCommand.getNodeConfig());
    }

    @PutMapping("/workflows/{id}/enable")
    public Boolean enable(@PathVariable BigInteger id) {
        return workflowService.updateStatus(id, true);
    }

    @PutMapping("/workflows/{id}/disable")
    public Boolean disable(@PathVariable BigInteger id) {
        return workflowService.updateStatus(id, false);
    }

    @DeleteMapping("/workflows")
    public Boolean deleteByIds(@RequestBody @NotEmpty(message = "ids不能为空") Set<BigInteger> ids) {
        return workflowService.deleteByIds(ids);
    }

}
