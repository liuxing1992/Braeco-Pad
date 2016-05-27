package com.braeco.braecowaiter.Interfaces;

import org.json.JSONObject;

/**
 * Created by Weiping on 2016/5/14.
 */
public interface OnGetGoodsAsyncTaskListener {
    void success(JSONObject result);
    void fail(String message);
    void signOut();
}
