package com.braeco.braecowaiter.Model;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Weiping on 2016/5/20.
 */

public class AuthorityManager {

    public static boolean ableTo(long authority) {
        if (Waiter.getInstance().getAuthority() == -1) return false;
        else return (Waiter.getInstance().getAuthority() & authority) != 0;
    }

    public static MaterialDialog showDialog(Context context, String words) {
        return new MaterialDialog.Builder(context)
                .title("权限不足")
                .content("抱歉，您没有 " + words + " 的权限。如需开启该项权限，请联系店长对您的权限进行修改")
                .positiveText("确定")
                .show();
    }

    private static AuthorityManager ourInstance = new AuthorityManager();

    public static AuthorityManager getInstance() {
        return ourInstance;
    }

    private AuthorityManager() {
    }
}
