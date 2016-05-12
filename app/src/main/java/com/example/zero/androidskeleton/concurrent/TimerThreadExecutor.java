package com.example.zero.androidskeleton.concurrent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zero on 5/10/16.
 */
public class TimerThreadExecutor {

    public interface ResultListener<T> {

        int OK = 0;

        int ERR = -1;

        int TIMEOUT = -2;

        void onResult(int code, T result);
    }

    private class Notifier<T> implements ResultListener<T> {

        private final ResultListener<T> mListener;

        private final AtomicBoolean mNotified = new AtomicBoolean(false);

        public Notifier(ResultListener<T> l) {
            mListener = l;
        }

        @Override
        public void onResult(int code, T result) {
            if (mNotified.compareAndSet(false, true)) {
                if (mListener != null) {
                    mListener.onResult(code, result);
                }
            }
        }
    }

    private final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<>(4);

    private final ThreadPoolExecutor mExecutor;

    private final Timer mTimer;

    /**
     *
     * @param poolSize         thread number for this pool
     * @param keepAliveTime    alive time in millisecond
     * @param timer            timer
     */
    public TimerThreadExecutor(int poolSize, long keepAliveTime, Timer timer) {
        mExecutor = new ThreadPoolExecutor(
            poolSize, poolSize, keepAliveTime, TimeUnit.MILLISECONDS, mWorkQueue);
        mTimer = timer;
    }

    public TimerThreadExecutor(int poolSize, long keepAliveTime) {
        this(poolSize, keepAliveTime, new Timer("thread-executor-timer"));
    }

    public <T> Future<T> submit(final Callable<T> task, final ResultListener<T> listener, final long timeout) {

        final Notifier<T> notifier = new Notifier<>(listener);

        final Future<T> future = mExecutor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                int code = ResultListener.OK;
                T result = null;
                try {
                    result = task.call();
                    return result;
                } catch (Exception e) {
                    code = ResultListener.ERR;
                } finally {
                    if (listener != null) {
                        listener.onResult(code, result);
                    }
                }
                return null;
            }
        });

        if (timeout >= 0) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    future.cancel(true);
                    notifier.onResult(ResultListener.TIMEOUT, null);
                }
            }, timeout);
        }

        return future;
    }

    public void submit(Runnable task) {
        mExecutor.submit(task);
    }
}
