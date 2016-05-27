package com.braeco.braecowaiter.Interfaces;

import com.braeco.braecowaiter.Model.Vip;

/**
 * Created by Weiping on 2016/5/14.
 */
public interface OnGetOneVipAsyncTaskListener {
    void success(Vip vip);
    void fail(String message);
    void signOut();
}
