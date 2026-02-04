package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.old.silence.job.server.domain.service.GroupConfigService;


@RestController
@RequestMapping("/api/v1")
public class OnlinePodsResource {
    private final GroupConfigService groupConfigService;

    public OnlinePodsResource(GroupConfigService groupConfigService) {
        this.groupConfigService = groupConfigService;
    }

    @GetMapping("/online/pods/{groupName}")
    public List<String> getOnlinePods(@PathVariable String groupName) {
        return groupConfigService.getOnlinePods(groupName);
    }

}
