package com.old.silence.job.server.common.util;

import com.google.common.base.Splitter;
import cn.hutool.core.util.StrUtil;

import com.google.common.base.Splitter;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;

import java.util.List;


public class ClientInfoUtils {

    public static String generate(RegisterNodeInfo registerNodeInfo) {
        return registerNodeInfo.getHostId() + StrUtil.AT + registerNodeInfo.address();
    }

    public static String clientId(String clientInfo) {
        return split(clientInfo).get(0);
    }

    public static String address(String clientInfo) {
        return split(clientInfo).get(1);
    }

    public static List<String> split(String clientInfo) {
        return Splitter.on(StrUtil.AT).trimResults().splitToList(clientInfo);
    }

}
