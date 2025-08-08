package com.umc.hwaroak.infrastructure.transaction;

import org.springframework.transaction.support.TransactionSynchronization;

public abstract class CustomTransactionSynchronization implements TransactionSynchronization {
    @Override
    public int getOrder() {
        return TransactionSynchronization.super.getOrder();
    }

    @Override
    public void suspend() {
        TransactionSynchronization.super.suspend();
    }

    @Override
    public void resume() {
        TransactionSynchronization.super.resume();
    }

    @Override
    public void flush() {
        TransactionSynchronization.super.flush();
    }

    @Override
    public void savepoint(Object savepoint) {
        TransactionSynchronization.super.savepoint(savepoint);
    }

    @Override
    public void savepointRollback(Object savepoint) {
        TransactionSynchronization.super.savepointRollback(savepoint);
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        TransactionSynchronization.super.beforeCommit(readOnly);
    }

    @Override
    public void beforeCompletion() {
        TransactionSynchronization.super.beforeCompletion();
    }

    @Override
    public void afterCommit() {
        TransactionSynchronization.super.afterCommit();
    }

    @Override
    public void afterCompletion(int status) {
        TransactionSynchronization.super.afterCompletion(status);
    }
}
