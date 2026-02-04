package com.old.silence.job.server.domain.service.task;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.job.server.domain.service.Access;

/**
 * 获取重试数据通道
 *
 */
public interface TaskAccess<T> extends Access {

    List<T> list(LambdaQueryWrapper<T> query);

    T one(LambdaQueryWrapper<T> query);

    int update(T t, LambdaUpdateWrapper<T> query);

    int updateById(T t);

    int delete(LambdaQueryWrapper<T> query);

    int insert(T t);

    int insertBatch(List<T> list);

    Page<T> listPage(Page<T> iPage, LambdaQueryWrapper<T> query);

    long count(LambdaQueryWrapper<T> query);

}
