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
import com.old.silence.job.server.api.assembler.NamespaceMapper;
import com.old.silence.job.server.domain.model.Namespace;
import com.old.silence.job.server.domain.service.NamespaceService;
import com.old.silence.job.server.dto.NamespaceCommand;
import com.old.silence.job.server.dto.NamespaceQueryVO;
import com.old.silence.job.server.vo.NamespaceResponseVO;

import java.math.BigInteger;
import java.util.List;



@RestController
@RequestMapping("/api/v1")
public class NamespaceResource {

    private final NamespaceService namespaceService;

    private final NamespaceMapper namespaceMapper;

    public NamespaceResource(NamespaceService namespaceService, NamespaceMapper namespaceMapper) {
        this.namespaceService = namespaceService;
        this.namespaceMapper = namespaceMapper;
    }

    @GetMapping("/namespaces")
    public List<NamespaceResponseVO> getAllNamespace() {
        return namespaceService.getAllNamespace();
    }

    @GetMapping(value = "/namespaces", params = {"pageNo", "pageSize"})
    public IPage<NamespaceResponseVO> query(Page<Namespace> pageDTO, NamespaceQueryVO queryVO) {
        var queryWrapper = QueryWrapperConverter.convert(queryVO, Namespace.class);
        return namespaceService.query(pageDTO, queryWrapper);
    }

    @PostMapping("/namespaces")
    public void create(@RequestBody @Validated NamespaceCommand namespaceCommand) {
        var namespace = namespaceMapper.convert(namespaceCommand);
        namespaceService.create(namespace);
    }

    @PutMapping("/namespaces/{id}")
    public void update(@PathVariable BigInteger id, @RequestBody @Validated NamespaceCommand namespaceCommand) {
        var namespace = namespaceMapper.convert(namespaceCommand);
        namespace.setId(id);
        namespaceService.update(namespace);
    }

    @DeleteMapping("/namespaces/{uniqueId}")
    public void delete(@PathVariable String uniqueId) {
        namespaceService.deleteByUniqueId(uniqueId);
    }

}
