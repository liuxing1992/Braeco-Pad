package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnGetPrinterAsyncTaskListener;
import com.braeco.braecowaiter.Tasks.GetPrinterAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;

public class MeFragmentSettingsPrint extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        TitleLayout.OnTitleActionListener {

    private Context mContext;
    private MaterialDialog mLoadingDialog;

    private TitleLayout title;
    private ListView listView;
    private MeFragmentSettingsPrintAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_print);

        mContext = this;

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);
        listView = (ListView)findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, MeFragmentSettingsPrintSettings.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        load();
    }

    @Override
    protected void onResume() {
        if (adapter != null) adapter.notifyDataSetChanged();
        super.onResume();
    }

    private void load() {
        mLoadingDialog = new MaterialDialog.Builder(mContext)
                .title("加载中……")
                .content("正在加载打印机信息，请耐心等候。")
                .cancelable(true)
                .progress(true, 0)
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .show();

        new GetPrinterAsyncTask(mOnGetPrinterAsyncTaskListener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    private OnGetPrinterAsyncTaskListener mOnGetPrinterAsyncTaskListener = new OnGetPrinterAsyncTaskListener() {
        @Override
        public void success() {
            refreshLayout.setRefreshing(false);
            if (mLoadingDialog != null) mLoadingDialog.dismiss();
            adapter = new MeFragmentSettingsPrintAdapter();
            listView.setAdapter(adapter);

            if (BraecoWaiterApplication.printers.size() == 0) {
                new MaterialDialog.Builder(mContext)
                        .title("打印机数量为零")
                        .content("您的餐厅打印机数量为零，是否需要联系客服为您安装打印机？")
                        .cancelable(false)
                        .positiveText("联系客服")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which.equals(DialogAction.POSITIVE)) {
                                    BraecoWaiterUtils.callForHelp(mContext);
                                } else if (which.equals(DialogAction.NEGATIVE)) {
                                    finish();
                                }
                            }
                        })
                        .show();
            }
        }

        @Override
        public void fail() {
            refreshLayout.setRefreshing(false);
            if (mLoadingDialog != null) mLoadingDialog.dismiss();
            new MaterialDialog.Builder(mContext)
                    .title("加载失败")
                    .content("加载打印机信息失败，是否重新加载？")
                    .cancelable(true)
                    .positiveText("重新加载")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which.equals(DialogAction.POSITIVE)) {
                                dialog.dismiss();
                                load();
                            } else if (which.equals(DialogAction.NEGATIVE)) {
                                finish();
                            }
                        }
                    })
                    .show();
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
            case R.id.finish:
                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {
        load();
    }

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {
        listView.setSelection(0);
    }

    @Override
    public void clickTitleEdit() {
        finish();
    }
}
