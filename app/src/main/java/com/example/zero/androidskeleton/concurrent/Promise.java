package com.example.zero.androidskeleton.concurrent;


import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Promise<T> extends FutureTask<Promise.Result<T>> {

    private static final String TAG = "PromiseT";

    /**
     * failed in unknown reason
     */
    public static final int CODE_FAIL           = -1;

    /**
     * success execution, of course
     */
    public static final int CODE_SUCCESS        =  0;

    /**
     * exception when we are executing or waiting for result
     */
    public static final int CODE_EXCEPTION      =  1;

    /**
     * time in execution or waiting
     */
    public static final int CODE_TIMEOUT        =  2;

    /**
     * user cancelled
     */
    public static final int CODE_CANCEL         =  3;

    // 项目默认线程池
    private static final Executor DEFAULT_EXECUTOR = new ThreadExecutor(5, 10, 100/*, "promise"*/);

    /**
     * the result combines e code and result
     *
     */
    public static class Result<T> {
        private final int code;
        private final T result;

        public Result(int code, T result) {
            this.code = code;
            this.result = result;
        }

        public int getCode() {
            return code;
        }

        public T getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "code=" + code +
                    ", result=" + result +
                    '}';
        }
    }

    /**
     * 使用指定的执行器, 异步执行指定的任务, 并返回代表此异步任务的Promise
     *
     * @param callable    callable
     * @param executor    executor
     * @return promise which represent future result
     */
    public static <U> Promise<U> supplyAsync(Callable<U> callable, Executor executor) {
        return supplyAsync(callable, executor, null, -1);
    }

    /**
     * 使用指定的执行器, 异步执行指定的任务, 并返回代表此异步任务的Promise
     *
     * @param callable    callable
     * @param executor    executor
     * @param timeout     timeout in millisecond
     * @return promise which represent future result
     */
    public static <U> Promise<U> supplyAsync(
            Callable<U> callable, final Executor executor, final Timer timer, final long timeout) {
        final Promise<U> promise = new Promise<U>(callable, executor);
        executor.execute(promise);

        if (timer != null && timeout >= 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    promise.set(new Result<U>(CODE_TIMEOUT, null));
                }
            }, timeout);
        }

        return promise;
    }

    // TODO
    // public static Promise<Void> runAsync(Runnable runnable, Executor executor) {}

    // TODO
    // public static CompletableFuture<Void< allOf(CompletableFuture<?<... cfs)
    // public static CompletableFuture<Object< anyOf(CompletableFuture<?<... cfs)

    private final ConcurrentLinkedQueue<Consumer<Result<T>>> consumerList = new ConcurrentLinkedQueue<>();
    private volatile boolean listenerHandled = false;
    private final Executor mExecutor;

    private static class ExceptionCallable<U> implements Callable<Result<U>> {
        private final Callable<U> callable;

        ExceptionCallable(Callable<U> callable) {
            this.callable = callable;
        }

        @Override
        public Result<U> call() throws Exception {
            try {
                return new Result<>(CODE_SUCCESS, callable.call());
            } catch (Exception e) {
                Log.w(TAG, "exception in callable: " + e.getMessage());
                return new Result<>(CODE_EXCEPTION, null);
            }
        }
    }

    private Promise(final Callable<T> callable, Executor executor) {
        super(new ExceptionCallable<>(callable));
        mExecutor = (executor == null) ? DEFAULT_EXECUTOR: executor;
    }

    /**
     * default constructor, use default executor and default execution black (which does nothing).
     */
    public Promise() {
        this(null);
    }

    /**
     * give a executor for listener calling and task executing.
     *
     * @param executor    the executor we'll use
     */
    public Promise(Executor executor) {
        this(new Callable<T>() {
            @Override
            public T call() {
                // default behavior, do nothing
                return null;
            }
        }, executor);
    }

    /**
     * 通知到Promise, 表明异步任务已经完成, 该接口仅可被成功调用一次, 如果有多次调用, 则以第一次调用的结果为准
     *
     * @param code      任务执行结果码
     * @param result    任务执行结果
     */
    public void complete(int code, T result) {
        set(new Result<>(code, result));
    }

    public void complete(Result<T> result) {
        set(result);
    }

    /**
     * 获取异步任务执行的结果, 如果异步任务尚未执行完成则阻塞当前线程
     *
     * @return instance of {@link Result}, which is never be null.
     */
    public Result<T> result() {
        try {
            return get();
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException while getting: " + e.getMessage());
        } catch (ExecutionException e) {
            Log.w(TAG, "ExecutionException while getting: " + e.getMessage());
        }
        return new Result<>(CODE_EXCEPTION, null);
    }

    /**
     * 获取异步任务执行的结果, 如果异步任务尚未执行完成则阻塞当前线程
     *
     * @return instance of {@link Result}, which is never be null.
     */
    public Result<T> result(long timeout) {
        try {
            try {
                return get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            return new Result<>(CODE_TIMEOUT, null);
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException while getting: " + e.getMessage());
        } catch (ExecutionException e) {
            Log.w(TAG, "ExecutionException while getting: " + e.getMessage());
        }
        return new Result<>(CODE_EXCEPTION, null);
    }

    /**
     * 注册指定的Consumer, 异步任务执行完成后, 将结果提供给该Consumer, 并返回代表该Consumer任务执行的Promise
     *
     * TODO: exception handling
     *
     * @param consumer    the listen will be added
     */
    public Promise<Void> thenAccept(final Consumer<Result<T>> consumer) {
        final Promise<Void> promise = new Promise<>(mExecutor);
        // check race-condition
        if (listenerHandled) {
            consumer.accept(result());
            promise.complete(new Result<Void>(CODE_SUCCESS, null));
        } else {
            consumerList.add(new Consumer<Result<T>>() {
                @Override
                public void accept(Result<T> result) {
                    consumer.accept(result);
                    promise.complete(new Result<Void>(CODE_SUCCESS, null));
                }
            });
        }
        return promise;
    }

    public Promise<Void> thenAcceptAsync(final Consumer<Result<T>> consumer) {
        return thenAcceptAsync(consumer, mExecutor);
    }

    public Promise<Void> thenAcceptAsync(final Consumer<Result<T>> consumer, Executor executor) {
        final Promise<Void> promise = new Promise<>(executor);
        // check race-condition
        if (listenerHandled) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    consumer.accept(result());
                    promise.complete(new Result<Void>(CODE_SUCCESS, null));
                }
            });
        } else {
            consumerList.add(new Consumer<Result<T>>() {
                @Override
                public void accept(Result<T> result) {
                    consumer.accept(result);
                    promise.complete(new Result<Void>(CODE_SUCCESS, null));
                }
            });
        }
        return promise;
    }

    @Override
    protected void done() {
        // mark we already handled all listeners, no new listener should be added.
        listenerHandled = true;

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Result<T> result = result();
                for (Consumer<Result<T>> c: consumerList) {
                    c.accept(result);
                }
                consumerList.clear();
            }
        });
    }

    /**
     * 注册指定的Function, 异步任务执行完成后, 将结果提供给该Function, 并返回代表该Function任务执行的Promise
     *
     * @param fn     指定的Function对象
     * @return Promise
     */
    public <U> Promise<U> thenApply(final Function<Result<T>, Result<U>> fn) {
        final Promise<U> promise = new Promise<U>(mExecutor);
        // check race-condition
        if (listenerHandled) {
            promise.complete(fn.apply(result()));
        } else {
            consumerList.add(new Consumer<Result<T>>() {
                @Override
                public void accept(Result<T> result) {
                    promise.complete(fn.apply(result()));
                }
            });
        }
        return promise;
    }

    public <U> Promise<U> thenApplyAsync(final Function<Result<T>, Result<U>> fn) {
        return thenApplyAsync(fn, mExecutor);
    }

    public <U> Promise<U> thenApplyAsync(final Function<Result<T>, Result<U>> fn, Executor executor) {
        final Promise<U> promise = new Promise<U>(executor);
        // check race-condition
        if (listenerHandled) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    promise.complete(fn.apply(result()));
                }
            });
        } else {
            consumerList.add(new Consumer<Result<T>>() {
                @Override
                public void accept(Result<T> result) {
                    promise.complete(fn.apply(result()));
                }
            });
        }
        return promise;
    }

    /**
     * 注册指定的Function(返回结果必须是Promise), 异步任务执行完成后, 将结果提供给该Function
     * 并返回代表该Function返回的异步任务的结果(不是Promise)
     *
     * @param fn     function
     * @return composed promise
     */
    public <U> Promise<U> thenCompose(final Function<Result<T>, Promise<U>> fn) {
        final Promise<U> promise = new Promise<U>(mExecutor);
        thenAccept(new Consumer<Result<T>>() {
            @Override
            public void accept(Result<T> result) {
                Promise<U> nextPromise = fn.apply(result);
                nextPromise.thenAccept(new Consumer<Result<U>>() {
                    @Override
                    public void accept(Result<U> result) {
                        promise.complete(result);
                    }
                });
            }
        });
        return promise;
    }

    public <U> Promise<U> thenComposeAsync(final Function<Result<T>, Promise<U>> fn) {
        return thenComposeAsync(fn, mExecutor);
    }

    public <U> Promise<U> thenComposeAsync(final Function<Result<T>, Promise<U>> fn, Executor executor) {
        final Promise<U> promise = new Promise<>(executor);
        thenAcceptAsync(new Consumer<Result<T>>() {
            @Override
            public void accept(Result<T> result) {
                Promise<U> nextPromise = fn.apply(result);
                nextPromise.thenAccept(new Consumer<Result<U>>() {
                    @Override
                    public void accept(Result<U> result) {
                        promise.complete(result);
                    }
                });
            }
        }, executor);
        return promise;
    }

    /**
     * 组合两个异步任务(Promise)的结果, 两个异步任务都执行完成后, 将结果提供给指定的Function,
     * 并返回代表该Function任务执行的Promise
     *
     * @param other    另一个Promise
     * @param fn       处理函数
     * @return 函数处理的结果
     */
    public <U, V> Promise<V> thenCombine(final Promise<U> other,
                                         final BiFunction<Result<T>, Result<U>, Result<V>> fn) {
        final Promise<V> promise = new Promise<>(mExecutor);

        thenAccept(new Consumer<Result<T>>() {
            @Override
            public void accept(final Result<T> result1) {
                other.thenAccept(new Consumer<Result<U>>() {
                    @Override
                    public void accept(Result<U> result2) {
                        promise.complete(fn.apply(result1, result2));
                    }
                });
            }
        });

        return promise;
    }

    public <U, V> Promise<V> thenCombineAsync(final Promise<U> other,
                                              final BiFunction<Result<T>, Result<U>, Result<V>> fn) {
        return thenCombineAsync(other, fn, mExecutor);
    }

    public <U, V> Promise<V> thenCombineAsync(final Promise<U> other,
                                              final BiFunction<Result<T>, Result<U>, Result<V>> fn,
                                              final Executor executor) {
        final Promise<V> promise = new Promise<>(executor);

        thenAcceptAsync(new Consumer<Result<T>>() {
            @Override
            public void accept(final Result<T> result1) {
                other.thenAcceptAsync(new Consumer<Result<U>>() {
                    @Override
                    public void accept(final Result<U> result2) {
                        promise.complete(fn.apply(result1, result2));
                    }
                }, executor);
            }
        });

        return promise;
    }

    private Consumer<Result<T>> getAcceptEitherConsumer(
            final Promise<Void> promise, final Consumer<Result<T>> consumer) {
        final int PROMISE_COUNT = 2;
        final AtomicInteger completeTime = new AtomicInteger(0);
        return new Consumer<Result<T>>() {
            @Override
            public void accept(Result<T> result) {
                if (promise.isDone()) {
                    return;
                }
                if (result.code != CODE_SUCCESS) {
                    // 如果本次执行结果失败, 检查是否已经完成过
                    // 如果已经完成过, 说明之前已经执行成功, 直接返回即可
                    // 如果没有完成过, 说明另外一个任务可能尚未执行完毕, 也可能执行完毕了但是执行结果为失败
                    // 对完成次数+1, 如果等于2, 说明这是第二个完成的任务, 直接执行
                    if (completeTime.addAndGet(1) == PROMISE_COUNT) {
                        consumer.accept(result);
                        promise.complete(new Result<Void>(CODE_SUCCESS, null));
                    }
                    return;
                }
                if (completeTime.addAndGet(PROMISE_COUNT) >= PROMISE_COUNT) {
                    consumer.accept(result);
                    promise.complete(new Result<Void>(CODE_SUCCESS, null));
                }
            }
        };
    }

    /**
     * 应用给先成功完成的一个异步任务
     *
     * 由于这个Promise的result是包括result code的, 所以即使有结果, 也不代表我们可以立刻接受它
     *
     * 1) 如果两个异步任务都成功, 那么应该成功完成第一个先完成的
     * 2) 如果两个仅有一个成功, 那么结果应该是成功的那个
     * 3) 如果两个都失败, 那么结果应该是最后失败的那个
     *
     * @param other          另一个异步任务
     * @param consumer       处理函数
     * @return 最终选择的执行结果
     */
    public Promise<Void> acceptEither(Promise<T> other, final Consumer<Result<T>> consumer) {
        final Promise<Void> promise = new Promise<>(mExecutor);

        final Consumer<Result<T>> interConsumer = getAcceptEitherConsumer(promise, consumer);

        this.thenAccept(interConsumer);

        other.thenAccept(interConsumer);

        return promise;
    }

    public Promise<Void> acceptEitherAsync(Promise<T> other, final Consumer<Result<T>> consumer, Executor executor) {
        final Promise<Void> promise = new Promise<>(executor);

        final Consumer<Result<T>> interConsumer = getAcceptEitherConsumer(promise, consumer);

        this.thenAcceptAsync(interConsumer, executor);

        other.thenAcceptAsync(interConsumer, executor);

        return promise;
    }

    private <U> Consumer<Result<T>> getApplyToEitherConsumer(
            final Promise<U> promise, final Function<Result<T>, Result<U>> fn) {
        final int PROMISE_COUNT = 2;
        final AtomicInteger completeTime = new AtomicInteger(0);
        return new Consumer<Result<T>>() {
            @Override
            public void accept(Result<T> result) {
                if (promise.isDone()) {
                    return;
                }
                if (result.code != CODE_SUCCESS) {
                    // 如果本次执行结果失败, 检查是否已经完成过
                    // 如果已经完成过, 说明之前已经执行成功, 直接返回即可
                    // 如果没有完成过, 说明另外一个任务可能尚未执行完毕, 也可能执行完毕了但是执行结果为失败
                    // 对完成次数+1, 如果等于2, 说明这是第二个完成的任务, 直接执行
                    if (completeTime.addAndGet(1) == PROMISE_COUNT) {
                        promise.complete(fn.apply(result));
                    }
                    return;
                }
                if (completeTime.addAndGet(PROMISE_COUNT) >= PROMISE_COUNT) {
                    promise.complete(fn.apply(result));
                }
            }
        };
    }

    /**
     * 应用给先成功完成的一个异步任务
     *
     * 由于这个Promise的result是包括result code的, 所以即使有结果, 也不代表我们可以立刻接受它
     *
     * 1) 如果两个异步任务都成功, 那么应该成功完成第一个先完成的
     * 2) 如果两个仅有一个成功, 那么结果应该是成功的那个
     * 3) 如果两个都失败, 那么结果应该是最后失败的那个
     *
     * @param other    另一个异步任务
     * @param fn       处理函数
     * @return 最终选择的执行结果
     */
    public <U> Promise<U> applyToEither(Promise<T> other, Function<Result<T>, Result<U>> fn) {
        final Promise<U> promise = new Promise<>();

        final Consumer<Result<T>> interConsumer = getApplyToEitherConsumer(promise, fn);

        this.thenAccept(interConsumer);

        other.thenAccept(interConsumer);

        return promise;
    }

    public <U> Promise<U> applyToEitherAsync(Promise<T> other, final Function<Result<T>, Result<U>> fn, Executor executor) {
        final Promise<U> promise = new Promise<>(executor);

        final Consumer<Result<T>> interConsumer = getApplyToEitherConsumer(promise, fn);

        this.thenAcceptAsync(interConsumer);

        other.thenAcceptAsync(interConsumer);

        return promise;
    }

    // TODO
    // public <U> Promise<Void> thenAcceptBoth(Promise<? extends U> other, BiConsumer<? super T,? super U> block)
    // public CompletableFuture<Void> runAfterBoth(CompletableFuture<?> other, Runnable action)
    // public CompletableFuture<Void> runAfterEither(CompletableFuture<?> other, Runnable action)

}
