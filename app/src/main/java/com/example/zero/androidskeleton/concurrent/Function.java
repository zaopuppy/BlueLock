package com.example.zero.androidskeleton.concurrent;

/**
 * Created by zhaoyi on 10/24/16.
 */
public interface Function<T, U> {
    U apply(T v);
}
