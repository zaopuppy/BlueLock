package com.example.zero.androidskeleton.concurrent;

/**
 * Created by zhaoyi on 7/18/16.
 */
public interface Future<T> extends java.util.concurrent.Future<T> {
    Future<T> addListener(GenericFutureListener<T> listener);
}
