package com.old.silence.job.server.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.api.assembler.RetrySceneConfigMapper;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.service.RetrySceneConfigService;
import com.old.silence.job.server.dto.ExportSceneCommand;
import com.old.silence.job.server.dto.SceneConfigQuery;
import com.old.silence.job.server.dto.SceneConfigCommand;
import com.old.silence.job.server.util.ExportUtils;
import com.old.silence.job.server.util.ImportUtils;
import com.old.silence.job.server.vo.RetrySceneConfigResponseVO;

import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/**
 * 重试场景接口
 *
 */
@RestController
@RequestMapping("/api/v1")
public class RetrySceneConfigResource {
    private final RetrySceneConfigService retrySceneConfigService;
    private final RetrySceneConfigMapper retrySceneConfigMapper;

    public RetrySceneConfigResource(RetrySceneConfigService retrySceneConfigService,
                                    RetrySceneConfigMapper retrySceneConfigMapper) {
        this.retrySceneConfigService = retrySceneConfigService;
        this.retrySceneConfigMapper = retrySceneConfigMapper;
    }

    @GetMapping(value = "/sceneConfig", params = {"pageNo", "pageSize"})
    public IPage<RetrySceneConfigResponseVO> getSceneConfigPageList(Page<RetrySceneConfig> page, SceneConfigQuery sceneConfigQuery) {
        var queryWrapper = QueryWrapperConverter.convert(sceneConfigQuery, RetrySceneConfig.class);
        return retrySceneConfigService.queryPage(page, queryWrapper);
    }

    @GetMapping(value = "/sceneConfig", params = { "groupName", "!pageNo", "!pageSize"})
    public List<RetrySceneConfigResponseVO> getSceneConfigList(@RequestParam String groupName) {
        return retrySceneConfigService.getSceneConfigList(groupName);
    }

    @GetMapping("/sceneConfig/{id}")
    public RetrySceneConfigResponseVO findById(@PathVariable BigInteger id) {
        return retrySceneConfigService.findById(id);
    }

    @PutMapping("/sceneConfig/{id}/enable")
    public Boolean enable(@PathVariable BigInteger id) {
        return retrySceneConfigService.updateStatus(id, true);
    }

    @PutMapping("/sceneConfig/{id}/disable")
    public Boolean disable(@PathVariable BigInteger id) {
        return retrySceneConfigService.updateStatus(id, false);
    }

    @PostMapping("/sceneConfig")
    public Boolean create(@RequestBody @Validated SceneConfigCommand sceneConfigCommand) {
        var sceneConfig = retrySceneConfigMapper.convert(sceneConfigCommand);
        return retrySceneConfigService.create(sceneConfig);
    }

    @PutMapping("/sceneConfig/{id}")
    public Boolean update(@PathVariable BigInteger id, @RequestBody @Validated SceneConfigCommand sceneConfigCommand) {
        var retrySceneConfig = retrySceneConfigMapper.convert(sceneConfigCommand);
        retrySceneConfig.setId(id);
        return retrySceneConfigService.update(retrySceneConfig);
    }

    @PostMapping(value = "/sceneConfig/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart MultipartFile file) throws IOException {
        var sceneConfigCommands = ImportUtils.parseList(file, SceneConfigCommand.class);
        // 写入数据
        retrySceneConfigService.importSceneConfig(sceneConfigCommands);
    }

    @PostMapping("/sceneConfig/export")
    public ResponseEntity<String> export(@RequestBody ExportSceneCommand exportSceneCommand) {
        return ExportUtils.doExport(retrySceneConfigService.exportSceneConfig(exportSceneCommand));
    }

    @DeleteMapping("/sceneConfig/ids")
    public boolean deleteByIds(@RequestBody @NotEmpty Set<BigInteger> ids) {
        return retrySceneConfigService.deleteByIds(ids);
    }

}
