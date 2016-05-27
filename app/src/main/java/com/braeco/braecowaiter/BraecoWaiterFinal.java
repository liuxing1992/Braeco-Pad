package com.braeco.braecowaiter;

/**
 * Created by Weiping on 2015/12/21.
 */
public class BraecoWaiterFinal {

    public final static int MIN_ATTRIBUTE_GROUP_NAME = 1;
    public final static int MAX_ATTRIBUTE_GROUP_NAME = 20;
    public final static int MIN_ATTRIBUTE_NAME = 1;
    public final static int MAX_ATTRIBUTE_NAME = 20;
    public final static double MIN_ATTRIBUTE_PRICE = 0;
    public final static double MAX_ATTRIBUTE_PRICE = 5000;
    public final static int MIN_CATEGORY_NAME = 1;
    public final static int MAX_CATEGORY_NAME = 14;
    public final static int MIN_ACTIVITY_NAME = 1;
    public final static int MAX_ACTIVITY_NAME = 40;
    public final static int MIN_ACTIVITY_SUMMARY = 1;
    public final static int MAX_ACTIVITY_SUMMARY = 40;
    public final static int MIN_ACTIVITY_DETAIL = 1;
    public final static int MAX_ACTIVITY_DETAIL = 400;
    public final static int MIN_ACTIVITY_GIVE = 1;
    public final static int MAX_ACTIVITY_GIVE = 16;

    public final static double MIN_SET_ATTRIBUTE_SIZE = 1;
    public final static double MAX_SET_ATTRIBUTE_SIZE = 99;
    public final static double MIN_SET_ATTRIBUTE_DISCOUNT = 1;
    public final static double MAX_SET_ATTRIBUTE_DISCOUNT = 100;

    public final static int MIN_YEAR = 2015;
    public final static int MAX_YEAR = 2050;

    public final static String REFUNDED_TEXT = "【已退款】";
    public final static String REFUNDING_TEXT = "【退款中】";

    public final static int MIN_REFUNDED_REMARK = 0;
    public final static int MAX_REFUNDED_REMARK = 255;

    private static BraecoWaiterFinal ourInstance = new BraecoWaiterFinal();

    public static BraecoWaiterFinal getInstance() {
        return ourInstance;
    }

    private BraecoWaiterFinal() {
    }
}
