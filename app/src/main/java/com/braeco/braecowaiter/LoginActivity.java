package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Table;
import com.braeco.braecowaiter.Model.Waiter;
import com.dd.CircularProgressButton;

import org.apache.http.Header;
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
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private CircularProgressButton btn_login;

    private boolean LOGINING = false;

    private EditText edt_username;
    private EditText edt_password;

    private Context mContext;

    private TextView phone;

    // onCreate/////////////////////////////////////////////////////////////////////////////////////////
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mContext = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(mContext, R.color.login_background));
        }

        SharedPreferences sharedPreferences = getSharedPreferences("VALUE", MODE_PRIVATE);

        edt_password = (EditText) findViewById(R.id.password);
        edt_username = (EditText) findViewById(R.id.username);

        edt_username.setText(sharedPreferences.getString("TIP_NAME", ""));
        if (edt_username.getText().toString().length() > 0) {
            edt_password.requestFocus();
            InputMethodManager keyboard
                    = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(edt_password, InputMethodManager.SHOW_IMPLICIT);
        }

        btn_login = (CircularProgressButton) findViewById(R.id.login1);
        btn_login.setIndeterminateProgressMode(true);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LOGINING) return;
                String username = edt_username.getText().toString();
                String password = edt_password.getText().toString();
                LOGINING = true;
                new GetLogin().execute("http://brae.co/Dinner/Waiter/Login", username, password);
                BraecoWaiterUtils.showToast(mContext, "正在登录");
                btn_login.setProgress(1);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        phone = (TextView) findViewById(R.id.forget);
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mContext)
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
		});
	}

	private class GetLogin extends AsyncTask<String, Void, String> {
		protected  String showResponseResult(HttpResponse response) {
  	        if (null == response) return null;

  	        Header[] headers = response.getHeaders("Set-Cookie");
  	        String headerstr = headers.toString();
  	        if (headers != null) {
  	        	for(int i = 0 ; i < headers.length ; i++) {
  	        		String cookie = headers[i].getValue();
  	        		String[] cookievalues = cookie.split(";");
  	        		for(int j = 0 ; j < cookievalues.length ; j++) {
  	        			String[] keyPair = cookievalues[j].split("=");
  	        			String key = keyPair[0].trim();
  	        			String value = keyPair.length > 1 ? keyPair[1].trim():"";
                        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", key + " = " + value);
  	        			if ("sid".equals(key)) {
                            BraecoWaiterApplication.sid = value;
                            mContext.getSharedPreferences("VALUE", MODE_PRIVATE).edit()
                                    .putString("SID", BraecoWaiterApplication.sid)
                                    .commit();
                            mContext.getSharedPreferences("VALUE", MODE_PRIVATE).edit()
                                    .putBoolean("HAS_LOGIN", true)
                                    .commit();
                        }
  	        		}
  	        	}
  	        }

  	        HttpEntity httpEntity = response.getEntity();
  	        try {
  	            InputStream inputStream = httpEntity.getContent();
  	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
  	            String result = "";
  	            String line = "";
  	            while (null != (line = reader.readLine())) {
  	                result += line;
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "login every line" + line);
  	            }
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "login result" + result);
  	            return result;
  	        } catch (Exception e) {
  	            e.printStackTrace();
  	        }
			return null;
  	    }
  		@Override
  	    protected String doInBackground(String... params) {
  			List<NameValuePair> pairList = new ArrayList<NameValuePair>();
  			pairList.add(new BasicNameValuePair("username", params[1]));
  			pairList.add(new BasicNameValuePair("password", BraecoWaiterUtils.getInstance().MD5(params[2])));
  			try {
  				HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) return showResponseResult(response);
//                if (response.getStatusLine().getStatusCode() == 400) {
//                    // Todo
//                    Toast.makeText(LoginActivity.this, "账户不符合邮箱或手机", Toast.LENGTH_LONG).show();
//                    return null;
//                }
			} catch (IOException e) {
				e.printStackTrace();
			}
  			return null;
  	    }
  	    @Override
  	    protected void onPostExecute(String result) {
//			String showResult = result;
//            while (true) {
//                Log.d("BraecoWaiter", showResult.substring(0, showResult.length() >= 1000 ? 1000 : showResult.length()));
//                if (showResult.length() <= 1000) break;
//                showResult = showResult.substring(1000);
//            }
			LOGINING = false;
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "get login: " + result);
  	    	if (result != null) {
  	        	JSONObject array;
				try {
					array = new JSONObject(result);
					if ("success".equals(array.getString("message"))) {
						LOGINING = true;
						btn_login.setProgress(100);
						BraecoWaiterUtils.showToast(mContext, "登录成功");
	  	        		// Todo record the sid
						ArrayList<Table> newTables = new ArrayList<>();
						JSONArray tables = array.getJSONArray("table");
						for (int i = 0; i < tables.length(); i++) {
							// Notice that if the table is existed,
							// we should not change the status of the table.
							if ("外带".equals(tables.getString(i))) continue;
							String id = tables.getString(i);
							boolean existed = false;
							int position = -1;
							for (int j = 0; BraecoWaiterApplication.tables != null && j < BraecoWaiterApplication.tables.size(); j++) {
								Table table = BraecoWaiterApplication.tables.get(j);
								if (table.getId() != null && table.getId().equals(id)) {
									existed = true;
									position = j;
									break;
								}
							}
							if (existed) {
								newTables.add(new Table(id, BraecoWaiterApplication.tables.get(position).isUsed()));
							} else {
								newTables.add(new Table(id, false));
							}
						}
						BraecoWaiterApplication.tables = newTables;
						BraecoWaiterUtils.sortTables();

                        BraecoWaiterApplication.token = array.getString("token");
						BraecoWaiterApplication.writePreferenceString(mContext, "TOKEN", BraecoWaiterApplication.token);
                        BraecoWaiterApplication.loginUrl = array.getString("url");
                        BraecoWaiterApplication.writePreferenceString(mContext, "LOGIN_URL", BraecoWaiterApplication.loginUrl);
                        BraecoWaiterApplication.password = edt_password.getText().toString();
                        BraecoWaiterApplication.writePreferenceString(mContext, "PASSWORD", BraecoWaiterApplication.password);
						Waiter.getInstance().setNickName(array.getString("usernick"));
                        BraecoWaiterApplication.phone = array.getString("phone");
                        BraecoWaiterApplication.writePreferenceString(mContext, "PHONE", BraecoWaiterApplication.phone);
                        BraecoWaiterApplication.port = array.getInt("port");
                        BraecoWaiterApplication.writePreferenceInt(mContext, "PORT", BraecoWaiterApplication.port);
						BraecoWaiterApplication.shopName = array.getString("dinnername");
						BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_NAME", BraecoWaiterApplication.shopName);
                        BraecoWaiterApplication.waiterLogo = array.getString("eateravatar");
                        BraecoWaiterApplication.writePreferenceString(mContext, "WAITER_LOGO", BraecoWaiterApplication.waiterLogo);
						BraecoWaiterApplication.shopPhone = array.getString("dinnerphone");
						BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_PHONE", BraecoWaiterApplication.shopPhone);
						BraecoWaiterApplication.address = array.getString("address");
						BraecoWaiterApplication.writePreferenceString(mContext, "ADDRESS", BraecoWaiterApplication.address);
						BraecoWaiterApplication.alipay = array.getString("alipay_qr");
						BraecoWaiterApplication.writePreferenceString(mContext, "ALIPAY", BraecoWaiterApplication.alipay);
						BraecoWaiterApplication.wxpay = array.getBoolean("wxpay_qr");
						BraecoWaiterApplication.writePreferenceBoolean(mContext, "WXPAY_QR", BraecoWaiterApplication.wxpay);
						BraecoWaiterApplication.orderHasDiscount = array.getBoolean("order_has_discount");
						BraecoWaiterApplication.writePreferenceBoolean(mContext, "ORDER_HAS_DISCOUNT", BraecoWaiterApplication.orderHasDiscount);
						BraecoWaiterApplication.tipName = edt_username.getText().toString();
						BraecoWaiterApplication.writePreferenceString(mContext, "TIP_NAME", BraecoWaiterApplication.tipName);

						Waiter.getInstance().setLastUseVersion(BuildConfig.VERSION_NAME);
						Waiter.getInstance().setAuthority(array.getLong("auth"));

                        Log.d("BraecoWaiter", "use_member_balance" + array.getBoolean("use_member_balance"));
						Waiter.getInstance().setUseMemberBalance(array.getBoolean("use_member_balance"));

//						Toast.makeText(LoginActivity.this,
//                                BraecoWaiterApplication.loginUrl, Toast.LENGTH_LONG).show();
						JSONArray a = array.getJSONArray("dinnerinfo");
						// new
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							public void run() {
								Intent intent = new Intent(mContext, MainActivity.class);
								startActivity(intent);
								finish();
							}
						}, 1500);
	  	        	} else if ("Version too old".equals(array.getString("message"))) {
						btn_login.setProgress(0);
						BraecoWaiterUtils.showToast(mContext, "软件版本过低，请到官网下载最新版本");
                        UpdateAppManager updateManager = new UpdateAppManager(mContext);
                        updateManager.spec = array.getString("url");
                        updateManager.message = "您的版本过于陈旧，请更新后再使用";
                        updateManager.checkUpdateInfo();
                    } else if ("Is not waiter of any dinner".equals(array.getString("message"))) {
						btn_login.setProgress(0);
						BraecoWaiterUtils.showToast(mContext, "该账户非本店服务员，请核对后重新登陆");
                    } else if ("User not found".equals(array.getString("message"))) {
						btn_login.setProgress(0);
						BraecoWaiterUtils.showToast(mContext, "该账户不存在，请核对后重新登陆");
                    } else if ("Wrong password".equals(array.getString("message"))) {
						btn_login.setProgress(0);
						BraecoWaiterUtils.showToast(mContext, "输入密码错误，请核对后重新登陆");
                    } else {
						btn_login.setProgress(0);
						BraecoWaiterUtils.showToast(mContext, "登陆失败");
                    }
				} catch (JSONException e) {
					btn_login.setProgress(0);
					e.printStackTrace();
				}

  	        } else {
				btn_login.setProgress(0);
				BraecoWaiterUtils.showToast(mContext, "网络连接失败");
  	        }
  	    }
    }
}
