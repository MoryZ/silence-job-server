package com.old.silence.job.server.domain.service;


import com.old.silence.job.common.enums.OperationTypeEnum;

/**
 * @author moryzang
 */
public interface Access {

    boolean supports(OperationTypeEnum operationType);
}
