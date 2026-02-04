package com.old.silence.job.server.domain.service.task;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDeadLetterDao;


@Component
public class RetryDeadLetterTaskAccess extends AbstractTaskAccess<RetryDeadLetter> {

    @Autowired
    private RetryDeadLetterDao retryDeadLetterDao;

    @Override
    public boolean supports(OperationTypeEnum operationType) {
        return OperationTypeEnum.RETRY_DEAD_LETTER.equals(operationType);
    }

    @Override
    protected int doUpdate(RetryDeadLetter retryDeadLetter, LambdaUpdateWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.update(retryDeadLetter, query);
    }

    @Override
    protected int doInsertBatch(List<RetryDeadLetter> list) {
        return retryDeadLetterDao.insertBatch(list);
    }

    @Override
    protected RetryDeadLetter doOne(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.selectOne(query);
    }

    @Override
    protected Page<RetryDeadLetter> doListPage(Page<RetryDeadLetter> PageDTO,
                                               final LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.selectPage(PageDTO, query);
    }

    @Override
    protected long doCount(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.selectCount(query);
    }

    @Override
    protected int doInsert(RetryDeadLetter retryDeadLetter) {
        return retryDeadLetterDao.insert(retryDeadLetter);
    }

    @Override
    protected int doDelete(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.delete(query);
    }

    @Override
    protected int doUpdateById(RetryDeadLetter retryDeadLetter) {
        return retryDeadLetterDao.updateById(retryDeadLetter);
    }

    @Override
    protected List<RetryDeadLetter> doList(LambdaQueryWrapper<RetryDeadLetter> query) {
        return retryDeadLetterDao.selectList(query);
    }
}
