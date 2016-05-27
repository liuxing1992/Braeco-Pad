package com.braeco.braecowaiter.Interfaces;

/**
 * Created by Weiping on 2016/5/14.
 */
public interface OnSetVipBalanceAsyncTaskListener {
    void success(int status, double balance, int exp);
    void fail(String message);
    void signOut();
}
