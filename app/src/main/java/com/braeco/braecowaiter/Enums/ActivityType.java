package com.braeco.braecowaiter.Enums;

/**
 * Created by Weiping on 2016/5/16.
 */


public enum ActivityType {

    /**
     * THEME  - 主题活动
     * GIVE   - 满送促销活动
     × REDUCE - 满减促销活动
     × OTHER  - 其他促销活动
     */

    THEME("theme"),
    GIVE("give"),
    REDUCE("reduce"),
    OTHER("other"),
    SECTION_THEME("section_theme"),
    SECTION_REDUCE("section_reduce"),
    ADD("add"),
    NONE("none");

    private String v;

    ActivityType(String v) {
        this.v = v;
    }
}
