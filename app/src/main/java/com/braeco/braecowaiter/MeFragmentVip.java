package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnGetVipAsyncTaskListener;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Tasks.GetVipAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import net.steamcrafted.materialiconlib.MaterialIconView;

public class MeFragmentVip extends BraecoAppCompatActivity
        implements
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        ServiceVipFragmentRecyclerViewAdapter.OnItemClickListener,
        View.OnClickListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;

    private SuperRecyclerView superRecyclerView;
    private ServiceVipFragmentRecyclerViewAdapter adapter;

    private MaterialIconView searchIcon;
    private EditText search;
    private MaterialIconView sort;

    private int SORT_TYPE = 3;
    private static final String[] SORT_NAMES = new String[]{"创建时间由远到近", "创建时间由近到远", "积分升序", "积分降序", "余额升序", "余额降序"};
    private static final String[] SORT_T = new String[]{"create_date", "create_date", "EXP", "EXP", "balance", "balance"};
    private static final String[] SORT_D = new String[]{"ASC", "DESC", "ASC", "DESC", "ASC", "DESC"};

    private TextView emptyTip;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_vip);

        mContext = this;

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        superRecyclerView = (SuperRecyclerView)findViewById(R.id.recyclerview);
        superRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        superRecyclerView.setRefreshListener(this);
        superRecyclerView.setRefreshingColorResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);
        superRecyclerView.setupMoreListener(this, Integer.MAX_VALUE);
        adapter = new ServiceVipFragmentRecyclerViewAdapter(this);
        superRecyclerView.setAdapter(adapter);
        adapter.SetOnItemClickListener(this);

        emptyTip = (TextView)superRecyclerView.getEmptyView().findViewById(R.id.empty_tip);

        searchIcon = (MaterialIconView)findViewById(R.id.search_icon);
        searchIcon.setOnClickListener(this);
        search = (EditText)findViewById(R.id.search);
        search.setHint("请输入会员id或5位手机号进行搜索");
        search.setOnClickListener(this);
        sort = (MaterialIconView)findViewById(R.id.sort);
        sort.setOnClickListener(this);

        if (AuthorityManager.ableTo(Authority.VIEW_VIP)) {
            search.addTextChangedListener(ableToViewTextWatcher);
        } else {
            emptyTip.setText("抱歉，您没有浏览会员信息的权限。如需开启该项权限，请联系店长对您的权限进行修改");
            search.setEnabled(false);
        }

        if (BraecoWaiterApplication.vips.size() == 0) {
            onRefresh();
        }
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (!AuthorityManager.ableTo(Authority.VIEW_VIP)) {
            superRecyclerView.hideMoreProgress();
            return;
        }
        if (BraecoWaiterApplication.vips.size() == BraecoWaiterApplication.maxVips) {
            superRecyclerView.hideMoreProgress();
            return;
        }
        getVip((0 < search.getText().toString().length() && search.getText().toString().length() <= 11) ? search.getText().toString() : null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {
        if (!AuthorityManager.ableTo(Authority.VIEW_VIP)) {
            MaterialDialog dialog = AuthorityManager.showDialog(mContext, "浏览会员信息");
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    superRecyclerView.getSwipeToRefresh().setRefreshing(false);
                }
            });
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (superRecyclerView != null)
                    superRecyclerView.getSwipeToRefresh().setRefreshing(false);
            }
        }, 3000);
        BraecoWaiterApplication.vips.clear();
        BraecoWaiterApplication.currentVipsPage = 1;
        adapter.notifyDataSetChanged();
        getVip((0 < search.getText().toString().length() && search.getText().toString().length() <= 11) ? search.getText().toString() : null);
    }

    private void getVip(String searchInfo) {
        new GetVipAsyncTask(mOnGetVipAsyncTaskListener, BraecoWaiterApplication.currentVipsPage, ++GetVipAsyncTask.TASK_ID)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        BraecoWaiterApplication.currentVipsPage + "",
                        GetVipAsyncTask.VIP_PER_PAGE + "",
                        SORT_T[SORT_TYPE],
                        SORT_D[SORT_TYPE],
                        searchInfo);
        BraecoWaiterApplication.currentVipsPage++;
    }

    @Override
    public void onItemClick(View view, final int position) {
        if (!AuthorityManager.ableTo(Authority.SET_BALANCE_EXP)) {
            AuthorityManager.showDialog(mContext, "为会员充值/修改积分");
            return;
        }
        if (BraecoWaiterData.VIP_NEEDS_PASSWORD) {
            new MaterialDialog.Builder(mContext)
                    .title("验证")
                    .content("请输入密码以进入会员修改界面")
                    .positiveText("确认")
                    .negativeText("取消")
                    .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                if (materialDialog.getInputEditText().getText().toString().equals(BraecoWaiterApplication.password)) {
                                    BraecoWaiterData.VIP_NEEDS_PASSWORD = false;
                                    Intent intent = new Intent(mContext, ServiceVipFragmentInformation.class);
                                    intent.putExtra("position", position);
                                    startActivity(intent);
                                } else {
                                    BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "密码错误，请重新输入");
                                }
                            }
                            if (dialogAction == DialogAction.NEUTRAL) {
                                materialDialog.dismiss();
                            }
                        }
                    })
                    .input(null, null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {

                        }
                    })
                    .show();
        } else {
            Intent intent = new Intent(mContext, ServiceVipFragmentInformation.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_icon:
                if (AuthorityManager.ableTo(Authority.VIEW_VIP)) {
                    search.requestFocus();
                    InputMethodManager keyboard
                            = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    AuthorityManager.showDialog(mContext, "浏览会员信息");
                }
                break;
            case R.id.sort:
                if (AuthorityManager.ableTo(Authority.VIEW_VIP)) {
                    new MaterialDialog.Builder(mContext)
                            .title("排序方式")
                            .items(SORT_NAMES)
                            .itemsCallbackSingleChoice(SORT_TYPE, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    SORT_TYPE = which;
                                    BraecoWaiterApplication.vips.clear();
                                    BraecoWaiterApplication.currentVipsPage = 1;
                                    adapter.notifyDataSetChanged();
                                    getVip((0 < search.getText().toString().length() && search.getText().toString().length() <= 11) ? search.getText().toString() : null);
                                    return true;
                                }
                            })
                            .show();
                } else {
                    AuthorityManager.showDialog(mContext, "浏览会员信息");
                }
                break;
        }
    }

    private OnGetVipAsyncTaskListener mOnGetVipAsyncTaskListener = new OnGetVipAsyncTaskListener() {
        @Override
        public void success() {
            adapter.notifyDataSetChanged();
            if (superRecyclerView != null) superRecyclerView.hideMoreProgress();
        }

        @Override
        public void fail(String message) {
            BraecoWaiterUtils.showToast(mContext, "获取会员失败（" + message + "）");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private TextWatcher ableToViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (search.getText().toString().length() < 6) {
                emptyTip.setText("加载会员数据中");
            } else {
                emptyTip.setText("搜索会员数据中");
            }
            if (search.getText().toString().length() == 0) {
                BraecoWaiterApplication.vips.clear();
                BraecoWaiterApplication.currentVipsPage = 1;
                adapter.notifyDataSetChanged();
                getVip(null);
                return;
            }
            if (0 < search.getText().toString().length() && search.getText().toString().length() <= 11) {
                BraecoWaiterApplication.vips.clear();
                BraecoWaiterApplication.currentVipsPage = 1;
                adapter.notifyDataSetChanged();
                getVip(search.getText().toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {
        superRecyclerView.getRecyclerView().scrollToPosition(0);
    }

    @Override
    public void clickTitleEdit() {
        if (AuthorityManager.ableTo(Authority.MANAGER_COUPONS)) {
            startActivity(new Intent(mContext, MeFragmentVipDiscount.class));
        } else {
            AuthorityManager.showDialog(mContext, "设置折扣");
        }
    }
}
