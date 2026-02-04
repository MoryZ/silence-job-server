package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.old.silence.job.server.domain.service.GroupConfigService;


@RestController
@RequestMapping("/api/v1")
public class PartitionResource {
    private final GroupConfigService groupConfigService;

    public PartitionResource(GroupConfigService groupConfigService) {
        this.groupConfigService = groupConfigService;
    }

    @GetMapping("/partitionTables")
    public List<Integer> getTablePartitionList() {
        return groupConfigService.getTablePartitionList();
    }


}
