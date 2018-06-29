package edu.coursera.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import static edu.rice.pcdp.PCDP.isolated;

/**
 * A thread-safe transaction implementation using object-based isolation.
 */
public final class BankTransactionsUsingObjectIsolation
        extends ThreadSafeBankTransaction {

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * {@inheritDoc}
     */
    @Override
    public void issueTransfer(final int amount, final Account src,
            final Account dst) {

        if (lock.tryLock()) {
            src.performTransfer(amount, dst);
            lock.unlock();
        }
    }
}
