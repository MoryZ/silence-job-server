package com.old.silence.job.server.domain.service.task;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;

/**
 * 重试任务操作类
 *
 */
@Component
public class RetryTaskAccess extends AbstractTaskAccess<Retry> {

    @Autowired
    private RetryDao retryDao;

    @Override
    public boolean supports(OperationTypeEnum operationType) {
        return OperationTypeEnum.RETRY.equals(operationType);
    }

    @Override
    protected int doUpdate(Retry retry, LambdaUpdateWrapper<Retry> query) {
        return retryDao.update(retry, query);
    }

    @Override
    protected int doInsertBatch(List<Retry> list) {
        return retryDao.insertBatch(list);
    }

    @Override
    protected Retry doOne(LambdaQueryWrapper<Retry> query) {
        return retryDao.selectOne(query);
    }

    @Override
    protected Page<Retry> doListPage(Page<Retry> iPage, final LambdaQueryWrapper<Retry> query) {
        return retryDao.selectPage(iPage, query);
    }

    @Override
    protected long doCount(LambdaQueryWrapper<Retry> query) {
        return retryDao.selectCount(query);
    }

    @Override
    protected int doInsert(Retry retry) {
        return retryDao.insert(retry);
    }

    @Override
    protected int doDelete(LambdaQueryWrapper<Retry> query) {
        return retryDao.delete(query);
    }

    @Override
    protected int doUpdateById(Retry retry) {
        return retryDao.updateById(retry);
    }

    @Override
    protected List<Retry> doList(LambdaQueryWrapper<Retry> query) {
        return retryDao.selectList(query);
    }
}
