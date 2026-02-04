package com.old.silence.job.server.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.api.assembler.GroupConfigMapper;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.service.GroupConfigService;
import com.old.silence.job.server.dto.ExportGroupCommand;
import com.old.silence.job.server.dto.GroupConfigCommand;
import com.old.silence.job.server.dto.GroupConfigQuery;
import com.old.silence.job.server.dto.GroupStatusUpdateCommand;
import com.old.silence.job.server.util.ExportUtils;
import com.old.silence.job.server.util.ImportUtils;
import com.old.silence.job.server.vo.CommonOptions;
import com.old.silence.job.server.vo.GroupConfigResponseVO;


import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class GroupConfigResource {
    private final GroupConfigService groupConfigService;
    private final GroupConfigMapper groupConfigMapper;

    public GroupConfigResource(GroupConfigService groupConfigService,
                               GroupConfigMapper groupConfigMapper) {
        this.groupConfigService = groupConfigService;
        this.groupConfigMapper = groupConfigMapper;
    }

    @GetMapping(path= "/groupConfigs", params = {"pageNo", "pageSize"})
    public IPage<GroupConfigResponseVO> queryPage(Page<GroupConfig> page, GroupConfigQuery groupConfigQuery) {
        var queryWrapper = QueryWrapperConverter.convert(groupConfigQuery, GroupConfig.class);
        return groupConfigService.queryPage(page, queryWrapper);
    }

    @GetMapping("/groupConfigs/{groupName}")
    public GroupConfigResponseVO getGroupConfigByGroupName(@PathVariable String groupName) {
        var groupConfigResponseVO = groupConfigService.getGroupConfigByGroupName(groupName);
        Optional.ofNullable(groupConfigResponseVO.getIdGeneratorMode()).ifPresent(idGeneratorMode -> {
            groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDescription());
        });
        return groupConfigResponseVO;
    }

    @GetMapping("/groupConfigs")
    public List<CommonOptions> getAllGroupNameList() {
        var groupConfigs = groupConfigService.getAllGroupNameList();
        return  CollectionUtils.transformToList(groupConfigs, groupConfig -> new CommonOptions(groupConfig.getGroupName(), groupConfig.getId()));

    }

    @PostMapping("/groupConfigs/findByNamespaces")
    public List<GroupConfigResponseVO> getAllGroupNameList(@RequestBody List<String> namespaceIds) {
        return groupConfigService.getAllGroupConfigList(namespaceIds);
    }

    @PostMapping("/groupConfigs")
    public Boolean create(@RequestBody @Validated GroupConfigCommand groupConfigCommand) {
        var groupConfig = groupConfigMapper.convert(groupConfigCommand);
        return groupConfigService.create(groupConfig);
    }

    @PostMapping(value = "/groupConfigs/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart MultipartFile file) throws IOException {
        var groupConfigCommands = ImportUtils.parseList(file, GroupConfigCommand.class);
        var groupConfigs = CollectionUtils.transformToList(groupConfigCommands, groupConfigMapper::convert);
        groupConfigService.importGroup(groupConfigs);
    }

    @PostMapping("/groupConfigs/export")
    public ResponseEntity<String> exportGroup(@RequestBody ExportGroupCommand exportGroupComman) {
        return ExportUtils.doExport(groupConfigService.exportGroup(exportGroupComman));
    }


    @PutMapping("/groupConfigs/{id}")
    public Boolean update(@PathVariable BigInteger id, @RequestBody @Validated GroupConfigCommand groupConfigCommand) {
        var groupConfig = groupConfigMapper.convert(groupConfigCommand);
        groupConfig.setId(id);
        return groupConfigService.updateGroup(groupConfig);
    }

    @PutMapping("/groupConfigs/enable")
    public Boolean enable(@RequestBody @Validated GroupStatusUpdateCommand groupStatusUpdateCommand) {
        return groupConfigService.updateGroupStatus(groupStatusUpdateCommand.getGroupName(), true);
    }

    @PutMapping("/groupConfigs/disable")
    public Boolean disable(@RequestBody @Validated GroupStatusUpdateCommand requestVO) {
        return groupConfigService.updateGroupStatus(requestVO.getGroupName(), false);
    }

    @DeleteMapping("/groupConfigs/{groupName}")
    public boolean deleteByGroupName(@PathVariable String groupName) {
        return groupConfigService.deleteByGroupName(groupName);
    }
}
