package com.umc.hwaroak.lock;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * JDBC Template를 통해 lock 실시
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class DiaryLockTemplate {

    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";

    public <T> T executeWithLock(String lockName, Supplier<T> action) {
        try {
            getLock(lockName);
            return action.get();
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.TRANSACTION_FAILED);
        } finally {
            releaseLock(lockName); // 데드락 방지
        }
    }

    private final EntityManager entityManager;

    private void getLock(String lockName) {
        Object result = entityManager.createNativeQuery(GET_LOCK)
                .setParameter(1, lockName)
                .setParameter(2, 10) // 무한대기 방지
                .getSingleResult();

        if (result instanceof Integer)
        checkResult((Long) result, lockName, "GET_LOCK");
    }

    private void releaseLock(String lockName) {
        Object result = entityManager.createNativeQuery(RELEASE_LOCK)
                .setParameter(1, lockName)
                .getSingleResult();

        checkResult((Long) result, lockName, "RELEASE_LOCK");
    }

    // logging method
    private void checkResult(Long result, String lockName, String type) {
        if (result == null) {
            log.error("쿼리 실행 결과가 없습니다. type = {}, lockName = {}", type, lockName);
            throw new GeneralException(ErrorCode.TRANSACTION_FAILED);
        }
        if (result != 1) {
            log.error("쿼리 실행 결괏값이 1이 아닙니다. type = {}, lockName = {}", type, lockName);
            throw new GeneralException(ErrorCode.TRANSACTION_FAILED);
        }
    }
}
