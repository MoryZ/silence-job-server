package com.old.silence.job.server.api;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.server.api.assembler.NotifyConfigMapper;
import com.old.silence.job.server.domain.model.NotifyConfig;
import com.old.silence.job.server.domain.service.NotifyConfigService;
import com.old.silence.job.server.dto.NotifyConfigCommand;
import com.old.silence.job.server.dto.NotifyConfigQuery;
import com.old.silence.job.server.vo.NotifyConfigResponseVO;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/v1")
public class NotifyConfigResource {
    private final NotifyConfigService notifyConfigService;
    private final NotifyConfigMapper notifyConfigMapper;

    public NotifyConfigResource(NotifyConfigService notifyConfigService, NotifyConfigMapper notifyConfigMapper) {
        this.notifyConfigService = notifyConfigService;
        this.notifyConfigMapper = notifyConfigMapper;
    }

    @GetMapping(value = "/notifyConfig", params = {"pageNo", "pageSize"})
    public IPage<NotifyConfigResponseVO> getNotifyConfigList(Page<NotifyConfig> page, NotifyConfigQuery notifyConfigQuery) {
        var queryWrapper = QueryWrapperConverter.convert(notifyConfigQuery, NotifyConfig.class);

        return notifyConfigService.getNotifyConfigList(page, queryWrapper);
    }

    
    @GetMapping("/notifyConfig/all/{systemTaskType}")
    public List<NotifyConfig> getNotifyConfigBySystemTaskTypeList(@PathVariable SystemTaskType systemTaskType) {
        return notifyConfigService.getNotifyConfigBySystemTaskTypeList(systemTaskType);
    }

    
    @GetMapping("/notifyConfig/{id}")
    public NotifyConfigResponseVO getNotifyConfigDetail(@PathVariable BigInteger id) {
        return notifyConfigService.getNotifyConfigDetail(id);
    }

    
    @PostMapping("/notifyConfig")
    public Boolean create(@RequestBody @Validated NotifyConfigCommand notifyConfigCommand) {
        var notifyConfig = notifyConfigMapper.convert(notifyConfigCommand);
        return notifyConfigService.create(notifyConfig);
    }

    
    @PutMapping("/notifyConfig/{id}")
    public Boolean update(@PathVariable BigInteger id, @RequestBody @Validated NotifyConfigCommand notifyConfigCommand) {
        var notifyConfig = notifyConfigMapper.convert(notifyConfigCommand);
        notifyConfig.setId(id);
        return notifyConfigService.update(notifyConfig);
    }

    
    @PutMapping("/notifyConfig/{id}/{status}")
    public Boolean updateStatus(@PathVariable BigInteger id, @PathVariable Boolean status) {
        return notifyConfigService.updateStatus(id, status);
    }

    
    @DeleteMapping("/notifyConfig")
    public Boolean batchDeleteNotify(@RequestBody @NotEmpty Set<BigInteger> ids) {
        return notifyConfigService.batchDeleteNotify(ids);
    }
}
