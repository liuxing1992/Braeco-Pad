package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.MySlideOutDownAnimator;
import com.braeco.braecowaiter.UIs.MyWaitAnimation;
import com.braeco.braecowaiter.UIs.ObservableScrollView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.CircularProgressButton;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;
import com.nineoldandroids.animation.Animator;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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

public class MeFragmentMenu extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        MeFragmentMenuAdapter.OnCheckListener {

    private static final int WAIT_COMPLETELY = 1;

    private LinearLayout back;
    private LinearLayout all;
    private LinearLayout buttons;
    private TextView edit;
    private TextView allText;

    private TextView change;
    private TextView delete;
    private TextView beTop;

    private SwipeRefreshLayout refresh;
    private CircularProgressButton reload;
    private ObservableScrollView scrollView;
    private ExpandedListView listview;
    private MeFragmentMenuAdapter adapter;

    private AnimCheckBox allCheck;

    private Boolean isEdit = false;
    private ArrayList<Boolean> isCheck = new ArrayList<>();

    private Context mContext;

    private Boolean wait = true;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu);

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        edit = (TextView)findViewById(R.id.edit);
        edit.setOnClickListener(this);
        edit.setVisibility(View.INVISIBLE);

        all = (LinearLayout)findViewById(R.id.all);
        allText = (TextView)findViewById(R.id.all_text);
        allCheck = (AnimCheckBox)findViewById(R.id.check);

        all.setOnClickListener(this);
        all.setVisibility(View.INVISIBLE);
        allCheck.setChecked(false);
        allCheck.setOnClickListener(this);

        refresh = (SwipeRefreshLayout)findViewById(R.id.refresh);
        scrollView = (ObservableScrollView)findViewById(R.id.scrollView);
        listview = (ExpandedListView)findViewById(R.id.list_view);

        buttons = (LinearLayout)findViewById(R.id.buttons);

        change = (TextView)findViewById(R.id.change);
        YoYo.with(Techniques.SlideOutDown).duration(0).playOn(change);
        change.setVisibility(View.GONE);
        change.setOnClickListener(this);

        delete = (TextView)findViewById(R.id.delete);
        YoYo.with(Techniques.SlideOutDown).duration(0).playOn(delete);
        delete.setVisibility(View.GONE);
        delete.setOnClickListener(this);

        beTop = (TextView)findViewById(R.id.betop);
        YoYo.with(Techniques.SlideOutDown).duration(0).playOn(beTop);
        beTop.setVisibility(View.GONE);
        beTop.setOnClickListener(this);

        reload = (CircularProgressButton)findViewById(R.id.reload);
        reload.setIndeterminateProgressMode(true);

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BraecoWaiterApplication.LOADING_MENU) {
                    BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                    BraecoWaiterApplication.LOADED_MENU = false;
                    BraecoWaiterApplication.LOADING_MENU = true;
                    reload.setProgress(1);
                    new getMenu().execute("http://brae.co/Dinner/Info/Get");
                }
            }
        });

        if (BraecoWaiterApplication.mButton == null || BraecoWaiterApplication.mMenu == null) {
            // the menu is loading or loading fail
            refresh.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
            listview.setVisibility(View.INVISIBLE);
            if (!BraecoWaiterApplication.LOADING_MENU) {
                if (BraecoWaiterApplication.LOAD_MENU_FAIL) {
                    // the menu is loading fail
                    reload.setIdleText("载入菜单失败，点击重新载入");
                    reload.setProgress(0);
                } else {
                    reload.setIdleText("点击载入菜单");
                    reload.setProgress(0);
                }
            } else {
                // the menu is loading
                reload.setProgress(1);
                new waitForLoading().execute();
            }
        } else {
            // the menu is loaded completely
            reload.setVisibility(View.INVISIBLE);
            load();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BraecoWaiterApplication.JUST_ADD_MENU) {
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                BraecoWaiterApplication.LOADED_MENU = false;
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
                BraecoWaiterUtils.showToast(mContext, "您刚刚添加了新餐品，正在刷新餐牌，请稍候");
            }
            BraecoWaiterApplication.JUST_ADD_MENU = false;
        }
        if (BraecoWaiterApplication.JUST_UPDATE_MENU) {
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                BraecoWaiterApplication.LOADED_MENU = false;
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
                BraecoWaiterUtils.showToast(mContext, "您刚刚更新了餐品，正在刷新餐牌，请稍候");
            }
            BraecoWaiterApplication.JUST_UPDATE_MENU = false;
        }
        if (BraecoWaiterApplication.JUST_REFRESH_MENU) {
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                BraecoWaiterApplication.LOADED_MENU = false;
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
                BraecoWaiterUtils.showToast(mContext, "您刚刚操作了餐牌，正在刷新餐牌，请稍候");
            }
            BraecoWaiterApplication.JUST_REFRESH_MENU = false;
        }
        if (BraecoWaiterData.JUST_ADD_CATEGORY) {
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                BraecoWaiterApplication.LOADED_MENU = false;
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
                BraecoWaiterUtils.showToast(mContext, "您刚刚新建了品类，正在刷新餐牌，请稍候");
            }
            BraecoWaiterData.JUST_ADD_CATEGORY = false;
        }
        if (BraecoWaiterData.JUST_UPDATE_CATEGORY) {
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                BraecoWaiterApplication.LOADED_MENU = false;
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
                BraecoWaiterUtils.showToast(mContext, "您刚刚更新了品类，正在刷新餐牌，请稍候");
            }
            BraecoWaiterData.JUST_UPDATE_CATEGORY = false;
        }
        if (isEdit) {
            onClick(edit);
        }
    }

    private void load() {
        // load tea
        findViewById(R.id.tea_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to tea settings
            }
        });
        findViewById(R.id.tea_layout).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // go to tea settings
                return false;
            }
        });
        ((TextView)findViewById(R.id.tea_number)).setText("茶位项：3");
        // load set
        findViewById(R.id.set_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to set settings
                mContext.startActivity(new Intent(mContext, MeFragmentMenuSet.class));
            }
        });
        findViewById(R.id.set_layout).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // go to set settings
                mContext.startActivity(new Intent(mContext, MeFragmentMenuSet.class));
                return false;
            }
        });
        if (BraecoWaiterApplication.mSet == null) ((TextView)findViewById(R.id.set_number)).setText("套餐数：" + 0);
        else ((TextView)findViewById(R.id.set_number)).setText("套餐数：" + BraecoWaiterApplication.mSet.size());
        // load menu
        edit.setVisibility(View.VISIBLE);
        refresh.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        listview.setVisibility(View.VISIBLE);
        listview.setFocusable(false);
        adapter = new MeFragmentMenuAdapter(this);
        listview.setAdapter(adapter);

        for (int i = 0; i < BraecoWaiterApplication.mButton.size(); i++) {
            isCheck.add(false);
        }

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == listview.getCount() - 1) {
                    if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) addCategory();
                    else AuthorityManager.showDialog(mContext, "新增品类");
                } else {
                    if (isEdit) {
                        View convertView = getViewByPosition(position, listview);
                        AnimCheckBox check = (AnimCheckBox) convertView.findViewById(R.id.check);
                        if (check.isChecked()) {
                            check.setChecked(false);
                        } else {
                            check.setChecked(true);
                        }
                        onCheck(position, check.isChecked());
                    } else {
                        onClick(edit);
                    }
                }
                return true;
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == listview.getCount() - 1) {
                    // new a category
                    if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) addCategory();
                    else AuthorityManager.showDialog(mContext, "新增品类");
                } else {
                    if (isEdit) {
                        View convertView = getViewByPosition(position, listview);
                        AnimCheckBox check = (AnimCheckBox) convertView.findViewById(R.id.check);
                        if (check.isChecked()) {
                            check.setChecked(false);
                        } else {
                            check.setChecked(true);
                        }
                        onCheck(position, check.isChecked());
                    } else {
                        if (position == 0) {
                            mContext.startActivity(new Intent(mContext, MeFragmentMenuSet.class));
                        } else {
                            Intent intent = new Intent(mContext, MeFragmentMenuMenu.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        }
                    }
                }

            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!BraecoWaiterApplication.LOADING_MENU) {
                    BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                    BraecoWaiterApplication.LOADED_MENU = false;
                    BraecoWaiterApplication.LOADING_MENU = true;
                    new getMenu().execute("http://brae.co/Dinner/Info/Get");
                }
            }
        });
    }

    private void addCategory() {
        Intent intent = new Intent(mContext, MeFragmentMenuEdit.class);
        intent.putExtra("position", -1);
        startActivity(intent);
    }

    @Override
    public void onCheck(int position, boolean check) {
        isCheck.set(position, check);
    }

    @Override
    public void onBackPressed() {
        if (isEdit) {
            onClick(edit);
            return;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.edit:
                for (int i = 0; i < listview.getCount() - 1; i++) {
                    View view = getViewByPosition(i, listview);
                    MaterialIconView icon = (MaterialIconView)view.findViewById(R.id.icon);
                    final AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);

                    if (!isEdit) {
                        icon.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeOut)
                                .duration(700)
                                .playOn(icon);
                        check.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.BounceInRight)
                                .duration(700)
                                .playOn(check);
                        isCheck.set(i, false);
                        check.setChecked(false);
                    } else {
                        icon.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeOut)
                                .duration(700)
                                .playOn(check);
                        check.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.BounceInRight)
                                .duration(700)
                                .playOn(icon);
                    }
                }
                if (!isEdit) {
                    all.setVisibility(View.VISIBLE);
                    allCheck.setChecked(false);
                    YoYo.with(Techniques.BounceInRight)
                            .duration(700)
                            .playOn(all);
                    change.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    beTop.setVisibility(View.VISIBLE);
                    DelayBounceInUp(0, change);
                    DelayBounceInUp(100, delete);
                    DelayBounceInUp(200, beTop);
                    edit.setText("完成");
                    refresh.setEnabled(false);
                } else {
                    YoYo.with(Techniques.FadeOut)
                            .duration(700)
                            .playOn(all);
                    YoYo.with(new MySlideOutDownAnimator())
                            .duration(700)
                            .delay(0)
                            .playOn(change);
                    YoYo.with(new MySlideOutDownAnimator())
                            .duration(700)
                            .delay(100)
                            .playOn(delete);
                    YoYo.with(new MySlideOutDownAnimator())
                            .duration(700)
                            .delay(200)
                            .playOn(beTop);
                    edit.setText("编辑");
                    refresh.setEnabled(true);
                }
                isEdit = !isEdit;
                break;
            case R.id.all:
            case R.id.check:
                if (allCheck.isChecked()) {
                    allCheck.setChecked(false);
                    for (int i = 0; i < listview.getCount() - 1; i++) {
                        View view = getViewByPosition(i, listview);
                        final AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
                        check.setChecked(false);
                        isCheck.set(i, false);
                    }
                    allText.setText("全选");
                } else {
                    allCheck.setChecked(true);
                    for (int i = 0; i < listview.getCount() - 1; i++) {
                        View view = getViewByPosition(i, listview);
                        final AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
                        check.setChecked(true);
                        isCheck.set(i, true);
                    }
                    allText.setText("全不选");
                }
                break;
            case R.id.change:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) change();
                else AuthorityManager.showDialog(mContext, "修改品类");
                break;
            case R.id.delete:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) delete();
                else AuthorityManager.showDialog(mContext, "删除品类");
                break;
            case R.id.betop:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) beTop();
                else AuthorityManager.showDialog(mContext, "置顶品类");
                break;
        }
    }

    private void DelayBounceInUp(long delay, final View view) {
        YoYo.with(new MyWaitAnimation())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        YoYo.with(Techniques.BounceInUp)
                                .duration(700)
                                .playOn(view);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .duration(delay)
                .playOn(back);
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void change() {
        int checkPosition = -1;
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                checkPosition = i;
                break;
            }
        }
        if (checkPosition == -1) {
            BraecoWaiterUtils.showToast(mContext, "请选择需要修改的品类");
        } else {
            Intent intent = new Intent(mContext, MeFragmentMenuEdit.class);
            intent.putExtra("position", checkPosition);
            startActivity(intent);
        }
    }

    private void delete() {
        final ArrayList<Integer> deleted = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                deleted.add(i);
            }
        }
        if (deleted.size() == 0) {
            BraecoWaiterUtils.showToast(mContext, "请选择需要删除的品类");
        } else {
            String content = "确定删除品类 ";
            for (int i = 0; i < deleted.size(); i++) {
                if (i > 0) content += "、";
                content += BraecoWaiterApplication.mButton.get(deleted.get(i)).get("button");
            }
            content += " 吗？";
            int sum = 0;
            String meals = "\n";
            meals += "以下属于上述品类的餐品都将被删除：\n";
            for (int i = 0; i < deleted.size(); i++) {
                meals += "属于品类 " + BraecoWaiterApplication.mButton.get(deleted.get(i)).get("button") + " 的餐品：\n";
                for (int j = BraecoWaiterApplication.a[deleted.get(i)]; j < BraecoWaiterApplication.a[deleted.get(i) + 1]; j++) {
                    meals += (String) BraecoWaiterApplication.mMenu.get(j).get("name") + "\n";
                    sum++;
                }
            }
            if (sum != 0) {
                content += meals;
            }
            new MaterialDialog.Builder(mContext)
                    .title("删除")
                    .content(content)
                    .positiveText("删除")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                progressDialog = new MaterialDialog.Builder(mContext)
                                        .title("正在删除")
                                        .content("请稍候")
                                        .cancelable(false)
                                        .progress(true, 0)
                                        .show();
                                JSONArray data = new JSONArray();
                                for (int i = 0; i < deleted.size(); i++) {
                                    data.put(BraecoWaiterApplication.mButton.get(deleted.get(i)).get("id"));
                                }
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "delete: " + data.toString());
                                new delete()
                                        .execute("http://brae.co/Category/Remove",
                                                data.toString());
                            }
                        }
                    })
                    .show();
        }
    }

    private void beTop() {
        final ArrayList<Integer> beToped = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                beToped.add(i);
            }
        }
        if (beToped.size() == 0) {
            BraecoWaiterUtils.showToast(mContext, "请选择需要置顶的品类");
        } else {
            String content = "确定置顶品类 ";
            for (int i = 0; i < beToped.size(); i++) {
                if (i > 0) content += "、";
                content += BraecoWaiterApplication.mButton.get(beToped.get(i)).get("button");
            }
            content += " 吗？";
            new MaterialDialog.Builder(mContext)
                    .title("置顶")
                    .content(content)
                    .positiveText("置顶")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                progressDialog = new MaterialDialog.Builder(mContext)
                                        .title("正在置顶")
                                        .content("请稍候")
                                        .cancelable(false)
                                        .progress(true, 0)
                                        .show();
                                JSONArray data = new JSONArray();
                                for (int i = 0; i < beToped.size(); i++) {
                                    data.put(BraecoWaiterApplication.mButton.get(beToped.get(i)).get("id"));
                                }
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "beTop: " + data.toString());
                                new beTop()
                                        .execute("http://brae.co/Category/Update/Top",
                                                data.toString());
                            }
                        }
                    })
                    .show();
        }
    }

    private class waitForLoading extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            while (!BraecoWaiterApplication.LOAD_MENU_FAIL
                    && BraecoWaiterApplication.LOADING_MENU
                    && !BraecoWaiterApplication.LOADED_MENU
                    && wait);
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (!wait) return;
            wait = false;
            Message msg = Message.obtain();
            msg.what = WAIT_COMPLETELY;
            waitCompletely.sendMessage(msg);
        }
    }

    //Handles messages from the MyAsyncTask
    Handler waitCompletely = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //What did that async task say?
            switch (msg.what) {
                case WAIT_COMPLETELY:
                    // the menu is loaded completely
                    if (!BraecoWaiterApplication.LOAD_MENU_FAIL) {
                        if (BraecoWaiterApplication.LOADED_MENU) {
                            reload.setVisibility(View.INVISIBLE);
                            load();
                        } else {
                            reload.setIdleText("点击载入菜单");
                            reload.setProgress(0);
                        }
                    } else {
                        reload.setIdleText("载入菜单失败，点击重新载入");
                        reload.setProgress(0);
                    }
                    break;
            }
        }
    };

    private class beTop extends AsyncTask<String, Void, String> {
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
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "beTop result: " + result);
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
//                httpPost.setEntity(requestHttpEntity);
                httpPost.setEntity(new StringEntity(params[1]));
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
            if (progressDialog != null) progressDialog.dismiss();
            if (BuildConfig.DEBUG) System.out.println("beTop result: " + result);
            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    if ("success".equals(jsonResult.getString("message"))
                            || "Already at top".equals(jsonResult.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "置顶成功，正在刷新");
                        if (isEdit) {
                            onClick(edit);
                        }
                        if (!BraecoWaiterApplication.LOADING_MENU) {
                            BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                            BraecoWaiterApplication.LOADED_MENU = false;
                            BraecoWaiterApplication.LOADING_MENU = true;
                            new getMenu().execute("http://brae.co/Dinner/Info/Get");
                        }
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }

    private class delete extends AsyncTask<String, Void, String> {
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
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "delete category result: " + result);
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
//                httpPost.setEntity(requestHttpEntity);
                httpPost.setEntity(new StringEntity(params[1]));
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
            if (progressDialog != null) progressDialog.dismiss();
            if (BuildConfig.DEBUG) System.out.println("delete category result: " + result);
            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    if ("success".equals(jsonResult.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "删除成功，正在刷新餐牌，请稍候");
                        if (isEdit) {
                            onClick(edit);
                        }
                        if (!BraecoWaiterApplication.LOADING_MENU) {
                            BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                            BraecoWaiterApplication.LOADED_MENU = false;
                            BraecoWaiterApplication.LOADING_MENU = true;
                            new getMenu().execute("http://brae.co/Dinner/Info/Get");
                        }
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "删除失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "删除失败");
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "删除失败");
            }
        }
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
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }
            BraecoWaiterApplication.LOADING_MENU = false;
            refresh.setRefreshing(false);
            if (BuildConfig.DEBUG) System.out.println("getMenu: " + result);
            if (result != null) {
                try {
                    writeMenu(new JSONArray(result));
                } catch (JSONException e) {
                    reload.setIdleText("载入菜单失败，点击重新载入");
                    reload.setProgress(0);
                    BraecoWaiterUtils.showToast(mContext, "登录已过期，请重新登录");
                    e.printStackTrace();
                    if (BraecoWaiterApplication.nowParentChoice == 1 && BraecoWaiterApplication.lastServiceChoice == 0) {
                        if (BraecoWaiterApplication.serviceMenuFragment != null) {
                            BraecoWaiterApplication.serviceMenuFragment.setEmptyTip(0, "菜单加载失败，点击重新加载", View.VISIBLE);
                        }
                    }
                }
            } else {
                reload.setIdleText("载入菜单失败，点击重新载入");
                reload.setProgress(0);
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                BraecoWaiterApplication.LOAD_MENU_FAIL = true;
                if (BraecoWaiterApplication.nowParentChoice == 1 && BraecoWaiterApplication.lastServiceChoice == 0) {
                    if (BraecoWaiterApplication.serviceMenuFragment != null) {
                        BraecoWaiterApplication.serviceMenuFragment.setEmptyTip(0, "菜单加载失败，点击重新加载", View.VISIBLE);
                    }
                }
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

            // show menu in activity
            if (adapter != null) {
                // load tea
                findViewById(R.id.tea_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // go to tea settings
                    }
                });
                findViewById(R.id.tea_layout).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // go to tea settings
                        return false;
                    }
                });
                ((TextView)findViewById(R.id.tea_number)).setText("茶位项：3");
                // load set
                findViewById(R.id.set_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // go to set settings
                        mContext.startActivity(new Intent(mContext, MeFragmentMenuSet.class));
                    }
                });
                findViewById(R.id.set_layout).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // go to set settings
                        mContext.startActivity(new Intent(mContext, MeFragmentMenuSet.class));
                        return false;
                    }
                });
                if (BraecoWaiterApplication.mSet == null) ((TextView)findViewById(R.id.set_number)).setText("套餐数：" + 0);
                else ((TextView)findViewById(R.id.set_number)).setText("套餐数：" + BraecoWaiterApplication.mSet.size());
                // load menu
                adapter.notifyDataSetChanged();
                edit.setVisibility(View.VISIBLE);
                isCheck = new ArrayList<>();
                for (int i = 0; i < BraecoWaiterApplication.mButton.size(); i++) {
                    isCheck.add(false);
                }
            } else {
                reload.setVisibility(View.GONE);
                load();
            }
        } catch (JSONException j) {
            j.printStackTrace();
        }
    }
}
