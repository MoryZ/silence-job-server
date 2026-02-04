package com.old.silence.job.server.api;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.domain.service.RetryDeadLetterService;
import com.old.silence.job.server.dto.BatchDeleteRetryDeadLetterCommand;
import com.old.silence.job.server.dto.BatchRollBackRetryDeadLetterCommand;
import com.old.silence.job.server.dto.RetryDeadLetterQuery;
import com.old.silence.job.server.vo.RetryDeadLetterResponseVO;
import java.math.BigInteger;


/**
 * 死信队列接口
 *
 */
@RestController
@RequestMapping("/api/v1")
public class RetryDeadLetterResource {
    private final RetryDeadLetterService retryDeadLetterService;

    public RetryDeadLetterResource(RetryDeadLetterService retryDeadLetterService) {
        this.retryDeadLetterService = retryDeadLetterService;
    }

    @GetMapping(value = "/retryDeadLetters", params = {"pageNo", "pageSize"})
    public IPage<RetryDeadLetterResponseVO> queryPage(Page<RetryDeadLetter> page, RetryDeadLetterQuery retryDeadLetterQuery) {
        var queryWrapper = QueryWrapperConverter.convert(retryDeadLetterQuery, RetryDeadLetter.class);
        return retryDeadLetterService.queryPage(page, queryWrapper);
    }

    @GetMapping("/retryDeadLetters/{id}")
    public RetryDeadLetterResponseVO findById(@RequestParam String groupName,
                                              @PathVariable BigInteger id) {
        return retryDeadLetterService.findById(groupName, id);
    }

    
    @PostMapping("/retryDeadLetters/batchRollback")
    public int rollback(@RequestBody @Validated BatchRollBackRetryDeadLetterCommand rollBackRetryDeadLetterVO) {
        return retryDeadLetterService.rollback(rollBackRetryDeadLetterVO);
    }

    
    @DeleteMapping("/retryDeadLetters/batchDelete")
    public boolean batchDelete(@RequestBody @Validated BatchDeleteRetryDeadLetterCommand deadLetterVO) {
        return retryDeadLetterService.batchDelete(deadLetterVO);
    }
}
