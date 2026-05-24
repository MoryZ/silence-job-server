package com.old.silence.job.server.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Repository Auto Configuration
 * Automatically scans and registers MyBatis Mapper interfaces
 */
@AutoConfiguration
@MapperScan(
    value = "com.old.silence.job.server.infrastructure.persistence.dao",
    annotationClass = Mapper.class
)
public class RepositoryAutoConfiguration {

}
