package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.server.domain.model.ServerNode;
import com.old.silence.job.server.domain.service.PodsService;
import com.old.silence.job.server.dto.ServerNodeQuery;
import com.old.silence.job.server.vo.ServerNodeResponseVO;

/**
 * @author moryzang
 */
@RestController
@RequestMapping("/api/v1")
public class PodsResource {

    private final PodsService podsService;

    public PodsResource(PodsService podsService) {
        this.podsService = podsService;
    }

    @GetMapping(value = "/pods", params = {"pageNo", "pageSize"})
    public IPage<ServerNodeResponseVO> pods(Page<ServerNode> page, ServerNodeQuery serverNodeQuery) {
        return podsService.pods(page, serverNodeQuery);
    }

}
