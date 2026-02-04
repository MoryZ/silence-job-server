package com.old.silence.job.server.domain.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.domain.model.JobExecutor;
import com.old.silence.job.server.infrastructure.persistence.dao.JobExecutorDao;

/**
 * @author moryzang
 */
@Service
public class JobExecutorService {

    private final JobExecutorDao jobExecutorDao;

    public JobExecutorService(JobExecutorDao jobExecutorDao) {
        this.jobExecutorDao = jobExecutorDao;
    }

    public Page<JobExecutor> queryPage(Page<JobExecutor> page, QueryWrapper<JobExecutor> queryWrapper) {

        return jobExecutorDao.selectPage(page, queryWrapper);

    }

    public JobExecutor findById(BigInteger id) {
        return jobExecutorDao.selectById(id);
    }

    public Set<String> getJobExecutorList(QueryWrapper<JobExecutor> queryWrapper) {
        return CollectionUtils.transformToSet(jobExecutorDao.selectList(queryWrapper), JobExecutor::getExecutorInfo);
    }

    @Transactional
    public Boolean deleteJobExecutorByIds(Set<Long> ids) {
        Assert.isTrue(ids.size() == jobExecutorDao.delete(
                new LambdaQueryWrapper<JobExecutor>()
                        .in(JobExecutor::getId, ids)
        ), CommonErrors.INVALID_PARAMETER::createException);
        return Boolean.TRUE;
    }
}
