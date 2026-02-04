package com.old.silence.job.server.domain.service.task;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.common.enums.OperationTypeEnum;


public abstract class AbstractTaskAccess<T> implements TaskAccess<T> {

    @Override
    public List<T> list(LambdaQueryWrapper<T> query) {
        return doList(query);
    }

    @Override
    public int update(T t, LambdaUpdateWrapper<T> query) {
        return doUpdate(t, query);
    }

    public abstract boolean supports(OperationTypeEnum operationType);

    protected abstract int doUpdate(T t, LambdaUpdateWrapper<T> query);

    @Override
    public int updateById(T t) {
        return doUpdateById(t);
    }

    @Override
    public int delete(LambdaQueryWrapper<T> query) {
        return doDelete(query);
    }

    @Override
    public int insert(T t) {
        return doInsert(t);
    }

    @Override
    public int insertBatch(List<T> list) {
        return doInsertBatch(list);
    }

    protected abstract int doInsertBatch(List<T> list);

    @Override
    public Page<T> listPage(Page<T> iPage, final LambdaQueryWrapper<T> query) {
        return doListPage(iPage, query);
    }

    @Override
    public T one(LambdaQueryWrapper<T> query) {
        return doOne(query);
    }

    protected abstract T doOne(LambdaQueryWrapper<T> query);

    protected abstract Page<T> doListPage(Page<T> iPage, LambdaQueryWrapper<T> query);

    @Override
    public long count(LambdaQueryWrapper<T> query) {
        return doCount(query);
    }

    protected abstract long doCount(LambdaQueryWrapper<T> query);

    protected abstract int doInsert(T t);

    protected abstract int doDelete(LambdaQueryWrapper<T> query);

    protected abstract int doUpdateById(T t);

    protected abstract List<T> doList(LambdaQueryWrapper<T> query);
}
