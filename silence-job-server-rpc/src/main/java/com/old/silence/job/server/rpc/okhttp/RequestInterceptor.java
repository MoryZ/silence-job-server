package com.old.silence.job.server.common.rpc.okhttp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class RequestInterceptor implements Interceptor {

    public static final String TIMEOUT_TIME = "executorTimeout";

    @NotNull
    @Override
    public Response intercept(@NotNull final Chain chain) throws IOException {
        Request request = chain.request();
        String timeoutTime = request.header(TIMEOUT_TIME);
        if (StrUtil.isNotBlank(timeoutTime)) {
            int timeout = Integer.parseInt(timeoutTime);
            if (timeout <= 0) {
                return chain.proceed(request);
            }

            return chain
                    .withReadTimeout(timeout, TimeUnit.SECONDS)
                    .withConnectTimeout(timeout, TimeUnit.SECONDS)
                    .withWriteTimeout(timeout, TimeUnit.SECONDS)
                    .proceed(chain.request());
        }

        return chain.proceed(request);

    }
}
