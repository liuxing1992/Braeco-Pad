package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class MeFragmentSettingsPassword extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private TextView finish;
    private LinearLayout old;
    private EditText oldEdit;
    private LinearLayout newPassword;
    private EditText newEdit;
    private LinearLayout again;
    private EditText againEdit;

    private String passwordStr;

    private Context mContext;

    private LinearLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_password);

        mContext = this;

        finish = (TextView)findViewById(R.id.finish);
        old = (LinearLayout)findViewById(R.id.old);
        oldEdit = (EditText)findViewById(R.id.old_edit);
        newPassword = (LinearLayout)findViewById(R.id.new_password);
        newEdit = (EditText)findViewById(R.id.new_edit);
        again = (LinearLayout)findViewById(R.id.again);
        againEdit = (EditText)findViewById(R.id.again_edit);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        finish.setOnClickListener(this);
        old.setOnClickListener(this);
        newPassword.setOnClickListener(this);
        again.setOnClickListener(this);

        oldEdit.requestFocus();
        InputMethodManager keyboard
                = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(oldEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void commit() {
        if (!BraecoWaiterApplication.password.equals(oldEdit.getText().toString())) {
            new MaterialDialog.Builder(this)
                    .title("错误")
                    .content("原密码错误")
                    .positiveText("确认")
                    .show();
            return;
        }
        if ("".equals(newEdit.getText().toString())) {
            new MaterialDialog.Builder(this)
                    .title("错误")
                    .content("新密码不能为空")
                    .positiveText("确认")
                    .show();
            return;
        }
        if (!againEdit.getText().toString().equals(newEdit.getText().toString())) {
            new MaterialDialog.Builder(this)
                    .title("错误")
                    .content("两次输入的新密码不一致")
                    .positiveText("确认")
                    .show();
            return;
        }

        passwordStr = newEdit.getText().toString();
        BraecoWaiterUtils.showToast(MeFragmentSettingsPassword.this, "请稍候");
        new changePassword().execute("http://brae.co/User/Update/Profile",
                oldEdit.getText().toString(), newEdit.getText().toString());
    }

    private class changePassword extends AsyncTask<String, Void, String> {
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
            pairList.add(new BasicNameValuePair("password", BraecoWaiterUtils.getInstance().MD5(params[1])));
            pairList.add(new BasicNameValuePair("newpassword", params[2]));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode()==200) return showResponseResult(response);
                else if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
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
                    String msg = json.getString("message");
                    if (msg.equals("success")) {
                        BraecoWaiterUtils.showToast(MeFragmentSettingsPassword.this, "密码修改成功，请重新登录");
                        BraecoWaiterApplication.socket.close();
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

                        Waiter.getInstance().cleanNickName();
                        Waiter.getInstance().cleanSid();
                        Waiter.getInstance().cleanLastUseVersion();
                        Waiter.getInstance().cleanAuthority();

                        BraecoWaiterApplication.clearFragments();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                    } else {
                        BraecoWaiterUtils.showToast(MeFragmentSettingsPassword.this, "密码修改失败，请检查密码输入");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                BraecoWaiterUtils.showToast(MeFragmentSettingsPassword.this, "请求发送失败");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                commit();
                break;
            case R.id.old:
                oldEdit.requestFocus();
                break;
            case R.id.new_password:
                newEdit.requestFocus();
                break;
            case R.id.again:
                againEdit.requestFocus();
                break;
            case R.id.back:
                finish();
                break;
        }
    }

}
