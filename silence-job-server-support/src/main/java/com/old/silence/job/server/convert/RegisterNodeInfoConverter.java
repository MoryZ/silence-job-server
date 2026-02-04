package com.old.silence.job.server.common.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.domain.model.ServerNode;


@Mapper
public interface RegisterNodeInfoConverter {
    RegisterNodeInfoConverter INSTANCE = Mappers.getMapper(RegisterNodeInfoConverter.class);

    RegisterNodeInfo toRegisterNodeInfo(ServerNode serverNode);
}
