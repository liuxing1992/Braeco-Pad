package com.braeco.braecowaiter.Interfaces;

import com.braeco.braecowaiter.Enums.PayType;

/**
 * Created by Weiping on 2016/5/14.
 */
public interface OnSetOrderAsyncTaskListener {
    void success(String message, PayType payType);
    void fail(String message);
    void signOut();
}
