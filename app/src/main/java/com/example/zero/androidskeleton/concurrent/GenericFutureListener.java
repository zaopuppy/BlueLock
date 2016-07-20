package com.example.zero.androidskeleton.concurrent;

/**
 * Created by zhaoyi on 7/18/16.
 */
public interface GenericFutureListener<T> {
    void onResult(T result);
}
