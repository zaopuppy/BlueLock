package com.example.zero.androidskeleton.bt;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zero on 4/16/16.
 */
public class Promise {

    //public static final int STATE_CREATED   = 0;

    public static final int STATE_PENDING   = 1;

    public static final int STATE_FULFILLED = 2;

    public static final int STATE_REJECTED  = 3;

    public interface Work {
        void exec(Promise promise);
    }

    public static class SuccessPromise extends Promise {
        @Override
        public Promise then(Work onFulfilled, Work onRejected) {
            // FIXME: wrong implementation
            return null;
        }
    }

    private final AtomicInteger mState = new AtomicInteger(STATE_PENDING);

    private Work mFulfillHandler = null;
    private Work mRejectHandler = null;
    private final Promise mPromise = new Promise();

    public Promise() {}

    public Promise then(Work onFulfilled, Work onRejected) {
        mFulfillHandler = onFulfilled;
        mRejectHandler = onRejected;
        return mPromise;
    }

    public void notifyFulfilled() {
        if (!mState.compareAndSet(STATE_PENDING, STATE_FULFILLED)) {
            throw new IllegalStateException("expected=STATE_PENDING, actual=" + mState.get());
        }

        if (mFulfillHandler != null) {
            mFulfillHandler.exec(mPromise);
        }
    }

    public void notifyRejected() {
        if (!mState.compareAndSet(STATE_PENDING, STATE_REJECTED)) {
            throw new IllegalStateException("expected=STATE_PENDING, actual=" + mState.get());
        }

        if (mRejectHandler != null) {
            mRejectHandler.exec(mPromise);
        }
    }
}
