package com.braeco.braecowaiter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnSetVipBalanceAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetVipExpAsyncTaskListener;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.Model.Vip;
import com.braeco.braecowaiter.Tasks.SetVipBalanceAsyncTask;
import com.braeco.braecowaiter.Tasks.SetVipExpAsyncTask;
import com.braeco.braecowaiter.UIs.TitleLayout;

public class ServiceVipFragmentInformation extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        TitleLayout.OnTitleActionListener {

    private int position;

    private TitleLayout title;

    private TextView name;
    private TextView id;
    private LinearLayout phoneLayout;
    private TextView phone;
    private TextView level;
    private TextView exp;
    private TextView balance;
    private TextView change;
    private TextView add;

    private final int MAX_EXP = 10000000;
    private int EXP = 0;

    private final int MAX_BALANCE = 10000;
    private int chargeBalance = 0;

    private Context mContext;

    private MaterialDialog progressDialog;

    private Vip mVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_vip_fragment_infomation);

        mContext = this;

        position = this.getIntent().getIntExtra("position", -1);

        if (position == -1) finish();

        mVip = BraecoWaiterApplication.vips.get(position);

        title = (TitleLayout)findViewById(R.id.title_layout);
        name = (TextView)findViewById(R.id.name);
        id = (TextView)findViewById(R.id.id);
        phoneLayout = (LinearLayout)findViewById(R.id.phone_layout);
        phone = (TextView)findViewById(R.id.phone);
        level = (TextView)findViewById(R.id.level);
        exp = (TextView)findViewById(R.id.exp);
        balance = (TextView)findViewById(R.id.balance);
        change = (TextView)findViewById(R.id.change);
        add = (TextView)findViewById(R.id.add);

        name.setText(BraecoWaiterApplication.vips.get(position).getNickname());
        id.setText(mVip.getDinnerId() + "");
        level.setText(BraecoWaiterApplication.vips.get(position).getLevel());
        String phoneString = BraecoWaiterApplication.vips.get(position).getPhone();
        if (phoneString == null) phoneLayout.setVisibility(View.GONE);
        else {
            phoneLayout.setVisibility(View.VISIBLE);
            phone.setText(phoneString);
        }
        exp.setText(mVip.getExp() + "");
        balance.setText("¥ " + String.format("%.2f", mVip.getBalance()));

        change.setOnClickListener(this);
        add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change:
                new MaterialDialog.Builder(this)
                        .title("积分修改")
                        .content("请填写修改后的积分。")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    new SetVipExpAsyncTask(mOnSetVipExpAsyncTaskListener, mVip.getId(), EXP)
                                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    progressDialog = new MaterialDialog.Builder(mContext)
                                            .title("修改积分中")
                                            .content("请稍候")
                                            .cancelable(false)
                                            .progress(true, 0)
                                            .show();
                                }
                            }
                        })
                        .alwaysCallInputCallback()
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if ("".equals(String.valueOf(input))) {
                                    EXP = 0;
                                } else {
                                    if (input.length() > 8) {
                                        EXP = MAX_EXP + 1;
                                    } else {
                                        EXP = Integer.parseInt(String.valueOf(input));
                                    }
                                    if (EXP > MAX_EXP) {
                                        dialog.setContent("积分值必须在0~10000000");
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                    } else {
                                        dialog.setContent("请填写修改后的积分。");
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    }
                                }
                            }
                        }).show();
                break;
            case R.id.add:
                new MaterialDialog.Builder(this)
                        .title("现金充值")
                        .content("请填写充值金额")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    materialDialog.dismiss();
                                    if (!BraecoWaiterUtils.notNull(mVip.getPhone())) {
                                        // no phone
                                        new MaterialDialog.Builder(mContext)
                                                .title("现金充值")
                                                .content("会员尚未绑定手机号，请输入会员手机号码")
                                                .inputType(InputType.TYPE_CLASS_NUMBER)
                                                .inputRangeRes(11, 11, R.color.red)
                                                .positiveText("确定")
                                                .negativeText("取消")
                                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        if (which.equals(DialogAction.POSITIVE)) {
                                                            new SetVipBalanceAsyncTask(mOnSetVipBalanceAsyncTaskListener, mVip.getId(), dialog.getInputEditText().getText().toString(), chargeBalance)
                                                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                            progressDialog = new MaterialDialog.Builder(mContext)
                                                                    .title("现金充值中")
                                                                    .content("请稍候")
                                                                    .cancelable(false)
                                                                    .progress(true, 0)
                                                                    .show();
                                                        }
                                                    }
                                                })
                                                .input("会员手机号码", "", new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                                    }
                                                })
                                                .show();
                                    } else {
                                        new SetVipBalanceAsyncTask(mOnSetVipBalanceAsyncTaskListener, mVip.getId(), null, chargeBalance)
                                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        progressDialog = new MaterialDialog.Builder(mContext)
                                                .title("现金充值中")
                                                .content("请稍候")
                                                .cancelable(false)
                                                .progress(true, 0)
                                                .show();
                                    }

                                }
                            }
                        })
                        .alwaysCallInputCallback()
                        .negativeText("取消")
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if ("".equals(String.valueOf(input))) {
                                    chargeBalance = 0;
                                } else {
                                    if (input.length() > 6) {
                                        chargeBalance = MAX_BALANCE + 1;
                                    } else {
                                        chargeBalance = Integer.parseInt(String.valueOf(input));
                                    }
                                }
                                if (chargeBalance > MAX_BALANCE || chargeBalance < 1) {
                                    dialog.setContent("充值金额必须在1~10000范围内");
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                } else {
                                    dialog.setContent("请填写充值金额");
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                }
                            }
                        }).show();
                break;
        }
    }

    private OnSetVipBalanceAsyncTaskListener mOnSetVipBalanceAsyncTaskListener = new OnSetVipBalanceAsyncTaskListener() {
        @Override
        public void success(int status, double balance, int exp) {
            if (progressDialog != null) progressDialog.dismiss();
            switch (status) {
                case 1:
                    String name = BraecoWaiterUtils.notNull(mVip.getNickname()) ? mVip.getNickname() : mVip.getId() + "";
                    mVip.setBalance(balance);
                    mVip.setExp(exp);
                    ServiceVipFragmentInformation.this.exp.setText(mVip.getExp() + "");
                    ServiceVipFragmentInformation.this.balance.setText("¥ " + String.format("%.2f", mVip.getBalance()));
                    new MaterialDialog.Builder(mContext)
                            .title("充值成功")
                            .content("已为会员 " + mVip.getNickname() + name + " 充值成功，当前余额¥ " + String.format("%.2f", mVip.getBalance()) + "，积分 " + mVip.getExp())
                            .positiveText("确定")
                            .show();
                    break;
                case 0:
                    new MaterialDialog.Builder(mContext)
                            .title("充值中")
                            .content("我们已经发送一条短信给会员，会员按照手机短信提示后回复短信即可充值成功，请提示会员留意手机信息")
                            .positiveText("确定")
                            .show();
                    break;
            }
            if (status == -1) fail("网络异常");
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            new MaterialDialog.Builder(mContext)
                    .title("充值失败")
                    .content(message)
                    .positiveText("确定")
                    .show();
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetVipExpAsyncTaskListener mOnSetVipExpAsyncTaskListener = new OnSetVipExpAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            mVip.setExp(EXP);
            exp.setText(EXP + "");
            new MaterialDialog.Builder(mContext)
                    .title("积分修改成功")
                    .content("修改后积分：" + EXP)
                    .positiveText("确认")
                    .show();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            new MaterialDialog.Builder(mContext)
                    .title("积分修改失败")
                    .content(message)
                    .positiveText("确认")
                    .show();
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {

    }

    @Override
    public void clickTitleEdit() {

    }
}
