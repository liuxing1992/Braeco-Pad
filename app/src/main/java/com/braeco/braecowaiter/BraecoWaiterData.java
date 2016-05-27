package com.braeco.braecowaiter;

import com.braeco.braecowaiter.Model.Activity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Weiping on 2015/12/21.
 */
public class BraecoWaiterData {

    // network
    public static String BRAECO_PREFIX = "http://brae.co";

    // data for MeFragmentMenuMenuEditAttribute
    public static Integer ATTRIBUTE_GROUP = 0;
    public static Integer ATTRIBUTE = 1;
    public static Integer ATTRIBUTE_ADD = 2;
    public static Integer ATTRIBUTE_GROUP_ADD = 3;
    public static Integer DELETE = 0;
    public static Integer ADD = 1;
    public static Integer CHANGE_NAME = 2;
    public static Integer CHANGE_PRICE = 3;
    public static ArrayList<Map<String, Object>> attributes;
    public static boolean lastIsSaved = false;
    public static boolean attributeIsChanged = false;

    // data for MeFragmentMenuSetEditAttribute
    public static Integer SET_ATTRIBUTE_NAME = 0;
    public static Integer SET_ATTRIBUTE_BODY = 1;
    public static Integer SET_ATTRIBUTE_ADD = 2;
    public static Integer CHANGE_SET_NAME = 0;
    public static Integer CHANGE_SET = 1;
    public static Integer CHANGE_SET_SIZE = 2;
    public static Integer CHANGE_SET_DISCOUNT = 3;
    public static Integer DELETE_SET = 4;
    public static Integer ADD_SET = 5;
    public static ArrayList<Map<String, Object>> setAttributes;
    public static boolean setLastIsSaved = false;
    public static boolean setAttributeIsChanged = false;

    // activity
    public static int ACTIVITY_SECTION_SALE = 0;
    public static int ACTIVITY_SECTION_THEME = 1;
    public static int ACTIVITY_THEME = 2;
    public static int ACTIVITY_REDUCE = 3;
    public static int ACTIVITY_GIVE = 4;
    public static int ACTIVITY_OTHER = 5;
    public static int ACTIVITY_ALL = 6;
    public static ArrayList<Activity> activities = new ArrayList<>();
    public static boolean ACTIVITY_LOADING = false;
    public static int ACTIVITY_TASK_NUM = 0;

    // whether get the disable menu from server
    public final static boolean getDisableMenu = true;

    // data statistics
    public static int TASK_STATISTICS_PROFIT = 0;
    public static int TASK_STATISTICS_GOODS = 0;
    public static int TASK_STATISTICS_VIP = 0;

    // single refund
    public static int refundId = -1;
    public static ArrayList<Map<String, Object>> refundMeals = new ArrayList<>();
    public static ArrayList<Map<String, Object>> unRefundMeals = new ArrayList<>();
    public static boolean JUST_REFRESH_RECORDS = false;
    public static final int MAX_UN_REFUND_ID = 10;

    // activity discount
    public static ArrayList<Map<String, Object>> reduces;
    public static ArrayList<Map<String, Object>> gives;

    // category
    public static boolean JUST_ADD_CATEGORY = false;
    public static boolean JUST_UPDATE_CATEGORY = false;

    // vip charge needs password
    public static boolean VIP_NEEDS_PASSWORD = true;

    // custom service phone
    public static String CUSTOM_SERVICE_PHONE_SHOW = "400-6040-978";
    public static String CUSTOM_SERVICE_PHONE = "4006040978";



















































    private static BraecoWaiterData ourInstance = new BraecoWaiterData();

    public static BraecoWaiterData getInstance() {
        return ourInstance;
    }

    private BraecoWaiterData() {
    }
}
