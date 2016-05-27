package com.braeco.braecowaiter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.braeco.braecowaiter.Interfaces.OnGetTableAsyncTaskListener;
import com.braeco.braecowaiter.Model.Table;
import com.braeco.braecowaiter.Model.Waiter;
import com.braeco.braecowaiter.Tasks.GetTableAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;

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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class MainActivity extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        MessageBookFragmentRecyclerViewAdapter.OnDealListener,
        ServiceMenuFragment.OnGetMenu,
        ScreenListener.ScreenStateListener {

    private FrameLayout messageLY;
    private FrameLayout serviceLY;
    private FrameLayout meLY;

    private ImageView messageIcon;
    private ImageView serviceIcon;
    private ImageView meIcon;

    private TextView messageTV;
    private TextView serviceTV;
    private TextView meTV;

    private TextView orderNumberTV;
    private TextView queueNumberTV;
    private TextView serviceNumberTV;

    private FrameLayout tab_fy_1;
    private FrameLayout tab_fy_2;
    private FrameLayout tab_fy_3;

    private TextView tab_text_1;
    private TextView tab_text_2;
    private TextView tab_text_3;

    private TextView tab1;
    private TextView tab2;
    private TextView tab3;

    public static TextView messageNumberTV;

    private FragmentManager fragmentManager;

    private Context mContext;

    private Boolean fromLogin = false;

    private SocketHandler mHandler;
    public static Thread newThread = null;

    private String content;

    private FrameLayout topTab;

    private ScreenListener screenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        screenListener = new ScreenListener(this);
        screenListener.begin(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(mContext, R.color.toolbar_black));
        }

        new getVersion().execute("http://brae.co/Server/Check/Agent/Version");

        messageNumberTV = (TextView)findViewById(R.id.message_num);

        fragmentManager = getSupportFragmentManager();

        // if the waiter has not login, login first
        SharedPreferences sharedPreferences = this.getSharedPreferences("VALUE", MODE_PRIVATE);
        BraecoWaiterApplication.settingsNotification = sharedPreferences.getBoolean("SETTINGS_NOTIFICATION", true);
        BraecoWaiterApplication.settingsSound = sharedPreferences.getBoolean("SETTINGS_SOUND", true);
        BraecoWaiterApplication.settingsVibrate = sharedPreferences.getBoolean("SETTINGS_VIBRATE", true);
        if (!sharedPreferences.getBoolean("HAS_LOGIN", false)
                || sharedPreferences.getString("SID", null) == null
                || sharedPreferences.getString("LOGIN_URL", null) == null
                || sharedPreferences.getInt("PORT", -1) == -1
                || !BuildConfig.VERSION_NAME.equals(Waiter.getInstance().getLastUseVersion())
                || Waiter.getInstance().getAuthority() == Waiter.DEFAULT_AUTHORITY) {
            BraecoWaiterUtils.log("Waiter has not signed in");
            fromLogin = true;
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            fromLogin = false;
            BraecoWaiterUtils.log("Waiter has signed in");
            BraecoWaiterApplication.hasLogin = true;
            BraecoWaiterApplication.sid = sharedPreferences.getString("SID", null);
            BraecoWaiterApplication.token = sharedPreferences.getString("TOKEN", null);
            BraecoWaiterApplication.loginUrl = sharedPreferences.getString("LOGIN_URL", null);
            BraecoWaiterApplication.password = sharedPreferences.getString("PASSWORD", null);
            BraecoWaiterApplication.phone = sharedPreferences.getString("PHONE", null);
            BraecoWaiterApplication.port = sharedPreferences.getInt("PORT", -1);
            BraecoWaiterApplication.shopName = sharedPreferences.getString("SHOP_NAME", "");
            BraecoWaiterApplication.waiterLogo = sharedPreferences.getString("WAITER_LOGO", "");
            BraecoWaiterApplication.shopPhone = sharedPreferences.getString("SHOP_PHONE", "");
            BraecoWaiterApplication.address = sharedPreferences.getString("ADDRESS", "");
            BraecoWaiterApplication.alipay = sharedPreferences.getString("ALIPAY", "");
            BraecoWaiterApplication.wxpay = sharedPreferences.getBoolean("WXPAY_QR", false);
            BraecoWaiterApplication.orderHasDiscount = sharedPreferences.getBoolean("ORDER_HAS_DISCOUNT", true);
            BraecoWaiterApplication.readPreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);

            Waiter.getInstance().setLastUseVersion(BuildConfig.VERSION_NAME);

            BraecoWaiterApplication.LOAD_MENU_FAIL = false;
            BraecoWaiterApplication.LOADED_MENU = false;
            if (BraecoWaiterApplication.nowParentChoice == 1 && BraecoWaiterApplication.lastServiceChoice == 0) {
                if (BraecoWaiterApplication.serviceMenuFragment != null) {
                    BraecoWaiterApplication.serviceMenuFragment.setEmptyTip(1, "", View.VISIBLE);
                }
            }
            if (!BraecoWaiterApplication.LOADING_MENU) {
                BraecoWaiterApplication.LOADING_MENU = true;
                new getMenu().execute("http://brae.co/Dinner/Info/Get");
            }

            new GetTableAsyncTask(mOnGetTableAsyncTaskListener)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            BraecoWaiterUtils.showToast(mContext, "正在加载，请稍候");

            BraecoWaiterApplication.connectFlag = false;

            BraecoWaiterApplication.mSocketHandler = new SocketHandler();
            final InetSocketAddress isa
                    = new InetSocketAddress(BraecoWaiterApplication.loginUrl , BraecoWaiterApplication.port);
            newThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BraecoWaiterApplication.socket
                                = new Socket(BraecoWaiterApplication.loginUrl , BraecoWaiterApplication.port);
                        BraecoWaiterApplication.bufferedReaderIn
                                = new BufferedReader(new InputStreamReader(
                                BraecoWaiterApplication.socket.getInputStream()));
                        BraecoWaiterApplication.printWriterOut
                                = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                BraecoWaiterApplication.socket.getOutputStream())), true);
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        if (!BraecoWaiterApplication.socket.isClosed()) {
                                            if (BraecoWaiterApplication.socket.isConnected()) {
                                                if(BraecoWaiterApplication.socket != null) {
                                                    BraecoWaiterApplication.printWriterOut.print(
                                                            BraecoWaiterUtils.getInstance().newString("hello" , "\"" + BraecoWaiterApplication.sid + "\""));
                                                    BraecoWaiterApplication.printWriterOut.flush();
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (NullPointerException n) {
                                        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Socket is null.");
                                        n.printStackTrace();
                                    }
                                }
                            }
                        };
                        timer.schedule(task, 200);
                        while (true) {
                            try {
                                if (BraecoWaiterApplication.socket != null) {
                                    if (!BraecoWaiterApplication.socket.isClosed()) {
                                        if (BraecoWaiterApplication.socket.isConnected()) {
                                            if(BraecoWaiterApplication.socket != null) {
                                                try{
                                                    if ((content = BraecoWaiterApplication.bufferedReaderIn.readLine()) != null) {
                                                        content += "\n";
                                                        BraecoWaiterApplication.mSocketHandler.obtainMessage(0, content).sendToTarget();
                                                    } else {

                                                    }
                                                } catch (IOException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (NullPointerException n) {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Socket is null.");
                                n.printStackTrace();
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            });
            new IsAgainConnect();
            newThread.start();

            initViews();

            tab_fy_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BraecoWaiterApplication.nowParentChoice != 2) {
                        if (BraecoWaiterApplication.nowParentChoice == 0)
                            BraecoWaiterApplication.lastMessageChoice = 0;
                        if (BraecoWaiterApplication.nowParentChoice == 1)
                            BraecoWaiterApplication.lastServiceChoice = 0;
                        setTabSelection(BraecoWaiterApplication.nowParentChoice, -1);
                    }
                }
            });
            tab_fy_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BraecoWaiterApplication.nowParentChoice != 2) {
                        if (BraecoWaiterApplication.nowParentChoice == 0)
                            BraecoWaiterApplication.lastMessageChoice = 1;
                        if (BraecoWaiterApplication.nowParentChoice == 1)
                            BraecoWaiterApplication.lastServiceChoice = 1;
                        setTabSelection(BraecoWaiterApplication.nowParentChoice, -1);
                    }
                }
            });
            tab_fy_3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BraecoWaiterApplication.nowParentChoice != 2) {
                        if (BraecoWaiterApplication.nowParentChoice == 0)
                            BraecoWaiterApplication.lastMessageChoice = 2;
                        if (BraecoWaiterApplication.nowParentChoice == 1)
                            BraecoWaiterApplication.lastServiceChoice = 2;
                        setTabSelection(BraecoWaiterApplication.nowParentChoice, -1);
                    }
                }
            });

            BraecoWaiterApplication.nowParentChoice = 0;
            BraecoWaiterApplication.lastMessageChoice = 0;
            BraecoWaiterApplication.lastServiceChoice = 0;

            if ("OPEN_MESSAGE_SERVICE".equals(getIntent().getStringExtra("ACTION"))) {
                setTabSelection(0, 2);
            } else if ("OPEN_MESSAGE_ORDER".equals(getIntent().getStringExtra("ACTION"))) {
                setTabSelection(0, 0);
            } else {
                setTabSelection(0, 0);
            }

            setNum();
        }
    }

    @Override
    public void finish() {
        if (screenListener != null) screenListener.unregisterListener();
        super.finish();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (BraecoWaiterApplication.messageBookFragment == null && fragment instanceof MessageBookFragment) {
            BraecoWaiterApplication.messageBookFragment = (MessageBookFragment)fragment;
        } else if (BraecoWaiterApplication.messageQueueFragment == null && fragment instanceof MessageQueueFragment) {
            BraecoWaiterApplication.messageQueueFragment = (MessageQueueFragment)fragment;
        } else if (BraecoWaiterApplication.messageServiceFragment == null && fragment instanceof MessageServiceFragment) {
            BraecoWaiterApplication.messageServiceFragment = (MessageServiceFragment)fragment;
        } else if (BraecoWaiterApplication.serviceMenuFragment == null && fragment instanceof ServiceMenuFragment) {
            BraecoWaiterApplication.serviceMenuFragment = (ServiceMenuFragment)fragment;
        } else if (BraecoWaiterApplication.serviceVipFragment == null && fragment instanceof ServiceVipFragment) {
            BraecoWaiterApplication.serviceVipFragment = (ServiceVipFragment)fragment;
        } else if (BraecoWaiterApplication.serviceRecordFragment == null && fragment instanceof ServiceRecordFragment) {
            BraecoWaiterApplication.serviceRecordFragment = (ServiceRecordFragment)fragment;
        } else if (BraecoWaiterApplication.meFragment == null && fragment instanceof MeFragment) {
            BraecoWaiterApplication.meFragment = (MeFragment)fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("BraecoWaiter", (getIntent().getStringExtra("ACTION") == null ? "null" : getIntent().getStringExtra("ACTION")));
        if (BraecoWaiterApplication.JUST_GIVE_ORDER) {
            setTabSelection(0, 0);
        } else {
            if ("OPEN_MESSAGE_SERVICE".equals(getIntent().getStringExtra("ACTION"))) {
                setTabSelection(0, 2);
            } else if ("OPEN_MESSAGE_ORDER".equals(getIntent().getStringExtra("ACTION"))) {
                setTabSelection(0, 0);
            } else {

            }
        }
        setNum();
    }

// set tab selection////////////////////////////////////////////////////////////////////////////////
// set tab selection////////////////////////////////////////////////////////////////////////////////
// set tab selection////////////////////////////////////////////////////////////////////////////////
    private void setTabSelection(int now, int last) {
        if (last == -1 || last == 0 || last == 2) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", now + " " + last);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter",
                BraecoWaiterApplication.lastMessageChoice + " " + BraecoWaiterApplication.lastServiceChoice);
        BraecoWaiterApplication.nowParentChoice = now;
        if (last != -1) {
            switch (BraecoWaiterApplication.nowParentChoice) {
                case 0: BraecoWaiterApplication.lastMessageChoice = last; break;
                case 1: BraecoWaiterApplication.lastServiceChoice = last; break;
            }
        }
        clearSelection();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        setNum();
        setTab();
        switch(BraecoWaiterApplication.nowParentChoice) {
            case 0:
                topTab.setVisibility(View.VISIBLE);
                messageIcon.setImageResource(R.drawable.icon_message_checked);
                messageTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_light));
                topTab.setVisibility(View.VISIBLE);
                tab_text_1.setText("订单");
                tab_text_2.setText("排位");
                tab_text_3.setText("服务");
                tab_text_1
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                tab_text_2
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                tab_text_3
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                if (BraecoWaiterApplication.lastMessageChoice == 0) {
                    tab_text_1
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                if (BraecoWaiterApplication.lastMessageChoice == 1) {
                    tab_text_2
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                if (BraecoWaiterApplication.lastMessageChoice == 2) {
                    tab_text_3
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                switch (BraecoWaiterApplication.lastMessageChoice) {
                    case 0:
                        if (BraecoWaiterApplication.messageBookFragment == null) {
                            BraecoWaiterApplication.messageBookFragment = new MessageBookFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.messageBookFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.messageBookFragment);
                        }
                        if (BraecoWaiterApplication.JUST_GIVE_ORDER) {
                            BraecoWaiterApplication.JUST_GIVE_ORDER = false;
                            if (BraecoWaiterApplication.messageBookFragment != null) {
                                BraecoWaiterApplication.messageBookFragment.scrollToBottom();
                            }
                        }
                        break;
                    case 1:
                        if (BraecoWaiterApplication.messageQueueFragment == null) {
                            BraecoWaiterApplication.messageQueueFragment = new MessageQueueFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.messageQueueFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.messageQueueFragment);
                        }
                        break;
                    case 2:
                        if (BraecoWaiterApplication.messageServiceFragment == null) {
                            BraecoWaiterApplication.messageServiceFragment = new MessageServiceFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.messageServiceFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.messageServiceFragment);
                        }
                        break;
                }
                break;
            case 1:
                topTab.setVisibility(View.VISIBLE);
                serviceIcon.setImageResource(R.drawable.icon_service_checked);
                serviceTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_light));
                topTab.setVisibility(View.VISIBLE);
                tab_text_1.setText("辅助点餐");
                tab_text_2.setText("会员充值");
                tab_text_3.setText("流水订单");
                tab_text_1
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                tab_text_2
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                tab_text_3
                        .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
                if (BraecoWaiterApplication.lastServiceChoice == 0) {
                    tab_text_1
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                if (BraecoWaiterApplication.lastServiceChoice == 1) {
                    tab_text_2
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                if (BraecoWaiterApplication.lastServiceChoice == 2) {
                    tab_text_3
                            .setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.primaryYellow));
                }
                switch (BraecoWaiterApplication.lastServiceChoice) {
                    case 0:
                        if (BraecoWaiterApplication.serviceMenuFragment == null) {
                            BraecoWaiterApplication.serviceMenuFragment = new ServiceMenuFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.serviceMenuFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.serviceMenuFragment);
                        }
                        break;
                    case 1:
                        if (BraecoWaiterApplication.serviceVipFragment == null) {
                            BraecoWaiterApplication.serviceVipFragment = new ServiceVipFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.serviceVipFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.serviceVipFragment);
                        }
                        break;
                    case 2:
                        if (BraecoWaiterApplication.serviceRecordFragment == null) {
                            BraecoWaiterApplication.serviceRecordFragment = new ServiceRecordFragment();
                            transaction.add(R.id.content, BraecoWaiterApplication.serviceRecordFragment);
                        } else {
                            transaction.show(BraecoWaiterApplication.serviceRecordFragment);
                        }
                        break;
                }
                break;
            case 2: {
                meIcon.setImageResource(R.drawable.icon_shop_checked);
                meTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_light));
                topTab.setVisibility(View.GONE);
                if(BraecoWaiterApplication.meFragment == null) {
                    BraecoWaiterApplication.meFragment = new MeFragment();
                    transaction.add(R.id.content, BraecoWaiterApplication.meFragment);
                } else {
                    transaction.show(BraecoWaiterApplication.meFragment);
                }
                break;
            }
        }
        transaction.commit();
    }

    private void setTab() {
        tab1.setVisibility(View.INVISIBLE);
        tab2.setVisibility(View.INVISIBLE);
        tab3.setVisibility(View.INVISIBLE);
        if (BraecoWaiterApplication.nowParentChoice == 0) {
            if (BraecoWaiterApplication.lastMessageChoice == 0)
                tab1.setVisibility(View.VISIBLE);
            if (BraecoWaiterApplication.lastMessageChoice == 1)
                tab2.setVisibility(View.VISIBLE);
            if (BraecoWaiterApplication.lastMessageChoice == 2)
                tab3.setVisibility(View.VISIBLE);
        }
        if (BraecoWaiterApplication.nowParentChoice == 1) {
            if (BraecoWaiterApplication.lastServiceChoice == 0)
                tab1.setVisibility(View.VISIBLE);
            if (BraecoWaiterApplication.lastServiceChoice == 1)
                tab2.setVisibility(View.VISIBLE);
            if (BraecoWaiterApplication.lastServiceChoice == 2)
                tab3.setVisibility(View.VISIBLE);
        }
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (BraecoWaiterApplication.messageBookFragment != null)
            transaction.hide(BraecoWaiterApplication.messageBookFragment);
        if (BraecoWaiterApplication.messageQueueFragment != null)
            transaction.hide(BraecoWaiterApplication.messageQueueFragment);
        if (BraecoWaiterApplication.messageServiceFragment != null)
            transaction.hide(BraecoWaiterApplication.messageServiceFragment);
        if (BraecoWaiterApplication.serviceMenuFragment != null)
            transaction.hide(BraecoWaiterApplication.serviceMenuFragment);
        if (BraecoWaiterApplication.serviceVipFragment != null)
            transaction.hide(BraecoWaiterApplication.serviceVipFragment);
        if (BraecoWaiterApplication.serviceRecordFragment != null)
            transaction.hide(BraecoWaiterApplication.serviceRecordFragment);
        if (BraecoWaiterApplication.meFragment != null)
            transaction.hide(BraecoWaiterApplication.meFragment);
    }

    private void clearSelection() {
        messageIcon.setImageResource(R.drawable.icon_message_unchecked);
        serviceIcon.setImageResource(R.drawable.icon_service_unchecked);
        meIcon.setImageResource(R.drawable.icon_shop_unchecked);
        messageTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_gray));
        serviceTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_gray));
        meTV.setTextColor(ContextCompat.getColor(mContext, R.color.icon_gray));
    }

    private void initViews() {
        messageLY = (FrameLayout) findViewById(R.id.message_layout);
        messageLY.setOnClickListener(this);
        serviceLY = (FrameLayout) findViewById(R.id.service_layout);
        serviceLY.setOnClickListener(this);
        meLY = (FrameLayout) findViewById(R.id.me_layout);
        meLY.setOnClickListener(this);

        messageIcon = (ImageView) findViewById(R.id.message_icon);
        serviceIcon = (ImageView) findViewById(R.id.service_icon);
        meIcon = (ImageView) findViewById(R.id.me_icon);

        messageTV = (TextView)findViewById(R.id.message_text);
        serviceTV = (TextView)findViewById(R.id.service_text);
        meTV = (TextView)findViewById(R.id.me_text);

        orderNumberTV = (TextView)findViewById(R.id.order_num);
        queueNumberTV = (TextView)findViewById(R.id.queue_num);
        serviceNumberTV = (TextView)findViewById(R.id.service_num);

        tab_fy_1 = (FrameLayout)findViewById(R.id.tab_fy_1);
        tab_fy_2 = (FrameLayout)findViewById(R.id.tab_fy_2);
        tab_fy_3 = (FrameLayout)findViewById(R.id.tab_fy_3);

        tab_fy_1.setOnClickListener(this);
        tab_fy_2.setOnClickListener(this);
        tab_fy_3.setOnClickListener(this);

        tab_text_1 = (TextView)findViewById(R.id.tab_text_1);
        tab_text_2 = (TextView)findViewById(R.id.tab_text_2);
        tab_text_3 = (TextView)findViewById(R.id.tab_text_3);

        tab1 = (TextView)findViewById(R.id.tab_1);
        tab2 = (TextView)findViewById(R.id.tab_2);
        tab3 = (TextView)findViewById(R.id.tab_3);

        topTab = (FrameLayout)findViewById(R.id.top_tab);

        messageNumberTV = (TextView)findViewById(R.id.message_num);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
            case R.id.message_layout:
                setTabSelection(0, -1);
                break;
            case R.id.service_layout:
                setTabSelection(1, -1);
                break;
            case R.id.me_layout:
                setTabSelection(2, -1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeal() {
        setNum();
    }

    @Override
    public void onGetMenu(boolean firstTime) {
        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
        if (firstTime && BraecoWaiterApplication.nowParentChoice == 1 && BraecoWaiterApplication.lastServiceChoice == 0) {
            if (BraecoWaiterApplication.serviceMenuFragment != null) {
                BraecoWaiterApplication.serviceMenuFragment.setEmptyTip(1, "", View.VISIBLE);
            }
        }
        new getMenu().execute("http://brae.co/Dinner/Info/Get");
    }

    @Override
    public void onScreenOn() {
        BraecoWaiterData.VIP_NEEDS_PASSWORD = true;
    }

    @Override
    public void onScreenOff() {
    }

    @Override
    public void onUserPresent() {
    }

// again connect////////////////////////////////////////////////////////////////////////////////////
// again connect////////////////////////////////////////////////////////////////////////////////////
// again connect////////////////////////////////////////////////////////////////////////////////////
    public class IsAgainConnect implements Runnable {
        private static final boolean CONNECT = true;
        private static final int WAIT_TIME = 18000;
        public void run() {
            // TODO Auto-generated method stub
            reunion();
        }
        public IsAgainConnect() {
            initiate();
        }
        // run方法
        private void reunion() {
            while (CONNECT) {
                holdTime(WAIT_TIME);
            }
        }
        // 等待时间
        private void holdTime(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (BraecoWaiterApplication.connectFlag != true) {
                handle();
            } else {
                BraecoWaiterApplication.connectFlag = false;
            }
        }
        // 发送测试数
        // 异常处理
        private void handle() {
            try {
                Looper.prepare();
                BraecoWaiterApplication.socket.close();
                BraecoWaiterApplication.socket =
                        new Socket(BraecoWaiterApplication.loginUrl, BraecoWaiterApplication.port);
                BraecoWaiterApplication.bufferedReaderIn
                        = new BufferedReader(new InputStreamReader(
                        BraecoWaiterApplication.socket.getInputStream()));
                BraecoWaiterApplication.printWriterOut
                        = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        BraecoWaiterApplication.socket.getOutputStream())), true);
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                if (!BraecoWaiterApplication.socket.isClosed()) {
                                    if (BraecoWaiterApplication.socket.isConnected()) {
                                        if(BraecoWaiterApplication.socket != null) {
                                            BraecoWaiterApplication.allOrder = new ArrayList<Map<String, Object>>();
//                                        main.all = new ArrayList<Map<String, Object>>();
//                                        main.waidai = new ArrayList<Map<String, Object>>();
                                            BraecoWaiterApplication.printWriterOut
                                                    .print(BraecoWaiterUtils.getInstance()
                                                            .newString("hello" , "\"" + BraecoWaiterApplication.sid + "\""));
                                            BraecoWaiterApplication.printWriterOut.flush();
                                            BraecoWaiterApplication.connectFlag = false;
                                            break;
                                        }
                                    }
                                }
                            } catch (NullPointerException n) {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Socket is null.");
                                n.printStackTrace();
                            }
                        }
                        initiate();
                    }
                };
                timer.schedule(task, 200);

            } catch (Exception e1) {
                initiate();
            }
            Looper.loop();
        }
        // 启动重连线程
        private void initiate() {
            Thread isAgainThread = new Thread(IsAgainConnect.this);
            isAgainThread.start();
        }
    }

// socket handler///////////////////////////////////////////////////////////////////////////////////
// socket handler///////////////////////////////////////////////////////////////////////////////////
// socket handler///////////////////////////////////////////////////////////////////////////////////
    public class SocketHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BraecoWaiterApplication.connectFlag = true;
                    int st = ((String)msg.obj).indexOf("{\"type" , 0);
                    int ed = st;
                    while (ed < ((String)msg.obj).length()) {
                        try {
                            ed = ((String)msg.obj).indexOf("{\"type" , st + 1);
                            if (ed == -1) ed = ((String)msg.obj).length();
                            String jsonstr = (String) ((String)msg.obj).subSequence(st, ed - 1);
                            st = ed;
                            //将handler中发送过来的消息创建json对象
                            JSONObject obj = new JSONObject((String)jsonstr);
                            String type = obj.getString("type");
                            if ("error".equals(type)) {
// error socket/////////////////////////////////////////////////////////////////////////////////////
                                if ("Invalid sid".equals(obj.getString("msg"))
                                        || "Validation failure".equals(obj.getString("msg"))) {
//                                    if (BuildConfig.DEBUG) Log.d("BraecoWatier", obj.getString("msg"));
//                                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "登陆信息已过期，请重新登陆");
//                                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "SID: " + BraecoWaiterApplication.sid);
//                                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "TOKEN: " + BraecoWaiterApplication.token);
//                                    BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "登陆信息已过期，请重新登陆");
//                                    BraecoWaiterApplication.socket.close();
//                                    BraecoWaiterApplication.hasLogin = false;
//                                    BraecoWaiterApplication.writePreferenceBoolean(mContext, "HAS_LOGIN", false);
//                                    BraecoWaiterApplication.sid = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "SID", "");
//                                    BraecoWaiterApplication.token = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "TOKEN", "");
//                                    BraecoWaiterApplication.loginUrl = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "LOGIN_URL", "");
//                                    BraecoWaiterApplication.port = -1;
//                                    BraecoWaiterApplication.writePreferenceInt(mContext, "PORT", -1);
//                                    BraecoWaiterApplication.password = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "PASSWORD", "");
//                                    Waiter.getInstance().cleanNickName();
//                                    BraecoWaiterApplication.phone = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "PHONE", "");
//                                    BraecoWaiterApplication.shopName = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_NAME", "");
//                                    BraecoWaiterApplication.waiterLogo = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "WAITER_LOGO", "");
//                                    BraecoWaiterApplication.shopPhone = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "SHOP_PHONE", "");
//                                    BraecoWaiterApplication.address = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "ADDRESS", "");
//                                    BraecoWaiterApplication.alipay = "";
//                                    BraecoWaiterApplication.writePreferenceString(mContext, "ALIPAY", "");
//                                    BraecoWaiterApplication.wxpay = false;
//                                    BraecoWaiterApplication.writePreferenceBoolean(mContext, "WXPAY_QR", false);
//                                    BraecoWaiterApplication.orderHasDiscount = true;
//                                    BraecoWaiterApplication.writePreferenceBoolean(mContext, "ORDER_HAS_DISCOUNT", true);
//                                    for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
//                                        BraecoWaiterApplication.pictureAddress[i] = "";
//                                    }
//                                    BraecoWaiterApplication.writePreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);
//                                    // start the login activity
//                                    Intent intent = new Intent(mContext, LoginActivity.class);
//                                    mContext.startActivity(intent);
//                                    BraecoWaiterApplication.clearFragments();
//                                    BraecoWaiterApplication.exit();
//
////                                    SysApplication.getInstance().exit();
                                    BraecoWaiterUtils.forceToLoginFor401(mContext);
                                }
                                if ("Someone login your account".equals(obj.getString("msg"))) {
// re login socket//////////////////////////////////////////////////////////////////////////////////
                                    BraecoWaiterUtils.log("Duplicate Login");
                                    BraecoWaiterUtils.forceToLoginForDuplicateUser(mContext);
                                }
                                return ;
                            }
                            if ("ping".equals(type)) {
// ping socket//////////////////////////////////////////////////////////////////////////////////////
                                try {
                                    if (!BraecoWaiterApplication.socket.isClosed()) {
                                        if (BraecoWaiterApplication.socket.isConnected()) {
                                            BraecoWaiterApplication.printWriterOut.
                                                    println(BraecoWaiterUtils.getInstance().newString("ping" , null));
                                            BraecoWaiterApplication.printWriterOut.flush();
                                            return ;
                                        }
                                    }
                                } catch (NullPointerException n) {
                                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Socket is null.");
                                    n.printStackTrace();
                                }

                            }
                            if ("refresh".equals(type)) {
// refresh socket///////////////////////////////////////////////////////////////////////////////////
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refresh: "
                                        + BraecoWaiterApplication.password + " " + (obj.getJSONObject("msg")).getString("key"));
                                BraecoWaiterApplication.token
                                        = BraecoWaiterUtils.getInstance().MD5(
                                        BraecoWaiterApplication.certificate
                                                + BraecoWaiterApplication.password
                                                + (obj.getJSONObject("msg")).getString("key"));
                                BraecoWaiterApplication.writePreferenceString(mContext, "SID", BraecoWaiterApplication.sid);
                                BraecoWaiterApplication.writePreferenceString(mContext, "TOKEN", BraecoWaiterApplication.token);
                                BraecoWaiterApplication.writePreferenceString(mContext, "LOGIN_URL", BraecoWaiterApplication.loginUrl);
                                BraecoWaiterApplication.writePreferenceInt(mContext, "PORT", BraecoWaiterApplication.port);
                                BraecoWaiterApplication.writePreferenceString(mContext, "PASSWORD", BraecoWaiterApplication.password);
                                BraecoWaiterApplication.writePreferenceString(mContext, "PHONE", BraecoWaiterApplication.phone);
                                return;
                            }
                            if ("order".equals(type)) {
// order socket/////////////////////////////////////////////////////////////////////////////////////
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "ORDER SOCKET");
                                JSONObject obj1 = obj.getJSONObject("msg");
                                JSONArray array = obj1.getJSONArray("content");
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", obj1.toString());
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", array.toString());
                                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

                                String refund = "发起退款";

                                double prices = 0;
                                for (int i = 0 ; i < array.length() ; i++) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    JSONObject obj2 = array.getJSONObject(i);
                                    if (obj2.getInt("type") == 0) {
                                        // is meal
                                        map.put("id", obj2.getInt("id"));
                                        map.put("name", obj2.getString("name"));
                                        if ("会员充值".equals(obj2.getString("name"))) {
                                            refund = "不可退款";
                                        }
                                        map.put("price" , obj2.getDouble("price"));
                                        map.put("sum", obj2.getInt("sum"));
                                        prices += (Double)map.get("price") * (Integer)map.get("sum");
                                        JSONArray arr = obj2.getJSONArray("property");
                                        String[] str1 = new String[arr.length()];
                                        for (int j = 0 ; j < arr.length() ; j++) {
                                            str1[j] = arr.getString(j);
                                        }
                                        if (arr.length() > 0) {
                                            String properties = (String)map.get("name") + "（";
                                            for (int k = 0; k < arr.length(); k++) {
                                                if (k > 0) properties += " ";
                                                properties += str1[k];
                                            }
                                            properties += "）";
                                            if (i == array.length() - 1) {
                                                map.put("properties", properties);
                                            } else {
                                                map.put("properties", properties);
                                            }
                                        } else {
                                            if (i == array.length() - 1) {
                                                map.put("properties", map.get("name"));
                                            } else {
                                                map.put("properties", map.get("name"));
                                            }
                                        }
                                        map.put("isSet", false);
                                        map.put("property", str1);
                                        map.put("ableRefund", BraecoWaiterUtils.ableRefund(map));
                                        map.put("refundingOrEd", BraecoWaiterUtils.refundingOrEd(map));
                                    } else if (obj2.getInt("type") == 1) {
                                        // is set
                                        map.put("id", obj2.getInt("id"));
                                        map.put("name", obj2.getString("name"));
                                        if ("会员充值".equals(obj2.getString("name"))) {
                                            refund = "不可退款";
                                        }
                                        map.put("price" , obj2.getDouble("price"));
                                        map.put("sum", obj2.getInt("sum"));
                                        prices += (Double)map.get("price") * (Integer)map.get("sum");

                                        // for combos
                                        ArrayList<ArrayList<Map<String, Object>>> combos = new ArrayList<>();
                                        ArrayList<Pair<Map<String, Object>, Integer>> combosInPair = new ArrayList<>();

                                        JSONArray combosJSON = obj2.getJSONArray("property");

                                        map.put("refund_property", combosJSON);

                                        int combosSize = combosJSON.length();
                                        for (int j = 0; j < combosSize; j++) {
                                            JSONArray meals = combosJSON.getJSONArray(j);
                                            ArrayList<Map<String, Object>> combo = new ArrayList<>();
                                            int mealsSize = meals.length();
                                            for (int k = 0; k < mealsSize; k++) {
                                                Map<String, Object> meal = new HashMap<>();
                                                JSONArray propertiesJSON = meals.getJSONObject(k).getJSONArray("p");
                                                ArrayList<String> properties = new ArrayList<>();
                                                int propertiesSize = propertiesJSON.length();
                                                for (int u = 0; u < propertiesSize; u++) {
                                                    properties.add(propertiesJSON.getString(u));
                                                }
                                                meal.put("id", meals.getJSONObject(k).getInt("id"));
                                                meal.put("name", meals.getJSONObject(k).getString("name"));
                                                meal.put("properties", properties);
                                                combo.add(meal);
                                            }
                                            combos.add(combo);
                                        }
                                        // calculate the same meal
                                        for (ArrayList<Map<String, Object>> meals : combos) {
                                            for (Map<String, Object> meal : meals) {
                                                // for every meal(id, properties only)
                                                boolean exist = false;
                                                int index = 0;
                                                for (Pair<Map<String, Object>, Integer> pair : combosInPair) {
                                                    if (BraecoWaiterUtils.isSameSubMeal(pair.first, meal)) {
                                                        // this meal is put already
                                                        combosInPair.set(index, new Pair<Map<String, Object>, Integer>(meal, pair.second + 1));
                                                        exist = true;
                                                        break;
                                                    }
                                                    index++;
                                                }
                                                if (!exist) {
                                                    // this meal is not put
                                                    combosInPair.add(new Pair<Map<String, Object>, Integer>(meal, 1));
                                                }
                                            }
                                        }
                                        // write all the sub meals to property
                                        String setPropertiesString = "：";
                                        for (Pair<Map<String, Object>, Integer> p : combosInPair) {
                                            // get meal name
                                            setPropertiesString += "\n" + p.first.get("name");
                                            String propertiesString = "（";
                                            boolean isFirstProperty = true;
                                            for (String property : (ArrayList<String>)p.first.get("properties")) {
                                                if (!isFirstProperty) propertiesString += "、";
                                                isFirstProperty = false;
                                                propertiesString += property;
                                            }
                                            propertiesString += "）";
                                            if ("（）".equals(propertiesString)) {
                                                // this meal has no properties
                                                propertiesString = "";
                                            }
                                            setPropertiesString += propertiesString + " ×" + p.second;
                                        }
                                        if ("：".equals(setPropertiesString)) setPropertiesString = "";
                                        map.put("isSet", true);
                                        map.put("properties", map.get("name") + setPropertiesString);
                                        map.put("ableRefund", BraecoWaiterUtils.ableRefund(map));
                                        map.put("refundingOrEd", BraecoWaiterUtils.refundingOrEd(map));
                                    }
                                    list.add(map);
                                }

                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("prices", prices);
                                map.put("content" , list);
                                map.put("date", obj1.getString("time"));
                                map.put("table", obj1.getString("tableid"));
                                map.put("channel", obj1.getString("channel"));
                                map.put("id", obj1.getInt("orderid"));
                                if (prices == 0) refund = "已全额退款";
                                map.put("refund", refund);

                                for (int i = 0; i < BraecoWaiterApplication.allOrder.size(); i++) {
                                    if (BraecoWaiterApplication.allOrder.get(i).get("id").equals(map.get("id"))) {
                                        return;
                                    }
                                }

                                if (obj1.has("serial")) {
                                    map.put("serial", obj1.get("serial"));
                                } else {
                                    map.put("serial", "No.?");
                                }

                                map.put("change", obj1.getInt("change"));
                                if (obj1.getDouble("change") == -2) {
                                    map.put("type", "waiter");
                                } else {
                                    if (obj1.getDouble("change") == -1) {
                                        map.put("type", "已在线支付");
                                    } else {
                                        map.put("type", "餐到付款");
                                    }
                                }
                                map.put("phone", obj1.getString("phone"));
                                BraecoWaiterApplication.allOrder.add(map);
                                if ("外带".equals(obj1.getString("tableid"))) {
                                    BraecoWaiterApplication.hongdian[1]++;
                                    BraecoWaiterApplication.waidai.add(map);
                                } else {
                                    BraecoWaiterApplication.hongdian[0]++;
                                    BraecoWaiterApplication.all.add(map);
                                }
                                JSONObject json = new JSONObject();
                                json.put("content", "Received");
                                json.put("orderid", String.valueOf(obj1.getInt("orderid")));
                                BraecoWaiterApplication.printWriterOut.print(
                                        BraecoWaiterUtils.getInstance().newString("feedback" , json));
                                BraecoWaiterApplication.printWriterOut.flush();
                                setNum();
                                handler.sendEmptyMessage(0);
                                if (BuildConfig.DEBUG) BraecoWaiterUtils.getInstance().LogListMap(BraecoWaiterApplication.allOrder);
                                return;
                            }
// table change/////////////////////////////////////////////////////////////////////////////////////
                            if ("table_change".equals(type)) {
                                JSONObject table = obj.getJSONObject("msg");
                                String tableId = table.getString("table");
                                int status = table.getInt("status");
                                if (BraecoWaiterApplication.tables != null) {
                                    boolean found = false;
                                    for (Table t : BraecoWaiterApplication.tables) {
                                        if (tableId.equals(t.getId())) {
                                            t.setUsed(status == 1);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        BraecoWaiterApplication.tables.add(new Table(tableId, status == 1));
                                        BraecoWaiterUtils.sortTables();
                                    }
                                } else {
                                    BraecoWaiterApplication.tables = new ArrayList<>();
                                    BraecoWaiterApplication.tables.add(new Table(tableId, status == 1));
                                    BraecoWaiterUtils.sortTables();
                                }
                                return;
                            }
// table refresh////////////////////////////////////////////////////////////////////////////////////
                            if ("table_refresh".equals(type)) {
                                JSONArray tables = obj.getJSONArray("msg");
                                if (BraecoWaiterApplication.tables == null) BraecoWaiterApplication.tables = new ArrayList<>();
                                for (int i = 0; i < tables.length(); i++) {
                                    String tableId = tables.getString(i);
                                    boolean found = false;
                                    for (Table t : BraecoWaiterApplication.tables) {
                                        if (tableId.equals(t.getId())) {
                                            t.setUsed(true);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) BraecoWaiterApplication.tables.add(new Table(tableId, true));
                                }
                                BraecoWaiterUtils.sortTables();
                                return ;
                            }
// order change/////////////////////////////////////////////////////////////////////////////////////
                            if ("order_change".equals(type)) {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "order change: ");
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", obj.toString());
                                JSONObject obb = obj.getJSONObject("msg");
                                int s = obb.getInt("orderid");
                                for (int i = 0; i < BraecoWaiterApplication.allOrder.size(); i++) {
                                    if ((int)BraecoWaiterApplication.allOrder.get(i).get("id") == s) {
                                        BraecoWaiterApplication.allOrder.remove(i);
                                        break;
                                    }
                                }
                                if (BraecoWaiterApplication.allOrder.size() == 0) {
                                    NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    nMgr.cancel(0);
                                }
                                if (BraecoWaiterApplication.messageBookFragment != null) {
                                    BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter.notifyDataSetChanged();
                                }
                                // Todo bookfragment change adapter
                                setNum();
                                return;
                            }
// service//////////////////////////////////////////////////////////////////////////////////////////
                            if ("service".equals(type)) {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "SERVICE SOCKET");
                                JSONObject obb = obj.getJSONObject("msg");
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", obj.toString());
                                int t = obb.getInt("status");
                                //showof("1");
                                if (t == 1) {
                                    String s = obb.getString("table");
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("table", s);
                                    map.put("serve", "呼叫服务员");
                                    map.put("word", obb.getString("word"));
                                    map.put("id", obb.getInt("id"));
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                    map.put("date", sdf.format(calendar.getTime()));
                                    BraecoWaiterApplication.serve.add(map);
                                    handlerService.sendEmptyMessage(0);
                                } else {
                                    int id = obb.getInt("id");
                                    for (int i = 0 ; i < BraecoWaiterApplication.serve.size() ; i++) {
                                        if (id == (Integer)BraecoWaiterApplication.serve.get(i).get("id")) {
                                            BraecoWaiterApplication.serve.remove(i);
                                            break;
                                        }
                                    }
                                    if (BraecoWaiterApplication.serve.size() == 0) {
                                        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        nMgr.cancel(1);
                                    }
                                }
                                if (BraecoWaiterApplication.messageServiceFragmentRecyclerViewAdapter != null) {
                                    BraecoWaiterApplication.messageServiceFragmentRecyclerViewAdapter.notifyDataSetChanged();
                                }
                                if (BuildConfig.DEBUG) BraecoWaiterUtils.getInstance().LogListMap(BraecoWaiterApplication.serve);
                                setNum();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

// notification/////////////////////////////////////////////////////////////////////////////////////
// notification/////////////////////////////////////////////////////////////////////////////////////
// notification/////////////////////////////////////////////////////////////////////////////////////
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
//            if (BraecoWaiterApplication.messageBookFragment != null) {
//                BraecoWaiterApplication.messageBookFragment.onRefresh();
//            } else {
//                BraecoWaiterApplication.messageBookFragment = new MessageBookFragment();
//            }
            if (BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter != null) {
                BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter.notifyDataSetChanged();
            }

            if (!BraecoWaiterApplication.settingsNotification) return;

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("ACTION", "OPEN_MESSAGE_ORDER");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager mNotificationManager
                    = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.build().defaults = Notification.DEFAULT_ALL;
            if (BraecoWaiterApplication.settingsVibrate) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            } else {
            }
            if (BraecoWaiterApplication.settingsSound) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {

            }
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
            mBuilder.setContentTitle("杯口");
            mBuilder.setContentText("您有" + BraecoWaiterApplication.allOrder.size() + "条新的订单，请及时处理");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setTicker("杯口");
            mBuilder.setSmallIcon(R.drawable.braeco_logo);
            mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
            Notification notification = mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(0, notification);
        }
    };

    private Handler handlerService = new Handler(){
        @Override
        public void handleMessage(Message msg){
//            if (BraecoWaiterApplication.messageServiceFragment != null) {
//                BraecoWaiterApplication.messageServiceFragment.onRefresh();
//            } else {
//                BraecoWaiterApplication.messageServiceFragment = new MessageServiceFragment();
//            }

            if (!BraecoWaiterApplication.settingsNotification) return;

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("ACTION", "OPEN_MESSAGE_SERVICE");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 , intent , 0);

            NotificationManager mNotificationManager
                    = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.build().defaults = Notification.DEFAULT_ALL;
            if (BraecoWaiterApplication.settingsVibrate) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            } else {
            }
            if (BraecoWaiterApplication.settingsSound) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {

            }
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
            mBuilder.setContentTitle("杯口");
            mBuilder.setContentText("您有" + BraecoWaiterApplication.serve.size() + "条新的服务请求，请及时处理");
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setTicker("杯口");
            mBuilder.setSmallIcon(R.drawable.braeco_logo);
            mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
            Notification notification = mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(1, notification);
        }
    };

    public void setNum() {
        if (BraecoWaiterApplication.allOrder.size() +
                BraecoWaiterApplication.serve.size() == 0) {
            messageNumberTV.setVisibility(View.GONE);
        } else {
            messageNumberTV.setText(
                    BraecoWaiterApplication.allOrder.size() +
                            BraecoWaiterApplication.serve.size() + "");
            if (messageNumberTV.getText().toString().length() >= 3) {
                messageNumberTV.setText("···");
            }
            messageNumberTV.setVisibility(View.VISIBLE);
        }
        if (BraecoWaiterApplication.allOrder.size() == 0) {
            orderNumberTV.setVisibility(View.GONE);
        } else {
            orderNumberTV.setText(BraecoWaiterApplication.allOrder.size() + "");
            if (orderNumberTV.getText().toString().length() >= 3) {
                orderNumberTV.setText("···");
            }
            orderNumberTV.setVisibility(View.VISIBLE);
        }
        if (BraecoWaiterApplication.serve.size() == 0) {
            serviceNumberTV.setVisibility(View.GONE);
        } else {
            serviceNumberTV.setText(BraecoWaiterApplication.serve.size() + "");
            if (serviceNumberTV.getText().toString().length() >= 3) {
                serviceNumberTV.setText("···");
            }
            serviceNumberTV.setVisibility(View.VISIBLE);
        }
        if (BraecoWaiterApplication.nowParentChoice != 0) {
            orderNumberTV.setVisibility(View.GONE);
            queueNumberTV.setVisibility(View.GONE);
            serviceNumberTV.setVisibility(View.GONE);
        }
    }

    private OnGetTableAsyncTaskListener mOnGetTableAsyncTaskListener = new OnGetTableAsyncTaskListener() {
        @Override
        public void success() {

        }

        @Override
        public void fail(String message) {

        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

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
            if (BuildConfig.DEBUG) System.out.println("getMenu: " + result);
            if (result != null) {
                try {
                    writeMenu(new JSONArray(result));
                } catch (JSONException e) {
                    BraecoWaiterApplication.LOAD_MENU_FAIL = true;
                    BraecoWaiterUtils.showToast(mContext, "登录已过期，请重新登录");
                    e.printStackTrace();
                    if (BraecoWaiterApplication.nowParentChoice == 1 && BraecoWaiterApplication.lastServiceChoice == 0) {
                        if (BraecoWaiterApplication.serviceMenuFragment != null) {
                            BraecoWaiterApplication.serviceMenuFragment.setEmptyTip(0, "菜单加载失败，点击重新加载", View.VISIBLE);
                        }
                    }
                }
            } else {
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
        } catch (JSONException j) {
            j.printStackTrace();
        }
    }

// get version//////////////////////////////////////////////////////////////////////////////////////
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
                if (response.getStatusLine().getStatusCode()==200) return showResponseResult(response);
                else return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "get version: " + result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String msg = json.getString("result");
                    if (msg.equals("Already latest")) {

                    } else {
                        BraecoWaiterApplication.newVersion = true;
                        if ("Version too old".equals(msg)) {
                            UpdateAppManager updateManager = new UpdateAppManager(mContext);
                            updateManager.spec = json.getString("url");
                            updateManager.message = "您的版本过于陈旧，请更新后再使用";
                            updateManager.mustUpdate = true;
                            updateManager.checkUpdateInfo();
                        } else {
                            UpdateAppManager updateManager = new UpdateAppManager(mContext);
                            updateManager.spec = json.getString("url");
                            updateManager.message = "软件有新版本可以更新";
                            updateManager.mustUpdate = false;
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

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            exitBy2Click();      //����˫���˳�����
        }
        return false;
    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // ׼���˳�
            BraecoWaiterUtils.showToast(mContext, "再点击一次返回桌面");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
        }
    }

}
