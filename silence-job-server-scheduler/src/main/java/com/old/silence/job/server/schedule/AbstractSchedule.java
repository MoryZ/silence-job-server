package com.old.silence.job.server.common.schedule;

import cn.hutool.core.lang.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Schedule;
import com.old.silence.job.server.common.lock.LockBuilder;
import com.old.silence.job.server.common.lock.LockManager;
import com.old.silence.job.server.common.lock.LockProvider;

import java.time.Duration;



public abstract class AbstractSchedule implements Schedule {

    @Autowired
    @Qualifier("scheduledExecutorService")
    protected TaskScheduler taskScheduler;

    @Override
    public void execute() {

        String lockName = lockName();
        String lockAtMost = lockAtMost();
        String lockAtLeast = lockAtLeast();
        Assert.notBlank(lockAtMost, () -> CommonErrors.INVALID_PARAMETER.createException("lockAtLeast can not be null."));
        Assert.notBlank(lockAtLeast, () -> CommonErrors.INVALID_PARAMETER.createException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> CommonErrors.INVALID_PARAMETER.createException("lockName can not be null."));

        LockProvider lockProvider = LockBuilder.newBuilder()
                .withResident(lockName)
                .build();
        boolean lock = false;
        try {
            lock = lockProvider.lock(Duration.parse(lockAtLeast), Duration.parse(lockAtMost));
            if (lock) {
                doExecute();
            }
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error(this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock();
            } else {
                LockManager.clear();
            }
        }

    }

    protected abstract void doExecute();

    protected abstract String lockName();

    protected abstract String lockAtMost();

    protected abstract String lockAtLeast();


}
