package com.braeco.braecowaiter.Model;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * Created by Weiping on 2016/5/20.
 */

public class ActivityCollector {

    private static List<android.app.Activity> activities = new ArrayList<>();

    public static int count() {
        return activities.size();
    }

    public static void add(Activity activity) {
//        if(!activities.contains(activity)){
//            activities.add(activity);
//        }
        activities.add(activity);
    }

    public static void finish(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for(Activity activity : activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}
