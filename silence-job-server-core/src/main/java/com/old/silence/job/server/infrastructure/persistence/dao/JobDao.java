package com.old.silence.job.server.infrastructure.persistence.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.job.server.domain.model.Job;

import java.math.BigInteger;
import java.util.List;


public interface JobDao extends ProjectionMapperRepository<Job, BigInteger> {

    int updateBatchNextTriggerAtById(@Param("list") List<Job> list);

    @Update("update sj_job set job_status=#{status} where id=#{id} ")
    int updateStatusById(boolean status, BigInteger id);
}
