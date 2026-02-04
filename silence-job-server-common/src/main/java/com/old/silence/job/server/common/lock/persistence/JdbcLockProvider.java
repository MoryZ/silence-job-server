package com.old.silence.job.server.common.lock.persistence;


import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.cache.CacheLockRecord;
import com.old.silence.job.server.common.dto.LockConfig;
import com.old.silence.job.server.common.register.ServerRegister;
import com.old.silence.job.server.domain.model.DistributedLock;
import com.old.silence.job.server.infrastructure.persistence.dao.DistributedLockDao;

import java.time.Instant;

/**
 * 基于DB实现的分布式锁
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JdbcLockProvider implements LockStorage, Lifecycle {

    private final DistributedLockDao distributedLockDao;
    private final PlatformTransactionManager platformTransactionManager;

    public JdbcLockProvider(DistributedLockDao distributedLockDao, PlatformTransactionManager platformTransactionManager) {
        this.distributedLockDao = distributedLockDao;
        this.platformTransactionManager = platformTransactionManager;
    }


    @Override
    public boolean createLock(LockConfig lockConfig) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            try {
                Instant now = lockConfig.getcreatedDate();
                DistributedLock distributedLock = new DistributedLock();
                distributedLock.setName(lockConfig.getLockName());
                distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                distributedLock.setLockedAt(now);
                distributedLock.setLockUntil(lockConfig.getLockAtMost());
                distributedLock.setCreatedDate(now);
                distributedLock.setUpdatedDate(now);
                return distributedLockDao.insert(distributedLock) > 0;
            } catch (DuplicateKeyException | ConcurrencyFailureException | TransactionSystemException e) {
                return false;
            } catch (DataIntegrityViolationException | BadSqlGrammarException | UncategorizedSQLException e) {
                SilenceJobLog.LOCAL.debug("Unexpected exception. lockName:[{}]", lockConfig.getLockName(), e);
                return false;
            }
        }));

    }

    @Override
    public boolean renewal(LockConfig lockConfig) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            Instant now = lockConfig.getcreatedDate();
            DistributedLock distributedLock = new DistributedLock();
            distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
            distributedLock.setLockedAt(now);
            distributedLock.setLockUntil(lockConfig.getLockAtMost());
            distributedLock.setName(lockConfig.getLockName());
            try {
                return distributedLockDao.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockConfig.getLockName())
                        .le(DistributedLock::getLockUntil, now)) > 0;
            } catch (ConcurrencyFailureException | DataIntegrityViolationException | TransactionSystemException |
                     UncategorizedSQLException e) {
                return false;
            }
        }));
    }

    @Override
    public boolean releaseLockWithDelete(String lockName) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    return distributedLockDao.delete(new LambdaUpdateWrapper<DistributedLock>()
                            .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    SilenceJobLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
                } finally {
                    CacheLockRecord.remove(lockName);
                }
            }
            return false;
        }));

    }

    @Override
    public boolean releaseLockWithUpdate(String lockName, Instant lockAtLeast) {
        Instant now = Instant.now();
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    DistributedLock distributedLock = new DistributedLock();
                    distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                    distributedLock.setLockUntil(now.isBefore(lockAtLeast) ? lockAtLeast : now);
                    return distributedLockDao.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                            .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    SilenceJobLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
                }
            }

            return false;
        }));
    }

    @Override
    public void start() {
        LockStorageFactory.registerLockStorage(this);
    }

    @Override
    public void close() {
        // 删除当前节点获取的锁记录
        distributedLockDao.delete(new LambdaUpdateWrapper<DistributedLock>()
                .eq(DistributedLock::getLockedBy, ServerRegister.CURRENT_CID));
    }

    private Boolean notSupportedTransaction(TransactionCallback<Boolean> action) {
        TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return template.execute(action);
    }
}
