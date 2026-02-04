package com.old.silence.job.server.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.NodeType;
import com.old.silence.job.server.common.dto.DistributeInstance;
import com.old.silence.job.server.domain.service.DashboardService;
import com.old.silence.job.server.domain.service.PodsService;
import com.old.silence.job.server.dto.JobLineQueryVo;
import com.old.silence.job.server.dto.LineQueryVO;
import com.old.silence.job.server.vo.ActivePodQuantityResponseVO;
import com.old.silence.job.server.vo.DashboardCardResponseVO;
import com.old.silence.job.server.vo.DashboardRetryLineResponseVO;

import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class DashboardResource {

    private final DashboardService dashBoardService;
    private final PodsService podsService;

    public DashboardResource(DashboardService dashBoardService,
                             PodsService podsService) {
        this.dashBoardService = dashBoardService;
        this.podsService = podsService;
    }

    @GetMapping("/dashboard/task-retry-job")
    public DashboardCardResponseVO taskRetryJob() {
        return dashBoardService.taskRetryJob();
    }

    @GetMapping("/dashboard/retry/line")
    public DashboardRetryLineResponseVO retryLineList(Page<Object> page, LineQueryVO queryVO) {
        return dashBoardService.retryLineList(page, queryVO);
    }

    @GetMapping("/dashboard/job/line")
    public DashboardRetryLineResponseVO jobLineList(Page<Object> page, JobLineQueryVo queryVO) {
        return dashBoardService.jobLineList(page, queryVO);
    }

    @GetMapping("/dashboard/pods")
    public ActivePodQuantityResponseVO pods() {
        var podsPage = podsService.pods(new Page<>(1, 100), null);
        ActivePodQuantityResponseVO activePodQuantityResponseVO = new ActivePodQuantityResponseVO();
        activePodQuantityResponseVO.setTotal(podsPage.getTotal());

        var serverTotal = podsPage.getRecords().stream().filter(serverNodeResponseVO -> NodeType.SERVER.equals(serverNodeResponseVO.getNodeType())).count();
        var clientTotal = podsPage.getRecords().stream().filter(serverNodeResponseVO -> NodeType.CLIENT.equals(serverNodeResponseVO.getNodeType())).count();
        activePodQuantityResponseVO.setServerTotal(serverTotal);
        activePodQuantityResponseVO.setClientTotal(clientTotal);
        return activePodQuantityResponseVO;
    }

    @GetMapping("/dashboard/consumer/bucket")
    public Set<Integer> allConsumerGroupName() {
        return DistributeInstance.INSTANCE.getConsumerBucket();
    }

}
