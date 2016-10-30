package com.example.zero.androidskeleton.concurrent;

import java.util.concurrent.*;

/**
 * Created by zero on 5/15/16.
 */
public class ThreadExecutor extends ThreadPoolExecutor {

    interface ResultListener<T> {

        int OK = 0;

        int ERR = -1;

        int TIMEOUT = -2;

        void onResult(int code, T result);
    }

    /**
     *
     * @param poolSize         thread number for this pool
     * @param keepAliveTime    alive time in millisecond
     */
    public ThreadExecutor(int poolSize, long keepAliveTime, int queueSize) {
        super(poolSize, poolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueSize));
    }

    public <T> Future<T> submit(final Callable<T> task, final ResultListener<T> listener) {
        return super.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                int code = ResultListener.ERR;
                T result = null;
                try {
                    result = task.call();
                    code = ResultListener.OK;
                } catch (Exception e) {
                    result = null;
                    code = ResultListener.ERR;
                } finally {
                    if (listener != null) {
                        listener.onResult(code, result);
                    }
                }
                return result;
            }
        });
    }
}

