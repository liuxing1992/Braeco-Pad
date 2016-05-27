package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MeFragmentShop extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        CustomTextSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener,
        View.OnLongClickListener {

    private LinearLayout time;
    private LinearLayout back;

    private TextView name;
    private TextView address;
    private TextView phone;
    private TextView emptyTip;

    private SliderLayout slider;

    private Context mContext;

    private int picturesNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_me_fragment_shop);

        time = (LinearLayout)findViewById(R.id.time);
        time.setOnClickListener(this);
        time.setVisibility(View.GONE);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        name = (TextView)findViewById(R.id.show_name);
        address = (TextView)findViewById(R.id.show_address);
        phone = (TextView)findViewById(R.id.show_phone);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        emptyTip.setOnClickListener(this);

        name.setText(BraecoWaiterApplication.shopName);
        address.setText(BraecoWaiterApplication.address);
        phone.setText(BraecoWaiterApplication.shopPhone);

        BraecoWaiterApplication.pictureSize = "?imageView2/1/w/" + mContext.getResources().getDisplayMetrics().widthPixels
                + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);

        loadPictures();

        getPictures();
    }

    private void loadPictures() {
        if ("".equals(BraecoWaiterApplication.pictureAddress[0])) {
            emptyTip.setVisibility(View.VISIBLE);
        } else {
            emptyTip.setVisibility(View.GONE);
        }

        slider = (SliderLayout) findViewById(R.id.slider);
        slider.removeAllSliders();

        picturesNumber = 0;
        for (String s : BraecoWaiterApplication.pictureAddress) {
            if ("".equals(s)) break;
            picturesNumber++;
            CustomTextSliderView textSliderView = new CustomTextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(BraecoWaiterApplication.shopName)
                    .image(s)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", BraecoWaiterApplication.shopName);

            slider.addSlider(textSliderView);
        }

        slider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(3000);
        slider.addOnPageChangeListener(this);
        slider.setOnClickListener(this);
        slider.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time:
                startActivity(new Intent(this, MeFragmentShopTime.class));
                break;
            case R.id.back:
                finish();
                break;
            case R.id.empty_tip:
                BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE = false;
                startActivity(new Intent(mContext, MeFragmentShopPicture.class));
                break;
        }
    }

    private void getPictures() {
        BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_TASK_NUM++;
        new GetPicture(BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_TASK_NUM)
                .execute("http://brae.co/Dinner/Cover/Get");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE) {
            getPictures();
        }
        if (picturesNumber > 1) slider.startAutoCycle();
    }

    @Override
    protected void onStop() {
        slider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE = false;
        startActivity(new Intent(mContext, MeFragmentShopPicture.class));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    public boolean onLongClick(View v) {
        return true;
    }

    private class GetPicture extends AsyncTask<String, Void, String> {

        private int task;

        public GetPicture(int task) {
            this.task = task;
        }

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
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
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
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (task != BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_TASK_NUM) return;

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "GetPictures:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        JSONArray jsonPictures;
                        jsonPictures = array.getJSONArray("covers");
                        boolean isSame = true;
                        for (int i = 0; i < jsonPictures.length(); i++) {
                            if (!(jsonPictures.getString(i) + BraecoWaiterApplication.pictureSize).equals(BraecoWaiterApplication.pictureAddress[i])) {
                                isSame = false;
                                break;
                            }
                        }
                        if (jsonPictures.length() < 5) {
                            if (!"".equals(BraecoWaiterApplication.pictureAddress[jsonPictures.length()])) {
                                isSame = false;
                            }
                        }
                        if (!isSame) {
                            for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
                                BraecoWaiterApplication.pictureAddress[i] = "";
                            }
                            for (int i = 0; i < jsonPictures.length(); i++) {
                                BraecoWaiterApplication.pictureAddress[i] = jsonPictures.getString(i) + BraecoWaiterApplication.pictureSize;
                            }
                            BraecoWaiterApplication.writePreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);
                            BraecoWaiterUtils.showToast(mContext, "门店图片有更新，正在加载图片");
                            loadPictures();
                        }
                    } else {
                        fail();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    fail();
                }
            } else {
                fail();
            }
        }
    }

    private void fail() {
        if (mContext != null) {
            new MaterialDialog.Builder(mContext)
                    .title("获取门店图片失败")
                    .content("网络连接失败")
                    .positiveText("确认")
                    .negativeText("重试")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.NEGATIVE) {
                                getPictures();
                            }
                        }
                    })
                    .show();
        }
    }
}
