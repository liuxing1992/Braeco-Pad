package com.braeco.braecowaiter.Model;

/**
 * Created by Weiping on 2016/5/20.
 */

public class Authority {

    /**
     * 新建、修改、删除、置顶品类
     * 新建、修改、移动、删除、置顶餐品（包含套餐和单品）
     */
    public static final long MANAGER_DISH = 1;

    /**
     * 恢复/暂停售卖餐品（包含套餐和单品）
     */
    public static final long VISIBLE_DISH = 2;

    /**
     * 新建、修改、删除主题/其他促销活动
     */
    public static final long MANAGER_ACTIVITY = 16;

    /**
     * 新建、修改、删除满减/满送促销活动
     */
    public static final long MANAGER_DISCOUNT = 32;

    /**
     * 浏览会员信息
     */
    public static final long VIEW_VIP = 64;

    /**
     * 会员充值、修改积分
     */
    public static final long SET_BALANCE_EXP = 128;

    /**
     * 修改会员等级所需积分和等级对应的优惠方式
     */
    public static final long MANAGER_COUPONS = 256;

    /**
     * 新功能，待定
     */
    public static final long SELL_COMBINATION = 512;

    /**
     * 查看今日流水订单、查看任意时间流水订单
     */
    public static final long VIEW_RECORD = 2048;

    /**
     * 退款操作
     */
    public static final long REFUND = 4096;

    /**
     * 查看数据统计
     */
    public static final long STATISTICS = 8192;

    /**
     * 新功能，待定
     */
    public static final long ANALYSIS = 16384;

    /**
     * 新功能，待定
     */
    public static final long SELF_SERVICE = 131072;

    /**
     * 新功能，待定
     */
    public static final long SUBSCRIBE = 262144;

    /**
     * 新功能，待定
     */
    public static final long TAKE_OUT = 524288;

    /**
     * 处理订单，不包含退款
     */
    public static final long DEAL_ORDER = 4194304;

    /**
     * 辅助点餐
     */
    public static final long GIVE_ORDER = 8388608;

}
