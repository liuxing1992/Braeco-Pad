package com.braeco.braecowaiter.Enums;

/**
 * Created by Weiping on 2016/5/14.
 */

public enum PayType {

    CASH(0),
    WECHAT(1),
    ALIPAY(2),
    BALANCE(3);

    int v;

    PayType(int v) {
        this.v = v;
    }
}
