package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.os.Bundle;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;

public class SignOutDialogActivity extends BraecoAppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_out_dialog);

        mContext = this;

        switch (getIntent().getIntExtra("type", -1)) {
            case -1:
                break;
            case 0:
                duplicateLogin();
                break;
            case 1:
                overdueLogin();
                break;
        }
    }

    private void duplicateLogin() {
        BraecoWaiterUtils.cleanData();
        if (!((Activity)mContext).isFinishing()) {
            new MaterialDialog.Builder(mContext)
                    .title("下线通知")
                    .content("您的账号在其他地方登录，如果不是您本人操作，请修改密码（我-设置-修改密码）或联系管理员，点击确定重新登录")
                    .cancelable(false)
                    .positiveText("确定")
                    .negativeText("联系管理员")
                    .negativeColorRes(R.color.primaryBrown)
                    .autoDismiss(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which.equals(DialogAction.POSITIVE)) {
                                dialog.dismiss();
                                BraecoWaiterUtils.forceToLogin(mContext);
                            } else if (which.equals(DialogAction.NEGATIVE)) {
                                BraecoWaiterUtils.callForHelp(mContext);
                            }
                        }
                    })
                    .show();
        }
    }

    private void overdueLogin() {
        BraecoWaiterUtils.cleanData();
        if (!((Activity)mContext).isFinishing()) {
            new MaterialDialog.Builder(mContext)
                    .title("权限改变")
                    .content("您的权限发生改变，请点击确定重新登录")
                    .cancelable(false)
                    .positiveText("确定")
                    .autoDismiss(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which.equals(DialogAction.POSITIVE)) {
                                dialog.dismiss();
                                BraecoWaiterUtils.forceToLogin(mContext);
                            }
                        }
                    })
                    .show();
        }
    }
}
