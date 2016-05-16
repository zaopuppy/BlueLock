package com.example.zero.androidskeleton.concurrent;

import java.util.concurrent.*;

/**
 * Created by zero on 5/15/16.
 */
public class ThreadExecutor {

    interface ResultListener<T> {

        int OK = 0;

        int ERR = -1;

        int TIMEOUT = -2;

        void onResult(int code, T result);
    }

    private final ThreadPoolExecutor mExecutor;

    /**
     *
     * @param poolSize         thread number for this pool
     * @param keepAliveTime    alive time in millisecond
     */
    public ThreadExecutor(int poolSize, long keepAliveTime) {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(4);
        mExecutor = new ThreadPoolExecutor(
            poolSize, poolSize, keepAliveTime, TimeUnit.MILLISECONDS, queue);
    }

    public <T> Future<T> submit(final Callable<T> task) {
        return mExecutor.submit(task);
    }

    public void submit(Runnable task) {
        mExecutor.submit(task);
    }
}
