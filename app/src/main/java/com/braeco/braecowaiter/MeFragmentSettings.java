package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.Model.Waiter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;

public class MeFragmentSettings extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private LinearLayout back;
    private LinearLayout password;
    private LinearLayout message;
    private LinearLayout print;
    private LinearLayout help;
    private LinearLayout feedback;
    private LinearLayout update;
    private LinearLayout call;
    private TextView customServicePhone;
    private LinearLayout logout;

    private TagGroup updateEnable;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings);

        mContext = this;

        back = (LinearLayout) findViewById(R.id.back);
        password = (LinearLayout) findViewById(R.id.password);
        message = (LinearLayout) findViewById(R.id.message);
        print = (LinearLayout) findViewById(R.id.print);
        help = (LinearLayout) findViewById(R.id.help);
        feedback = (LinearLayout) findViewById(R.id.feedback);
        update = (LinearLayout) findViewById(R.id.update);
        call = (LinearLayout) findViewById(R.id.call);
        customServicePhone = (TextView) findViewById(R.id.custom_service_phone);
        logout = (LinearLayout) findViewById(R.id.logout);

        updateEnable = (TagGroup) findViewById(R.id.update_enable);

        back.setOnClickListener(this);
        password.setOnClickListener(this);
        message.setOnClickListener(this);
        print.setOnClickListener(this);
        help.setOnClickListener(this);
        feedback.setOnClickListener(this);
        update.setOnClickListener(this);
        call.setOnClickListener(this);
        customServicePhone.setText(BraecoWaiterData.CUSTOM_SERVICE_PHONE_SHOW);
        logout.setOnClickListener(this);

        if (BraecoWaiterApplication.newVersion) {
            updateEnable.setTags(new String[]{"新版本"});
            updateEnable.setVisibility(View.VISIBLE);
        } else {
            updateEnable.setVisibility(View.INVISIBLE);
        }

        updateEnable.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                checkForUpdate();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.password:
                startActivity(new Intent(this, MeFragmentSettingsPassword.class));
                break;
            case R.id.message:
                startActivity(new Intent(this, MeFragmentSettingsRemind.class));
                break;
            case R.id.print:
                startActivity(new Intent(this, MeFragmentSettingsPrint.class));
                break;
            case R.id.help:
                break;
            case R.id.feedback:
                break;
            case R.id.update:
                checkForUpdate();
                break;
            case R.id.call:
                callForHandsomeBoy();
                break;
            case R.id.logout:
                logout();
                break;
        }
    }

    private void checkForUpdate() {
        BraecoWaiterUtils.showToast(this, "检查更新中……");
        new getVersion().execute("http://brae.co/Server/Check/Agent/Version");
    }

    private void callForHandsomeBoy() {
        new MaterialDialog.Builder(this)
                .title("呼叫客服")
                .content("杯口，聆听您的一切。\n" + BraecoWaiterData.CUSTOM_SERVICE_PHONE_SHOW)
                .positiveText("呼叫")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            BraecoWaiterUtils.callForHelp(mContext);
                        }
                    }
                })
                .show();
    }

    private void logout() {
        new MaterialDialog.Builder(this)
                .title("退出登录")
                .content("确认退出吗？")
                .positiveText("确认")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            if (BraecoWaiterApplication.socket != null) {
                                try {
                                    BraecoWaiterApplication.socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            BraecoWaiterApplication.hasLogin = false;
                            BraecoWaiterApplication.writePreferenceBoolean(mContext, "HAS_LOGIN", false);
                            BraecoWaiterApplication.sid = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "SID", "");
                            BraecoWaiterApplication.token = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "TOKEN", "");
                            BraecoWaiterApplication.loginUrl = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "LOGIN_URL", "");
                            BraecoWaiterApplication.port = -1;
                            BraecoWaiterApplication.writePreferenceInt(mContext, "PORT", -1);
                            BraecoWaiterApplication.password = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "PASSWORD", "");
                            BraecoWaiterApplication.phone = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "PHONE", "");
                            BraecoWaiterApplication.shopName = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_NAME", "");
                            BraecoWaiterApplication.waiterLogo = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "WAITER_LOGO", "");
                            BraecoWaiterApplication.shopPhone = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_PHONE", "");
                            BraecoWaiterApplication.address = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "ADDRESS", "");
                            BraecoWaiterApplication.alipay = "";
                            BraecoWaiterApplication.writePreferenceString(mContext, "ALIPAY", "");
                            BraecoWaiterApplication.wxpay = false;
                            BraecoWaiterApplication.writePreferenceBoolean(mContext, "WXPAY_QR", false);
                            BraecoWaiterApplication.orderHasDiscount = true;
                            BraecoWaiterApplication.writePreferenceBoolean(mContext, "ORDER_HAS_DISCOUNT", true);
                            for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
                                BraecoWaiterApplication.pictureAddress[i] = "";
                            }
                            BraecoWaiterApplication.writePreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);
                            // start the login activity

                            Waiter.getInstance().cleanNickName();
                            Waiter.getInstance().cleanSid();
                            Waiter.getInstance().cleanLastUseVersion();
                            Waiter.getInstance().cleanAuthority();

                            BraecoWaiterApplication.clearFragments();
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            mContext.startActivity(intent);
                            BraecoWaiterApplication.exit();
                        }
                    }
                })
                .show();
    }

    public class getVersion extends AsyncTask<String, Void, String> {
        protected  String showResponseResult(HttpResponse response)
        {
            if (null == response)
            {
                return null;
            }

            HttpEntity httpEntity = response.getEntity();
            try
            {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine()))
                {
                    result += line;
                }
                //System.out.println(result);
                return result;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(new BasicNameValuePair("",""));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                else if (response.getStatusLine().getStatusCode()==200) return showResponseResult(response);
                else return null;
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

            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String msg = json.getString("result");
                    if (msg.equals("Already latest")) {
                        new MaterialDialog.Builder(mContext)
                                .title("已是最新版本")
                                .content("已经是最新版本。\n感谢您对杯口的支持。")
                                .positiveText("确认")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {

                                    }
                                })
                                .show();
                    } else {
                        if ("Version too old".equals(msg)) {
                            UpdateAppManager updateManager = new UpdateAppManager(mContext);
                            updateManager.spec = json.getString("url");
                            updateManager.message = "您的版本过于陈旧，请更新后再使用";
                            updateManager.checkUpdateInfo();
                        } else {
                            UpdateAppManager updateManager = new UpdateAppManager(mContext);
                            updateManager.spec = json.getString("url");
                            updateManager.message = "软件有新版本可以更新";
                            updateManager.checkUpdateInfo();
                        }
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } else {
            }
        }
    }
}
