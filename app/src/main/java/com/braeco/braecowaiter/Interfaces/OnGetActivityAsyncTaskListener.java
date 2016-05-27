package com.braeco.braecowaiter.Interfaces;

/**
 * Created by Weiping on 2016/5/14.
 */
public interface OnGetActivityAsyncTaskListener {
    void success();
    void fail(String message);
    void signOut();
}
