package com.old.silence.job.server.infrastructure.persistence.dao;

import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.job.server.domain.model.NotifyConfig;

import java.math.BigInteger;


public interface NotifyConfigDao extends ProjectionMapperRepository<NotifyConfig, BigInteger> {
}
