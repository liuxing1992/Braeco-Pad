package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Enums.PayType;
import com.braeco.braecowaiter.Interfaces.OnGetTableAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetOrderAsyncTaskListener;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.Tasks.GetTableAsyncTask;
import com.braeco.braecowaiter.Tasks.SetOrderAsyncTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ServiceMenuFragmentCarMakeSureAlipay extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private TextView sum;
    private LinearLayout back;
    private LinearLayout makeSure;

    private ImageView image;

    private Context mContext;

    private boolean isOutdoor;
    private String tableNum;

    private boolean hasPressed = false;

    private boolean canGoBack = true;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_menu_fragment_car_make_sure_alipay);

        mContext = this;

        sum = (TextView)findViewById(R.id.sum);
        sum.setText(getIntent().getStringExtra("sum"));

        isOutdoor = getIntent().getBooleanExtra("isOutdoor", false);
        tableNum = getIntent().getStringExtra("tableNum");

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        makeSure = (LinearLayout)findViewById(R.id.make_sure);
        makeSure.setOnClickListener(this);

        image = (ImageView)findViewById(R.id.image);
        image.setImageBitmap(generateQRCode(BraecoWaiterApplication.alipay));
    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 500, 500);
            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.make_sure:
                if (hasPressed) return;
                else {
                    hasPressed = true;
                    canGoBack = false;
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < BraecoWaiterApplication.orderedMealsPair.size(); i++) {
                        JSONObject object = new JSONObject();
                        try {
                            if (BraecoWaiterApplication.orderedMealsPair.get(i).first.containsKey("isSet")
                                    && (Boolean)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("isSet")) {
                                // is set
                                ArrayList<ArrayList<Map<String, Object>>> combos = (ArrayList<ArrayList<Map<String, Object>>>)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("properties");
                                JSONArray combosJSON = new JSONArray();
                                int combosSize = combos.size();
                                for (int j = 0; j < combosSize; j++) {
                                    ArrayList<Map<String, Object>> meals = combos.get(j);
                                    JSONArray mealsJSON = new JSONArray();
                                    int mealsSize = combos.get(j).size();
                                    for (int k = 0; k < mealsSize; k++) {
                                        JSONObject meal = new JSONObject();
                                        ArrayList<String> properties = (ArrayList<String>)meals.get(k).get("properties");
                                        JSONArray propertiesJSON = new JSONArray();
                                        int propertiesSize = properties.size();
                                        for (int u = 0; u < propertiesSize; u++) {
                                            propertiesJSON.put(properties.get(u));
                                        }
                                        meal.put("id", (Integer)meals.get(k).get("id"));
                                        meal.put("p", propertiesJSON);
                                        mealsJSON.put(meal);
                                    }
                                    combosJSON.put(mealsJSON);
                                }
                                object.put("p", combosJSON);
                                object.put("s", BraecoWaiterApplication.orderedMealsPair.get(i).second);
                                object.put("id", BraecoWaiterApplication.orderedMealsPair.get(i).first.get("id"));
                                object.put("m", BraecoWaiterUtils.round02((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice"), 2));
                            } else {
                                // is meal
                                object.put("id", BraecoWaiterApplication.orderedMealsPair.get(i).first.get("id"));
                                if (!BraecoWaiterApplication.orderHasDiscount) {
                                    object.put("m", BraecoWaiterUtils.round02((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice"), 2));
                                } else {
                                    if ("half".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                                        object.put("m", "H" + String.valueOf(BraecoWaiterUtils.round02((double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice"), 2)));
                                    } else if ("sale".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                                        Double sale = Double.valueOf(
                                                (Integer)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc"));
                                        object.put("m", BraecoWaiterUtils.round02(((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice") - sale), 2));
                                    } else if ("discount".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                                        Double discount = (Double.valueOf((Integer)BraecoWaiterApplication.orderedMealsPair
                                                .get(i).first.get("dc"))) / 100;
                                        object.put("m", BraecoWaiterUtils.round02((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                                                * discount, 2));
                                    } else {
                                        object.put("m", BraecoWaiterUtils.round02((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice"), 2));
                                    }
                                }
                                object.put("s" , (int)BraecoWaiterApplication.orderedMealsPair.get(i).second);
                                JSONArray jsonArray = new JSONArray();
                                ArrayList<String>[] attributeItemName = (ArrayList<String>[]) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("shuxing");
                                int[] choices = (int[])BraecoWaiterApplication.orderedMealsPair.get(i).first.get("choices");
                                int attributeGroupNum = (Integer) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("num_shuxing");
                                for (int j = 0; j < attributeGroupNum; j++) {
                                    jsonArray.put(attributeItemName[j].get(choices[j]));
                                }
                                object.put("p" , jsonArray);
                            }
                            Log.d("BraecoWaiter", object.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        array.put(object);
                    }
                    if (isOutdoor)
                        new SetOrderAsyncTask(mOnSetOrderAsyncTaskListener, array.toString(), "tkot", PayType.ALIPAY, "")
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        new SetOrderAsyncTask(mOnSetOrderAsyncTaskListener, array.toString(), tableNum, PayType.ALIPAY, "")
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    progressDialog = new MaterialDialog.Builder(mContext)
                            .title("下单中")
                            .content("请稍候")
                            .cancelable(false)
                            .progress(true, 0)
                            .show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!canGoBack) {
            BraecoWaiterUtils.showToast(mContext, "请等待下单完成");
            return;
        }
        super.onBackPressed();
    }



// get menu/////////////////////////////////////////////////////////////////////////////////////////
// get menu/////////////////////////////////////////////////////////////////////////////////////////
// get menu/////////////////////////////////////////////////////////////////////////////////////////
    private class getMenu extends AsyncTask<String, Void, String> {
        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "getMenu result: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(new BasicNameValuePair("",""));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            BraecoWaiterApplication.LOADING_MENU = false;
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }
            if (BuildConfig.DEBUG) System.out.println("getMenu: " + result);
            if (result != null) {
                try {
                    writeMenu(new JSONArray(result));
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "登录已过期，请重新登录");
                    e.printStackTrace();
                    BraecoWaiterApplication.LOAD_MENU_FAIL = true;
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                BraecoWaiterApplication.LOAD_MENU_FAIL = true;
            }
        }
    }

// fill in//////////////////////////////////////////////////////////////////////////////////////////
// fill in//////////////////////////////////////////////////////////////////////////////////////////
// fill in//////////////////////////////////////////////////////////////////////////////////////////
    private void writeMenu(JSONArray jsonCategories) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "writeMenu array: " + jsonCategories.toString());

        Log.d("BraecoWaiter", "Category Number : " + jsonCategories.length());

        for (int i = 0; i < BraecoWaiterUtils.MAX_CATEGORY; i++) BraecoWaiterApplication.a[i] = -1;
        BraecoWaiterApplication.a[0] = 0;
        BraecoWaiterApplication.mButton = new ArrayList<>();
        BraecoWaiterApplication.mMenu = new ArrayList<>();
        BraecoWaiterApplication.mSet = new ArrayList<>();
        BraecoWaiterApplication.mSelectedMenu = new ArrayList<>();
        for (int i = 0; i < BraecoWaiterUtils.MAX_CATEGORY; i++) BraecoWaiterApplication.b[i] = -1;
        BraecoWaiterApplication.b[0] = 0;
        BraecoWaiterApplication.mSettingMenu = new ArrayList<>();

        try {
            int categoriesNum = jsonCategories.length();
            int setNum = 0;
            for (int i = 0; i < categoriesNum; i++) {
                JSONObject jsonCategory = jsonCategories.getJSONObject(i);
                JSONArray jsonMenus = jsonCategory.getJSONArray("dishes");
                int menusNum = jsonMenus.length();
                int disableNum = 0;
                for (int j = 0; j < menusNum; j++) {
                    JSONObject jsonMenu = jsonMenus.getJSONObject(j);
                    if (!BraecoWaiterData.getDisableMenu && !jsonMenu.getBoolean("able")) {
                        disableNum++;
                    }
                    if ("combo_static".equals(jsonMenu.getString("dc_type")) || "combo_sum".equals(jsonMenu.getString("dc_type"))) setNum++;
                    setNum -= disableNum;
                }
            }
            Map<String, Object> setCategory = new HashMap<>();
            setCategory.put("id", -1);
            setCategory.put("button", "套餐推荐");
            setCategory.put("categorypic", "");
            BraecoWaiterApplication.mButton.add(setCategory);
            BraecoWaiterApplication.a[1] = setNum;
            BraecoWaiterApplication.b[1] = setNum;
            int lastSetPosition = 0;

            for (int i = 0; i < categoriesNum; i++) {
                JSONObject jsonCategory = jsonCategories.getJSONObject(i);
                Map<String, Object> category = new HashMap<>();
                category.put("id", jsonCategory.getInt("categoryid"));
                category.put("button", jsonCategory.getString("categoryname"));
                category.put("categorypic", jsonCategory.getString("categorypic"));
                JSONArray jsonMenus = jsonCategory.getJSONArray("dishes");
                int menusNum = jsonMenus.length();
                int menusShownNum = jsonMenus.length();
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Category " + i + ": " + jsonCategory.getString("categoryname") + " " + menusNum + " " + jsonCategory.getString("categorypic"));
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Category " + i + jsonMenus.toString());
                int disableNum = 0;
                for (int j = 0; j < menusNum; j++) {
                    JSONObject jsonMenu = jsonMenus.getJSONObject(j);
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("able", jsonMenu.getBoolean("able"));
                    if (!BraecoWaiterData.getDisableMenu && !jsonMenu.getBoolean("able")) {
                        disableNum++;
                        // Todo this continue can lead to bugs
                        continue;
                    }
                    if (jsonMenu.has("dc_num")) menu.put("dc_num", jsonMenu.getInt("dc_num"));
                    else menu.put("dc_num", -1);
                    menu.put("dc_type", jsonMenu.getString("dc_type"));
                    if (jsonMenu.has("dc")) menu.put("dc", jsonMenu.getInt("dc"));
                    else menu.put("dc", -1);
                    menu.put("price", jsonMenu.getDouble("defaultprice"));
                    menu.put("detail", jsonMenu.getString("detail"));
                    menu.put("id", jsonMenu.getInt("dishid"));
                    menu.put("name", jsonMenu.getString("dishname"));
                    menu.put("en_name", jsonMenu.getString("dishname2"));
                    menu.put("img", jsonMenu.getString("dishpic"));
                    menu.put("like", jsonMenu.getInt("like"));
                    menu.put("tag", jsonMenu.getString("tag"));
                    // attributes
                    JSONArray jsonAttributes = jsonMenu.getJSONArray("groups");
                    int attributesNum = jsonAttributes.length();
                    String[] attributesNames = new String[attributesNum];
                    int[] attributesCounts = new int[attributesNum];
                    ArrayList<Double>[] subAttributesPrices = new ArrayList[attributesNum];
                    ArrayList<String>[] subAttributesNames = new ArrayList[attributesNum];
                    for (int k = 0; k < attributesNum; k++) {
                        JSONObject jsonAttribute = jsonAttributes.getJSONObject(k);
                        subAttributesNames[k] = new ArrayList<>();
                        subAttributesPrices[k] = new ArrayList<>();
                        attributesCounts[k] = 0;
                        attributesNames[k] = jsonAttribute.getString("groupname");
                        JSONArray jsonSubAttributes = jsonAttribute.getJSONArray("property");
                        attributesCounts[k] = jsonSubAttributes.length();
                        for (int o = 0; o < attributesCounts[k]; o++) {
                            JSONObject jsonSubAttribute = jsonSubAttributes.getJSONObject(o);
                            subAttributesNames[k].add(jsonSubAttribute.getString("name"));
                            subAttributesPrices[k].add(jsonSubAttribute.getDouble("price"));
                        }
                    }
                    menu.put("num_shuxing", attributesNum);
                    menu.put("shuxingName", attributesNames);
                    menu.put("res", attributesCounts);
                    menu.put("addshuxing", subAttributesPrices);
                    menu.put("shuxing", subAttributesNames);
                    // combo
                    menu.put("categoryid", jsonCategory.getInt("categoryid"));
                    if (jsonMenu.has("combo")) {
                        JSONArray jsonArrayCombo = jsonMenu.getJSONArray("combo");
                        ArrayList<Map<String, Object>> combos = new ArrayList<>();
                        int comboSize = jsonArrayCombo.length();
                        for (int k = 0; k < comboSize; k++) {
                            JSONObject jsonCombo = jsonArrayCombo.getJSONObject(k);
                            Map<String, Object> combo = new HashMap<>();
                            if ("combo_sum".equals(jsonMenu.getString("dc_type"))) {
                                combo.put("discount", jsonCombo.getInt("discount"));
                            } else {
                                combo.put("discount", -2);  // static
                            }
                            combo.put("require", jsonCombo.getInt("require"));
                            combo.put("name", jsonCombo.getString("name"));
                            JSONArray jsonArrayContent = jsonCombo.getJSONArray("content");
                            HashSet<Integer> ids = new HashSet<>();
                            int idSize = jsonArrayContent.length();
                            for (int v = 0; v < idSize; v++) {
                                ids.add(jsonArrayContent.getInt(v));
                            }
                            combo.put("content", ids);
                            combos.add(combo);
                        }
                        menu.put("combo", combos);
                        BraecoWaiterApplication.mSet.add(menu);
                    }
                    if (BuildConfig.DEBUG) Log.d("BraecoWatier", jsonMenu.toString());
                    if (BuildConfig.DEBUG) Log.d("BraecoWatier", menu.toString());

                    if (!"combo_only".equals(jsonMenu.getString("dc_type")))
                        BraecoWaiterApplication.mMenu.add(menu);
                    else menusShownNum--;
                    if ("combo_sum".equals(jsonMenu.getString("dc_type")) || "combo_static".equals(jsonMenu.getString("dc_type")))
                        BraecoWaiterApplication.mMenu.add(lastSetPosition, menu);

                    if (!"combo_sum".equals(jsonMenu.getString("dc_type")) && !"combo_static".equals(jsonMenu.getString("dc_type")))
                        BraecoWaiterApplication.mSelectedMenu.add(menu);

                    BraecoWaiterApplication.mSettingMenu.add(menu);
                    if ("combo_sum".equals(jsonMenu.getString("dc_type")) || "combo_static".equals(jsonMenu.getString("dc_type")))
                        BraecoWaiterApplication.mSettingMenu.add(lastSetPosition++, menu);

                }
                BraecoWaiterApplication.a[i + 2] = BraecoWaiterApplication.a[i + 1] + menusShownNum - disableNum;
                BraecoWaiterApplication.b[i + 2] = BraecoWaiterApplication.b[i + 1] + menusNum;
                BraecoWaiterApplication.mButton.add(category);
            }
            if (BuildConfig.DEBUG) {
                String as = "";
                for (int i = 0; i < BraecoWaiterUtils.MAX_CATEGORY; i++) {
                    if (BraecoWaiterApplication.a[i] == -1) break;
                    as += " " + BraecoWaiterApplication.a[i];
                }
                Log.d("BraecoWaiter", "A:" + as);
            }
            BraecoWaiterApplication.LOADED_MENU = true;
            BraecoWaiterApplication.isPinned = new boolean[BraecoWaiterApplication.mButton.size() + BraecoWaiterApplication.mMenu.size()];
            BraecoWaiterApplication.index = new int[BraecoWaiterApplication.mButton.size() + BraecoWaiterApplication.mMenu.size()];
            BraecoWaiterApplication.orderedMealsPair = new ArrayList<>();
            BraecoWaiterApplication.orderedMeals = new ArrayList<>();
            for (int i = BraecoWaiterApplication.mMenu.size() - 1; i >= 0; i--)
                BraecoWaiterApplication.orderedMeals.add(new Stack<Map<String, Object>>());
            int p = 0, section = 0, item = 0;
            while (true) {
                if (item == BraecoWaiterApplication.a[section]) {
                    if (BraecoWaiterApplication.a[section + 1] == -1) break;
                    BraecoWaiterApplication.index[p] = section++;
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", BraecoWaiterApplication.index[p] + "");
                    BraecoWaiterApplication.isPinned[p++] = true;
                } else {
                    BraecoWaiterApplication.index[p] = item++;
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", BraecoWaiterApplication.index[p] + "");
                    BraecoWaiterApplication.isPinned[p++] = false;
                }
            }

            BraecoWaiterUtils.showToast(mContext, "载入菜单完成");
            if (BraecoWaiterApplication.serviceMenuFragment != null) {
                BraecoWaiterApplication.serviceMenuFragment.refreshMenu();
            }
            BraecoWaiterUtils.getInstance().LogListMap(BraecoWaiterApplication.mMenu);
            BraecoWaiterUtils.getInstance().LogListMap(BraecoWaiterApplication.mButton);
        } catch (JSONException j) {
            j.printStackTrace();
        }
    }

    private OnGetTableAsyncTaskListener mOnGetTableAsyncTaskListener = new OnGetTableAsyncTaskListener() {
        @Override
        public void success() {
            BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "桌位刷新成功");
        }

        @Override
        public void fail(String message) {
            BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "桌位刷新失败（" + message + "）");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetOrderAsyncTaskListener mOnSetOrderAsyncTaskListener = new OnSetOrderAsyncTaskListener() {
        @Override
        public void success(String content, PayType payType) {
            progressDialog.dismiss();
            if (PayType.WECHAT.equals(payType)) {
                // use wechat to pay
                // we should go to a new activity to let the customer pay
                Intent intent = new Intent(mContext, ServiceMenuFragmentCarMakeSureWechatPay.class);
                intent.putExtra("sum", sum.getText().toString());
                intent.putExtra("qrcode", content);
                startActivityForResult(intent, 1);
                return;
            }

            // Todo whether decrease
            BraecoWaiterUtils.decreaseLimit();
            for (int i = BraecoWaiterApplication.orderedMeals.size() - 1; i >= 0; i--)
                BraecoWaiterApplication.orderedMeals.get(i).clear();
            BraecoWaiterApplication.orderedMealsPair.clear();

            new MaterialDialog.Builder(mContext)
                    .title("下单成功")
                    .content("下单成功")
                    .cancelable(false)
                    .positiveText("确认")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("ERROR", "NO_ERROR");
                                setResult(RESULT_OK, resultIntent);
                                BraecoWaiterApplication.FINISH_ORDER = true;
                                BraecoWaiterApplication.JUST_GIVE_ORDER = true;
                                finish();
                            }
                        }
                    })
                    .show();
        }

        @Override
        public void fail(String message) {
            progressDialog.dismiss();
            switch (message) {
                case "价格与服务器不符合，请重新下单":
                    if (!BraecoWaiterApplication.LOADING_MENU) {
                        BraecoWaiterApplication.LOADED_MENU = false;
                        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                        BraecoWaiterApplication.LOADING_MENU = true;
                        new getMenu().execute("http://brae.co/Dinner/Info/Get");
                    }
                    new MaterialDialog.Builder(mContext)
                            .title("下单失败")
                            .content("价格与服务器不符合")
                            .positiveText("确认")
                            .cancelable(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (dialogAction == DialogAction.POSITIVE) {
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("ERROR", "NOT_MATCH");
                                        setResult(RESULT_OK, resultIntent);
                                        BraecoWaiterApplication.FINISH_ORDER = true;
                                        finish();
                                    }
                                }
                            })
                            .show();
                    break;
                case "桌位不存在，请重新下单":
                    new MaterialDialog.Builder(mContext)
                            .title("下单失败")
                            .content("桌位不存在")
                            .positiveText("确认")
                            .show();
                    new GetTableAsyncTask(mOnGetTableAsyncTaskListener)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                    hasPressed = false;
                    break;
                case "其中某些餐品不存在，请重新下单":
                    if (!BraecoWaiterApplication.LOADING_MENU) {
                        BraecoWaiterApplication.LOADED_MENU = false;
                        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                        BraecoWaiterApplication.LOADING_MENU = true;
                        new getMenu().execute("http://brae.co/Dinner/Info/Get");
                    }
                    new MaterialDialog.Builder(mContext)
                            .title("下单失败")
                            .content("某些菜品不存在")
                            .positiveText("确认")
                            .cancelable(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (dialogAction == DialogAction.POSITIVE) {
                                        BraecoWaiterApplication.FINISH_ORDER = true;
                                        finish();
                                    }
                                }
                            })
                            .show();
                    break;
                case "其中某些餐品暂时无法提供，请重新下单":
                    if (!BraecoWaiterApplication.LOADING_MENU) {
                        BraecoWaiterApplication.LOADED_MENU = false;
                        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                        BraecoWaiterApplication.LOADING_MENU = true;
                        new getMenu().execute("http://brae.co/Dinner/Info/Get");
                    }new MaterialDialog.Builder(mContext)
                        .title("下单失败")
                        .content("某些菜品菜品暂时无法提供")
                        .positiveText("确认")
                        .cancelable(false)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("ERROR", "NOT_MATCH");
                                    setResult(RESULT_OK, resultIntent);
                                    BraecoWaiterApplication.FINISH_ORDER = true;
                                    finish();
                                }
                            }
                        })
                        .show();
                    break;

                case "其中某些限量供应的餐品售罄，正在刷新餐品":
                    if (!BraecoWaiterApplication.LOADING_MENU) {
                        BraecoWaiterApplication.LOADED_MENU = false;
                        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                        BraecoWaiterApplication.LOADING_MENU = true;
                        new getMenu().execute("http://brae.co/Dinner/Info/Get");
                    }
                    new MaterialDialog.Builder(mContext)
                            .title("下单失败")
                            .content("所点的某种限量供应的餐品售罄，正在刷新餐品")
                            .positiveText("确认")
                            .cancelable(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (dialogAction == DialogAction.POSITIVE) {
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("ERROR", "NOT_MATCH");
                                        setResult(RESULT_OK, resultIntent);
                                        BraecoWaiterApplication.FINISH_ORDER = true;
                                        finish();
                                    }
                                }
                            })
                            .show();
                    break;
                case "套餐格式不合法":
                case "餐厅端未开启，请开启后重新下单":
                case "版本过旧，请更新后下单":
                default:
                    new MaterialDialog.Builder(mContext)
                            .title("下单失败")
                            .content(message)
                            .positiveText("确认")
                            .show();
                    hasPressed = false;
                    break;
            }
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };
}
