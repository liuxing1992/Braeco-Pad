package com.braeco.braecowaiter;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.braeco.braecowaiter.Enums.ActivityType;
import com.braeco.braecowaiter.Model.Activity;
import com.braeco.braecowaiter.Model.Table;
import com.braeco.braecowaiter.Model.Waiter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.johnpersano.supertoasts.SuperToast;
import com.nineoldandroids.animation.ObjectAnimator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;

/**
 * Created by Weiping on 2015/12/1.
 */

public class BraecoWaiterUtils {

    public static int MAX_CATEGORY = 200;

    public static int MAX_EXP = 1000000;
    public static int MAX_C_NAME_LENGTH = 32;
    public static int MIN_C_NAME_LENGTH = 1;
    public static int MAX_E_NAME_LENGTH = 32;
    public static int MIN_E_NAME_LENGTH = 0;
    public static double MAX_PRICE = 9999;
    public static double MIN_PRICE = 0;
    public static int MIN_TAG_LENGTH = 0;
    public static int MAX_TAG_LENGTH = 18;
    public static int MIN_DETAIL_LENGTH = 0;
    public static int MAX_DETAIL_LENGTH = 400;
    public static int MIN_DISCOUNT = 10;
    public static int MAX_DISCOUNT = 99;
    public static int MIN_MINUS = 1;
    public static int MAX_MINUS = 50;
    public static int MIN_LIMIT = 1;
    public static int MAX_LIMIT = 99;

    private static BraecoWaiterUtils ourInstance = new BraecoWaiterUtils();

    public static BraecoWaiterUtils getInstance() {
        return ourInstance;
    }

    private BraecoWaiterUtils() {

    }

    public String MD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public void LogListMap(List<Map<String, Object>> list) {
        for (int i = 0; i < list.size(); i++) {
            Log.d("BraecoWaiter", (i + 1) + " : " + list.get(i).toString());
        }
    }

    public void LogMap(Map<String, Object> m) {
        for (Map.Entry<String, Object> obj : m.entrySet()) {
            Log.d("BraecoWaiter", obj.getKey() + " : " + obj.getValue());
        }
    }

    public String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getKey(String type_ , Object msg_){
        JSONObject object = new JSONObject();
        String str1 = "{";
        str1 += "\"certificate\":" + "\"" + BraecoWaiterApplication.certificate + "\"";
        if (msg_ != null) str1 += ",\"msg\":" +  msg_.toString();
        str1 += ",\"pw\":" + "\""+ BraecoWaiterApplication.password + "\"";
        str1 += ",\"token\":" + "\""+ BraecoWaiterApplication.token+ "\"";
        str1 += ",\"type\":" + "\""+ type_ + "\""+ "}";
        return SHA1(str1);
    }

    public String newString(String type_ , Object msg_) {
        String key = "";
        key = getKey(type_ , msg_);
        JSONObject object = new JSONObject();
        Object str1 = "{";
        str1 += "\"type\":" + "\"" + type_ + "\"";
        if (msg_ != null) str1 += ",\"msg\":" +  msg_;
        str1 += ",\"key\":" + "\""+ key + "\""+ "}";
        return (String)str1  + "\n";
    }

    public void getTotalHeightofListView(ListView listView) {

        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));

            totalHeight += mView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    public int getColorFromResource(Context context, int res) {
        return ContextCompat.getColor(context, res);
    }

    public boolean isSameMeal(Map<String, Object> m1, Map<String, Object> m2) {
        if (m1.size() != m2.size())
            return false;
        boolean m1IsSet = m1.containsKey("isSet") && (Boolean)m1.get("isSet");
        boolean m2IsSet = m2.containsKey("isSet") && (Boolean)m2.get("isSet");
        if ((m1IsSet && !m2IsSet) || (!m1IsSet && m2IsSet)) return false;
        else if (!m1IsSet && !m2IsSet) {
            for (Object key: m1.keySet()) {
                if (key.equals("choices")) {
                    for (int i = 0; i < ((int[])m1.get(key)).length; i++) {
                        if (((int[])m1.get(key))[i] != ((int[])m2.get(key))[i]) return false;
                    }
                    continue;
                }
                if (!m1.get(key).equals(m2.get(key)))
                    return false;
            }
            return true;
        } else {
            if (((Double)m1.get("fullPrice")).equals((Double)m2.get("fullPrice"))) return false;
            else if (((Integer)m1.get("id")).equals((Integer)m2.get("id"))) return false;
            else {
                // compare every combo
                ArrayList<ArrayList<Map<String, Object>>> combos1 = ( ArrayList<ArrayList<Map<String, Object>>>)m1.get("properties");
                ArrayList<ArrayList<Map<String, Object>>> combos2 = ( ArrayList<ArrayList<Map<String, Object>>>)m2.get("properties");
                int size1 = combos1.size();
                int size2 = combos2.size();
                if (size1 != size2) return false;
                else {
                    for (int i = 0; i < size1; i++) {
                        ArrayList<Map<String, Object>> mealsInACombo1 = combos1.get(i);
                        ArrayList<Map<String, Object>> mealsInACombo2 = combos2.get(i);
                        int size3 = mealsInACombo1.size();
                        int size4 = mealsInACombo2.size();
                        if (size3 != size4) return false;
                        else {
                            for (int j = 0; j < size3; j++) {
                                int id1 = (Integer)mealsInACombo1.get(j).get("id");
                                int id2 = (Integer)mealsInACombo2.get(j).get("id");
                                if (id1 != id2) return false;
                                else {
                                    ArrayList<String> properties1 = (ArrayList<String>)mealsInACombo1.get(j).get("properties");
                                    ArrayList<String> properties2 = (ArrayList<String>)mealsInACombo2.get(j).get("properties");
                                    int size5 = properties1.size();
                                    int size6 = properties2.size();
                                    if (size5 != size6) return false;
                                    else {
                                        for (int k = 0; k < size5; k++) {
                                            if (!properties1.get(k).equals(properties2.get(k))) return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    public static boolean isSameSubMeal(Map<String, Object> subMeal1, Map<String, Object> subMeal2) {
        if (!subMeal1.get("id").equals(subMeal2.get("id"))) return false;
        else {
            ArrayList<String> properties1 = (ArrayList<String>)subMeal1.get("properties");
            ArrayList<String> properties2 = (ArrayList<String>)subMeal2.get("properties");
            int size5 = properties1.size();
            int size6 = properties2.size();
            if (size5 != size6) return false;
            else {
                for (int k = 0; k < size5; k++) {
                    if (!properties1.get(k).equals(properties2.get(k))) return false;
                }
            }
            return true;
        }
    }

    public static Double round02(Double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void showToast(String text) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(BraecoWaiterApplication.getAppContext());
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.show();
    }

    public static void showToast(Context context, String text, int color) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(context);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(color);
        superToast.show();
    }

    private static String lastToast = "";
    public static void showToast(Context context, String text) {
        if (context == null) return;
        if (lastToast.equals(text)) {
            SuperToast.cancelAllSuperToasts();
        } else {
            lastToast = text;
        }
        SuperToast superToast = new SuperToast(context);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.show();
    }

    public static void showToast(Context context, String text, boolean replace) {
        if (context == null) return;
        if (replace) {
            lastToast = text;
            SuperToast.cancelAllSuperToasts();
        } else {
            if (lastToast.equals(text)) {
                SuperToast.cancelAllSuperToasts();
            } else {
                lastToast = text;
            }
        }
        SuperToast superToast = new SuperToast(context);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.show();
    }

    // sundays from 2015 to 2020
    public static ArrayList<Long> MONDAYS = null;
    public static ArrayList<String> MONDAYS_STRING = null;
    public static long FOUR_WEEK_LONG = 4 * 7 * 24 * 60 * 60;
    public static long ONE_WEEK_LONG = 7 * 24 * 60 * 60;

    public static void createMondays() {
        if (MONDAYS != null && MONDAYS_STRING != null) return;
        MONDAYS = new ArrayList<>();
        MONDAYS_STRING = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 0, 26, 23, 59, 59);
        calendar.add(Calendar.SECOND, 0);
        long startSunday = calendar.getTimeInMillis() / 1000;
        long fourWeeksLong = 4 * 7 * 24 * 60 * 60;
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2020, 0, 1, 0, 0, 0);
        endCalendar.add(Calendar.SECOND, 0);
        long endSunday = endCalendar.getTimeInMillis() / 1000;
        while (startSunday < endSunday) {
            MONDAYS.add(startSunday);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            String dateString
                    = "从" + format1.format((startSunday - BraecoWaiterUtils.FOUR_WEEK_LONG  + 1) * 1000)
                    + "到" + format1.format(startSunday * 1000);
            MONDAYS_STRING.add(dateString);
            startSunday += fourWeeksLong;
        }
    }

    public static String chinaToUnicode(String str){
        String result="";
        for (int i = 0; i < str.length(); i++){
            int chr1 = (char) str.charAt(i);
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)
                result += "\\u" + Integer.toHexString(chr1);
            }else{
                result+=str.charAt(i);
            }
        }
        return result;
    }

    public static String allToUnicode(String str) {
        String result="";
        for (int i = 0; i < str.length(); i++){
            int chr1 = (char) str.charAt(i);
            result += "\\u" + Integer.toHexString(chr1);
        }
        return result;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public static int dp2px(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
    }

    public static boolean notNull(String text) {
        return text != null && !"null".equals(text);
    }

    public static boolean isInvalidMenuData(String data) {
        return data.contains(">") || data.contains("<") || data.contains("\\") || data.contains("&");
    }

    public static boolean isSameListObject(ArrayList<Object> m1, ArrayList<Object> m2) {
        if (m1.size() != m2.size())
            return false;
        for (int i = 0; i < m1.size(); i++) {
            if (m1.get(i) == null) {
                if (m2.get(i) != null) return false;
            } else {
                if (!m1.get(i).equals(m2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSame(ArrayList<Map<String, Object>> m1, ArrayList<Map<String, Object>> m2) {
        if (m1.size() != m2.size())
            return false;
        for (int i = 0; i < m1.size(); i++) {
            for (Object key : m1.get(i).keySet()) {
                if (!(m2.get(i).containsKey(key) && m2.get(i).get(key).equals(m1.get(i).get(key))))
                    return false;
            }
        }
        return true;
    }

    public static boolean isSameHashSetForInteger(HashSet<Integer> m1, HashSet<Integer> m2) {
        if (m1.size() != m2.size())
            return false;
        for (Integer i : m1) {
            if (!m2.contains(i)) return false;
        }
        return true;
    }

    public static boolean isSameHashSetForString(HashSet<String> m1, HashSet<String> m2) {
        if (m1.size() != m2.size())
            return false;
        for (String i : m1) {
            if (!m2.contains(i)) return false;
        }
        return true;
    }

    public static ArrayList<Map<String, Object>> copyList(ArrayList<Map<String, Object>> m2) {
        if (m2 == null) return null;
        ArrayList<Map<String, Object>> m1 = new ArrayList<>();
        for (int i = 0; i < m2.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>(m2.get(i));
            m1.add(map);
        }
        return m1;
    }

    public static ArrayList<Integer> copyList2(ArrayList<Integer> m2) {
        if (m2 == null) return null;
        ArrayList<Integer> m1 = new ArrayList<>();
        for (int i = 0; i < m2.size(); i++) {
            m1.add(m2.get(i));
        }
        return m1;
    }

    public static HashSet<Integer> copySet(HashSet<Integer> m2) {
        if (m2 == null) return null;
        HashSet<Integer> m1 = new HashSet<>();
        for (Integer i : m2) {
            m1.add(i);
        }
        return m1;
    }

    public static String toUnicode(String s) {
        if (s == null) return null;
        String ans = chinaToUnicode(s);
        ans.replace("\\\\", "\\");
        return ans;
//        StringBuilder b = new StringBuilder(s.length());
//        Formatter f = new Formatter(b);
//        for (char c : s.toCharArray()) {
//            if (c < 128) {
//                b.append(c);
//            } else {
//                f.format("\\u%04x", (int) c);
//            }
//        }
//        String ans = b.toString();
//        ans.replace("\\\\", "\\");
//        return ans;
    }

    public static int textCounter(String s) {
        int counter = 0;
        for (char c : s.toCharArray()) {
            if (c < 128) {
                counter++;
            } else {
                counter += 2;
            }
        }
        return counter;
    }

    public static String chineseToUnicode(String s) {
        if (s == null) return null;
        String as = chinaToUnicode(s);
        as.replace("\\\\", "\\");
        return as;
    }

    public static void sortActivities() {
        Collections.sort(BraecoWaiterData.activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity lhs, Activity rhs) {
                if (!lhs.getType().equals(ActivityType.THEME) && rhs.getType().equals(ActivityType.THEME)) return -1;
                else if (lhs.getType().equals(ActivityType.THEME) && !rhs.getType().equals(ActivityType.THEME)) return 1;
                return 0;
            }
        });
    }

    public static String cleanActivityParentheses(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '）') {
                return s.substring(i + 1);
            }
        }
        return s;
    }

    public static Spannable getDialogContent(Context mContext, String pre, String post, boolean countValid) {
        int i0 = 0, i1 = pre.length(), i2 = i1 + post.length();
        Spannable spannable = new SpannableString(pre + post);
        spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), i0, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), i1, i2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (!countValid) {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), i0, i2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.red)), i0, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.text_120)), i1, i2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public static String invalidString(CharSequence c) {
        String s = String.valueOf(c);
        String ans = "";
        if (s.contains("<")) ans += " <";
        if (s.contains(">")) ans += " >";
        if (s.contains("\\")) ans += " \\";
        if (s.contains("&")) ans += " &";
        if (ans.length() != 0) ans = "含有非法字符：" + ans;
        return ans;
    }

    public static String money2Digit(Double money) {
        return "¥" + String.format("%.2f", money);
    }

    public static void longInfo(String tag, String str) {
        if (str == null) Log.d(tag, "null");
        else {
            if (str.length() > 4000) {
                Log.d(tag, str.substring(0, 4000));
                longInfo(tag, str.substring(4000));
            } else Log.d(tag, str);
        }

    }

    public static void setMargins(View v, Context context, int l, int t, int r, int b) {
        l = dp2px(l, context);
        t = dp2px(t, context);
        r = dp2px(r, context);
        b = dp2px(b, context);
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static boolean ableRefund(Map<String, Object> meal) {
        if ((Integer)meal.get("id") <= BraecoWaiterData.MAX_UN_REFUND_ID) return false;
        String[] property = (String[]) meal.get("property");
        if (property == null) {
            return true;
        } else {
            for (int i = property.length - 1; i >= 0; i--) {
                if (BraecoWaiterFinal.REFUNDED_TEXT.equals(property[i])
                        || BraecoWaiterFinal.REFUNDING_TEXT.equals(property[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean refundingOrEd(Map<String, Object> meal) {
        String[] property = (String[]) meal.get("property");
        if (property == null) {
            return false;
        } else {
            for (int i = property.length - 1; i >= 0; i--) {
                if (BraecoWaiterFinal.REFUNDED_TEXT.equals(property[i])
                        || BraecoWaiterFinal.REFUNDING_TEXT.equals(property[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean refundingOrEdForSet(Map<String, Object> meal) {
        String property = (String) meal.get("properties");
        if (property == null) {
            return false;
        } else {
            if (property.contains(BraecoWaiterFinal.REFUNDED_TEXT)
                    || property.contains(BraecoWaiterFinal.REFUNDING_TEXT)) return true;
        }
        return false;
    }

    public static double calculateComboSum(ArrayList<Map<String, Object>> combo, boolean isCombo) {
        double ans = 0;
        if (isCombo) {
            for (int i = combo.size() - 1; i >= 0; i--) {
                int size = (Integer)combo.get(i).get("require");
                if (size == -1) {
                    ans += 0;
                } else {
                    HashSet<Integer> ids = (HashSet<Integer>) combo.get(i).get("content");
                    double minPrice = Double.MAX_VALUE;
                    for (Integer id : ids) {
                        if (getMenuPrice(id) < minPrice) minPrice = getMenuPrice(id);
                    }
                    if (minPrice == Double.MAX_VALUE) minPrice = 0;
                    int discount = (Integer)combo.get(i).get("discount");
                    ans += size * Double.parseDouble(new DecimalFormat("#.##").format(minPrice * discount / 100));
                }
            }
            return ans;
        } else {
            for (int i = combo.size() - 2; i > 0; i -= 2) {
                int size = (Integer)combo.get(i).get("data2");
                if (size == -1) {
                    ans += 0;
                } else {
                    HashSet<Integer> ids = (HashSet<Integer>) combo.get(i).get("data1");
                    double minPrice = Double.MAX_VALUE;
                    for (Integer id : ids) {
                        if (getMenuPrice(id) < minPrice) minPrice = getMenuPrice(id);
                    }
                    if (minPrice == Double.MAX_VALUE) minPrice = 0;
                    int discount = (Integer)combo.get(i).get("data3");
                    ans += size * Double.parseDouble(new DecimalFormat("#.##").format(minPrice * discount / 100));
                }
            }
            return ans;
        }
    }

    public static double getMenuPrice(int id) {
        for (int i = BraecoWaiterApplication.mSettingMenu.size() - 1; i >= 0; i--) {
            if (BraecoWaiterApplication.mSettingMenu.get(i).get("id").equals(id)) {
                return (Double) BraecoWaiterApplication.mSettingMenu.get(i).get("price");
            }
        }
        return 0;
    }

    public static void log(String string) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", string);
    }

//    public static Map<String, Object> getRecordFromJSON(JSONObject jsonObject) {
//
//    }

    public static void updateSid(Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if ("Set-Cookie".equals(entry.getKey())) {
                String[] cookieValues = ((entry.getValue() + "").substring(1)).split(";");
                for (String string : cookieValues) {
                    String[] keyPair = string.split("=");
                    String key = keyPair[0].trim();
                    String value = keyPair.length > 1 ? keyPair[1].trim() : "";
                    if ("sid".equals(key)) {
                        // Todo
                        BraecoWaiterUtils.log("Update sid(" + BraecoWaiterApplication.sid + ") to " + value);
                        BraecoWaiterApplication.sid = value;
                        Waiter.getInstance().setSid(value);
                        return;
                    }
                }
            }
        }
    }

    public static final String STRING_401 = "440011";
    public static JSONObject get401Json() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", STRING_401);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static void forceToLoginFor401(Context context) {
        BraecoWaiterUtils.log("Force to login for 401: " + context.getClass().getSimpleName());
        Intent intent = new Intent(context, SignOutDialogActivity.class);
        intent.putExtra("type", 1);
        context.startActivity(intent);
    }

    public static void forceToLoginForDuplicateUser(final Context context) {
        BraecoWaiterUtils.log("Force to login for duplicate user: " + context.getClass().getSimpleName());
        Intent intent = new Intent(context, SignOutDialogActivity.class);
        intent.putExtra("type", 0);
        context.startActivity(intent);
    }

    public static void cleanData() {
        BraecoWaiterApplication.hasLogin = false;
        BraecoWaiterApplication.writePreferenceBoolean(BraecoWaiterApplication.getAppContext(), "HAS_LOGIN", false);
        BraecoWaiterApplication.sid = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "SID", "");
        BraecoWaiterApplication.token = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "TOKEN", "");
        BraecoWaiterApplication.loginUrl = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "LOGIN_URL", "");
        BraecoWaiterApplication.port = -1;
        BraecoWaiterApplication.writePreferenceInt(BraecoWaiterApplication.getAppContext(), "PORT", -1);
        BraecoWaiterApplication.password = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "PASSWORD", "");
        BraecoWaiterApplication.phone = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "PHONE", "");
        BraecoWaiterApplication.shopName = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "SHOP_NAME", "");
        BraecoWaiterApplication.waiterLogo = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "WAITER_LOGO", "");
        BraecoWaiterApplication.shopPhone = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "SHOP_PHONE", "");
        BraecoWaiterApplication.address = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "ADDRESS", "");
        BraecoWaiterApplication.alipay = "";
        BraecoWaiterApplication.writePreferenceString(BraecoWaiterApplication.getAppContext(), "ALIPAY", "");
        BraecoWaiterApplication.wxpay = false;
        BraecoWaiterApplication.writePreferenceBoolean(BraecoWaiterApplication.getAppContext(), "WXPAY_QR", false);
        BraecoWaiterApplication.orderHasDiscount = true;
        BraecoWaiterApplication.writePreferenceBoolean(BraecoWaiterApplication.getAppContext(), "ORDER_HAS_DISCOUNT", true);
        for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
            BraecoWaiterApplication.pictureAddress[i] = "";
        }
        BraecoWaiterApplication.writePreferenceArray(BraecoWaiterApplication.getAppContext(), "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);

        Waiter.getInstance().cleanNickName();
        Waiter.getInstance().cleanSid();
        Waiter.getInstance().cleanLastUseVersion();
        Waiter.getInstance().cleanAuthority();
    }


    public static void forceToLogin(Context context) {
        // Todo
        try {
            if (BraecoWaiterApplication.socket != null) BraecoWaiterApplication.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cleanData();
        // start the login activity
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        BraecoWaiterApplication.clearFragments();
        BraecoWaiterApplication.exit();
    }

    public static void sortTables() {
        Collections.sort(BraecoWaiterApplication.tables, new Comparator<Table>() {
            @Override
            public int compare(Table lhs, Table rhs) {
                String strl = lhs.getId();
                String strr = rhs.getId();
                boolean isNum = true;
                for (int i = 0; i < strl.length(); i++) {
                    if (!('0' <= strl.charAt(i) && strl.charAt(i) <= '9')) {
                        isNum = false;
                        break;
                    }
                }
                if (isNum) {
                    while (strl.length() < 3) strl = "0" + strl;
                }
                isNum = true;
                for (int i = 0; i < strr.length(); i++) {
                    if (!('0' <= strr.charAt(i) && strr.charAt(i) <= '9')) {
                        isNum = false;
                        break;
                    }
                }
                if (isNum) {
                    while (strr.length() < 3) strr = "0" + strr;
                }
                return strl.compareTo(strr);
            }
        });
    }

    public static String getParams(List<NameValuePair> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first) first = false;
            else result.append("&");

            result.append(pair.getName());
            result.append("=");
            result.append(pair.getValue());
        }

        return result.toString();
    }

    public static void decreaseLimit() {
        for (int i = 0; i < BraecoWaiterApplication.orderedMealsPair.size(); i++) {
            int id = (Integer) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("id");
            for (int j = 0; j < BraecoWaiterApplication.mMenu.size(); j++) {
                if (id == (Integer) BraecoWaiterApplication.mMenu.get(j).get("id")) {
                    if ("limit".equals((String) BraecoWaiterApplication.mMenu.get(j).get("dc_type"))) {
                        int original = (Integer) BraecoWaiterApplication.mMenu.get(j).get("dc");
                        BraecoWaiterApplication.mMenu.get(j).put("dc",
                                original - BraecoWaiterApplication.orderedMealsPair.get(i).second);
                        break;
                    }
                    break;
                }
            }
        }
    }

    public static void disappear(final View view) {
        view.setVisibility(View.INVISIBLE);
//        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f).setDuration(300);
//        objectAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                view.setVisibility(View.INVISIBLE);
//                super.onAnimationEnd(animation);
//            }
//        });
//        objectAnimator.start();
    }

    public static void appear(View view) {
        view.setVisibility(View.VISIBLE);
//        ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f).setDuration(300).start();
    }

    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static View[] getVisibleViewByPosition(ListView listView) {
        int firstListItemPosition = listView.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        int count = lastListItemPosition - firstListItemPosition + 1;
        View[] views = new View[count];
        for (int i = 0; i < count; i++) {
            views[i] = listView.getChildAt(i);
        }
        return views;
    }

    public static void callForHelp(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        context.startActivity(
                new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + BraecoWaiterData.CUSTOM_SERVICE_PHONE)));
    }

    public static int getScreenWidth(Context context) {
        Display localDisplay
                = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point point = new Point();
        localDisplay.getSize(point);
        return point.x;
    }

    public static void delayBounceInUp(final long delay, final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0).setDuration(delay);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                YoYo.with(Techniques.BounceInUp)
                        .duration(700)
                        .playOn(view);
            }
        });
        animator.start();
    }

    public static ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        return animator;
    }
}
