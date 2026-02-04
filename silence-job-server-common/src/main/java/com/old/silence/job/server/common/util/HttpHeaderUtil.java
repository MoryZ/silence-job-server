package com.old.silence.job.server.common.util;

import io.netty.handler.codec.http.HttpHeaders;
import com.old.silence.job.common.enums.HeadersEnum;


public final class HttpHeaderUtil {

    public static String getGroupName(HttpHeaders headers) {
        return headers.getAsString(HeadersEnum.GROUP_NAME.getKey());
    }

    public static String getNamespace(HttpHeaders headers) {
        return headers.getAsString(HeadersEnum.NAMESPACE.getKey());
    }

}
