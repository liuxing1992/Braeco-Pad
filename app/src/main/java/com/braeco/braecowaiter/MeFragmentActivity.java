package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Enums.ActivityType;
import com.braeco.braecowaiter.Interfaces.OnGetActivityAsyncTaskListener;
import com.braeco.braecowaiter.Model.Activity;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Tasks.GetActivityAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.dd.CircularProgressButton;

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

public class MeFragmentActivity extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        CustomTextSliderView.OnSliderClickListener,
        TitleLayout.OnTitleActionListener,
        SwipeRefreshLayout.OnRefreshListener {

    private Context mContext;

    private SwipeRefreshLayout refreshLayout;
    private TitleLayout titleLayout;
    private ScrollView scrollView;
    private TextView emptyTip;
    private SliderLayout slider;
    private ExpandedListView listView;
    private MeFragmentActivityAdapter adapter;
    private CircularProgressButton reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_me_fragment_activity);

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        titleLayout = (TitleLayout)findViewById(R.id.title_layout);
        titleLayout.setOnTitleActionListener(this);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        emptyTip.setOnClickListener(this);
        reload = (CircularProgressButton)findViewById(R.id.reload);
        reload.setIndeterminateProgressMode(true);
        reload.setOnClickListener(this);

        BraecoWaiterApplication.pictureSize = "?imageView2/1/w/" + mContext.getResources().getDisplayMetrics().widthPixels
                + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);

        loadPictures();

        getPictures();

        getActivity();
    }

    private void loadPictures() {
        if ("".equals(BraecoWaiterApplication.pictureAddress[0])) {
            emptyTip.setVisibility(View.VISIBLE);
        } else {
            emptyTip.setVisibility(View.GONE);
        }

        slider = (SliderLayout) findViewById(R.id.slider);
        slider.removeAllSliders();

        for (String s : BraecoWaiterApplication.pictureAddress) {
            if ("".equals(s)) break;
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
        slider.setOnClickListener(this);
    }

    private void setListener() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (BraecoWaiterData.activities.get(position).getType()) {
                    case ADD:
                        if (position == BraecoWaiterData.activities.size() - 1) {
                            if (AuthorityManager.ableTo(Authority.MANAGER_ACTIVITY)) {
                                Intent intent = new Intent(mContext, MeFragmentActivityThemeEdit.class);
                                intent.putExtra("position", -1);
                                startActivity(intent);
                            } else {
                                AuthorityManager.showDialog(mContext, "新建主题活动");
                            }
                        } else {
                            if (AuthorityManager.ableTo(Authority.MANAGER_ACTIVITY)
                                    || AuthorityManager.ableTo(Authority.MANAGER_DISCOUNT)) {
                                Intent intent = new Intent(mContext, MeFragmentActivityReduceGiveOtherEdit.class);
                                intent.putExtra("position", -1);
                                startActivity(intent);
                            } else {
                                AuthorityManager.showDialog(mContext, "新建活动");
                            }
                        }
                        break;
                    case THEME:
                        if (AuthorityManager.ableTo(Authority.MANAGER_ACTIVITY)) {
                            Intent intent = new Intent(mContext, MeFragmentActivityThemeEdit.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        } else {
                            AuthorityManager.showDialog(mContext, "编辑主题活动");
                        }
                        break;
                    case OTHER:
                        if (AuthorityManager.ableTo(Authority.MANAGER_ACTIVITY)) {
                            Intent intent = new Intent(mContext, MeFragmentActivityReduceGiveOtherEdit.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        } else {
                            AuthorityManager.showDialog(mContext, "编辑其他促销活动");
                        }
                        break;
                    case REDUCE:
                    case GIVE:
                        if (AuthorityManager.ableTo(Authority.MANAGER_DISCOUNT)) {
                            Intent intent = new Intent(mContext, MeFragmentActivityReduceGiveOtherEdit.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        } else {
                            AuthorityManager.showDialog(mContext, "编辑促销活动");
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.empty_tip:
                BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE = false;
                startActivity(new Intent(mContext, MeFragmentShopPicture.class));
                break;
            case R.id.reload:
                getActivity();
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
            scrollView.scrollTo(0, 0);
            getPictures();
        }
        slider.startAutoCycle();
        if (BraecoWaiterApplication.JUST_ADD_ACTIVITY) {
            BraecoWaiterApplication.JUST_ADD_ACTIVITY = false;
            BraecoWaiterUtils.showToast(mContext,
                    "新建活动 " + BraecoWaiterApplication.JUST_ACTIVITY_NAME +  " 成功，正在刷新活动列表，请稍候");
            getActivity();
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        } else if (BraecoWaiterApplication.JUST_UPDATE_ACTIVITY) {
            BraecoWaiterApplication.JUST_UPDATE_ACTIVITY = false;
            BraecoWaiterUtils.showToast(mContext,
                    "修改活动 " + BraecoWaiterApplication.JUST_ACTIVITY_NAME +  " 成功，正在刷新活动列表，请稍候");
            getActivity();
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        } else if (BraecoWaiterApplication.JUST_REFRESH_ACTIVITY) {
            BraecoWaiterApplication.JUST_REFRESH_ACTIVITY = false;
            getActivity();
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        } else if (BraecoWaiterApplication.JUST_DELETE_ACTIVITY) {
            BraecoWaiterApplication.JUST_DELETE_ACTIVITY = false;
            BraecoWaiterUtils.showToast(mContext,
                    "删除活动 " + BraecoWaiterApplication.JUST_ACTIVITY_NAME +  " 成功，正在刷新活动列表，请稍候");
            getActivity();
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        BraecoWaiterData.ACTIVITY_LOADING = false;
    }

    private void getActivity() {
        if (!BraecoWaiterData.ACTIVITY_LOADING) {
            reload.setProgress(1);
            reload.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.INVISIBLE);
            BraecoWaiterData.ACTIVITY_LOADING = true;
            new GetActivityAsyncTask(mOnGetActivityAsyncTaskListener, ++GetActivityAsyncTask.TASK_ID)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void clickTitleEdit() {

    }

    @Override
    public void onRefresh() {
        getActivity();
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

    private OnGetActivityAsyncTaskListener mOnGetActivityAsyncTaskListener = new OnGetActivityAsyncTaskListener() {
        @Override
        public void success() {
            refreshLayout.setRefreshing(false);
            BraecoWaiterData.ACTIVITY_LOADING = false;
            reload.setProgress(0);
            reload.setIdleText("点击载入活动");
            reload.setVisibility(View.INVISIBLE);

            int lastSalePosition = -1;

            for (int i = 0; i < BraecoWaiterData.activities.size(); i++) {
                if (BraecoWaiterData.activities.get(i).getType().equals(ActivityType.REDUCE)
                        || BraecoWaiterData.activities.get(i).getType().equals(ActivityType.OTHER)
                        || BraecoWaiterData.activities.get(i).getType().equals(ActivityType.GIVE)) {
                    lastSalePosition = i;
                }
            }

            BraecoWaiterData.activities.add(lastSalePosition + 1, new Activity(ActivityType.SECTION_THEME));
            BraecoWaiterData.activities.add(lastSalePosition + 1, new Activity(ActivityType.ADD));
            BraecoWaiterData.activities.add(0, new Activity(ActivityType.SECTION_REDUCE));
            BraecoWaiterData.activities.add(BraecoWaiterData.activities.size(), new Activity(ActivityType.ADD));

            adapter = new MeFragmentActivityAdapter(mContext);
            listView = (ExpandedListView) findViewById(R.id.list_view);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);

            setListener();

            BraecoWaiterUtils.showToast(mContext, "活动载入完成");

            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }

        @Override
        public void fail(String message) {
            refreshLayout.setRefreshing(false);
            BraecoWaiterData.ACTIVITY_LOADING = false;
            getActivityFail();
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private void fail() {
        if (mContext == null) return;
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

    private void getActivityFail() {
        reload.setProgress(0);
        reload.setIdleText("载入活动失败，点击重新载入");
        reload.setVisibility(View.VISIBLE);
        if (listView != null) listView.setVisibility(View.INVISIBLE);
    }
}
