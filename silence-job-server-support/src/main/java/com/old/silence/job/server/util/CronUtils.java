package com.old.silence.job.server.common.util;

import cn.hutool.core.lang.Assert;

import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.common.util.CronExpression;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class CronUtils {

    public static List<Instant> getExecuteTimeByCron(String cron, int nums) {

        List<Instant> list = new ArrayList<>();
        Instant now = Instant.now();
        for (int i = 0; i < nums; i++) {
            Date nextValidTime;
            try {
                ZonedDateTime zdt = now.atZone(ZoneOffset.ofHours(8));
                nextValidTime = new CronExpression(cron).getNextValidTimeAfter(Date.from(zdt.toInstant()));
                if (Objects.isNull(nextValidTime)) {
                    continue;
                }
                now = Instant.ofEpochSecond(nextValidTime.getTime() / 1000);
                list.add(now);
            } catch (ParseException ignored) {
            }
        }

        return list;
    }

    public static long getExecuteInterval(String cron) {
        List<Instant> executeTimeByCron = getExecuteTimeByCron(cron, 2);
        Assert.isTrue(!executeTimeByCron.isEmpty(), () -> CommonErrors.INVALID_PARAMETER.createException("表达式解析有误.[{}]", cron));
        Assert.isTrue(executeTimeByCron.size() == 2, () -> CommonErrors.INVALID_PARAMETER.createException("表达式必须支持多次执行.[{}]", cron));
        Instant first = executeTimeByCron.get(0);
        Instant second = executeTimeByCron.get(1);
        Duration duration = Duration.between(first, second);
        return duration.toMillis();
    }

}
