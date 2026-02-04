package com.old.silence.job.server.enums;

import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.vo.DashboardLineResponseVO;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public enum DateTypeEnum {

    /**
     * 天（按小时）
     */
    DAY(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(voList,
                        DashboardLineResponseVO::getCreatedDate);
                int hourNow = Instant.now().atZone(ZoneId.systemDefault()).getHour();;
                for (int hourOffset = 0; hourOffset <= hourNow; hourOffset++) {
                    String createdDate = Instant.now().plus(hourOffset, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH"));
                    if (!responseVoMap.containsKey(createdDate)) {
                        voList.add(buildZeroedVoWithCreatedDate(createdDate));
                    }
                }
            },
            (startTime) -> startTime.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(),
            (endTime) -> endTime.atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999).toInstant()
    ),

    /**
     * 周
     */
    WEEK(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreatedDate);
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    String createdDate = Instant.now().minus(dayOffset, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (!responseVoMap.containsKey(createdDate)) {
                        voList.add(buildZeroedVoWithCreatedDate(createdDate));
                    }
                }
            },
            (startTime) -> startTime.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant().minus(7, ChronoUnit.DAYS),
            (endTime) -> endTime.atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999).toInstant()
    ),

    /**
     * 月
     */
    MONTH(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreatedDate);
                int lastDayOfMonth = Instant.now().with(TemporalAdjusters.lastDayOfMonth()).atZone(ZoneId.systemDefault()).getDayOfMonth();
                for (int dayOffset = 0; dayOffset < lastDayOfMonth; dayOffset++) {
                    String createdDate = LocalDate.now().minusDays(dayOffset).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (!responseVoMap.containsKey(createdDate)) {
                        voList.add(buildZeroedVoWithCreatedDate(createdDate));
                    }
                }
            },
            (startTime) -> startTime.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant().minus(1, ChronoUnit.MONTHS),
            (endTime) -> endTime.atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999).toInstant()
    ),

    /**
     * 年
     */
    YEAR(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreatedDate);
                for (int monthOffset = 0; monthOffset < 12; monthOffset++) {
                    String createdDate = Instant.now().minus(monthOffset, ChronoUnit.MONTHS).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    if (!responseVoMap.containsKey(createdDate)) {
                        voList.add(buildZeroedVoWithCreatedDate(createdDate));
                    }
                }
            },
            (startTime) -> Instant.now().atZone(ZoneId.systemDefault()).withDayOfYear(1).toInstant(),
            (endTime) -> Instant.parse(Instant.now().atZone(ZoneId.systemDefault()).getYear() + "-12-31T23:59:59.999Z")
    ),

    /**
     * 其他类型
     */
    OTHERS(
            voList -> {
            },
            (startTime) -> startTime.atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(),
            (endTime) -> endTime.atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999).toInstant());

    DateTypeEnum(Consumer<List<DashboardLineResponseVO>> consumer, Function<Instant, Instant> startTime, Function<Instant, Instant> endTime) {
        this.consumer = consumer;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private Consumer<List<DashboardLineResponseVO>> consumer;
    private Function<Instant, Instant> startTime;
    private Function<Instant, Instant> endTime;

    private static DashboardLineResponseVO buildZeroedVoWithCreatedDate(String createdDate) {
        var dashboardLineResponseVO = new DashboardLineResponseVO();
        dashboardLineResponseVO.setTotal(0L);
        dashboardLineResponseVO.setTotalNum(0L);
        dashboardLineResponseVO.setFail(0L);
        dashboardLineResponseVO.setFailNum(0L);
        dashboardLineResponseVO.setMaxCountNum(0L);
        dashboardLineResponseVO.setRunningNum(0L);
        dashboardLineResponseVO.setSuccess(0L);
        dashboardLineResponseVO.setSuccessNum(0L);
        dashboardLineResponseVO.setSuspendNum(0L);
        dashboardLineResponseVO.setStop(0L);
        dashboardLineResponseVO.setCancel(0L);
        dashboardLineResponseVO.setCreatedDate(createdDate);
        return dashboardLineResponseVO;

    }

    public Consumer<List<DashboardLineResponseVO>> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<List<DashboardLineResponseVO>> consumer) {
        this.consumer = consumer;
    }

    public Function<Instant, Instant> getStartTime() {
        return startTime;
    }

    public void setStartTime(Function<Instant, Instant> startTime) {
        this.startTime = startTime;
    }

    public Function<Instant, Instant> getEndTime() {
        return endTime;
    }

    public void setEndTime(Function<Instant, Instant> endTime) {
        this.endTime = endTime;
    }
}
