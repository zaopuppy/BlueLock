package com.example.zero.androidskeleton.state;

/**
 * Created by zero on 5/14/16.
 */
public interface State {
    void handle(Context context, int event, int arg, Object o);
}
