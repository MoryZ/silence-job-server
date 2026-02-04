package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.domain.service.RetryTaskService;
import com.old.silence.job.server.dto.RetryTaskLogMessageQueryVO;
import com.old.silence.job.server.dto.RetryTaskQuery;
import com.old.silence.job.server.vo.RetryTaskLogMessageResponseVO;
import com.old.silence.job.server.vo.RetryTaskResponseVO;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.util.Set;

/**
 * 重试日志接口
 *
 */
@RestController
@RequestMapping("/api/v1")
public class RetryTaskResource {

    private final RetryTaskService retryTaskService;

    public RetryTaskResource(RetryTaskService retryTaskService) {
        this.retryTaskService = retryTaskService;
    }


    @GetMapping(value = "/retryTasks", params = {"pageNo", "pageSize"})
    public IPage<RetryTaskResponseVO> queryPage(Page<RetryTask> page, RetryTaskQuery retryTaskQuery) {
        var queryWrapper = QueryWrapperConverter.convert(retryTaskQuery, RetryTask.class);
        return retryTaskService.queryPage(page, queryWrapper);
    }

    
    @GetMapping(value = "/retryTasks/messages", params = {"pageNo", "pageSize"})
    public RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(Page<RetryTaskLogMessage> page, RetryTaskLogMessageQueryVO queryVO) {
        return retryTaskService.getRetryTaskLogMessagePage(page, queryVO);
    }

    
    @GetMapping("/retryTasks/{id}")
    public RetryTaskResponseVO getRetryTaskById(@PathVariable BigInteger id) {
        return retryTaskService.getRetryTaskById(id);
    }

    
    @PutMapping("/retryTasks/{id}/stop")
    public Boolean stopById(@PathVariable BigInteger id) {
        return retryTaskService.stopById(id);
    }

    
    @DeleteMapping("/retryTasks/{id}")
    public Boolean deleteById(@PathVariable BigInteger id) {
        return retryTaskService.deleteById(id);
    }

    
    @DeleteMapping("/retryTasks")
    public Boolean batchDelete(@RequestBody @NotEmpty(message = "ids不能为空") Set<BigInteger> ids) {
        return retryTaskService.batchDelete(ids);
    }


}
