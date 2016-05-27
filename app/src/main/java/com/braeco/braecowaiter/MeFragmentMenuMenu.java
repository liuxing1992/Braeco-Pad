package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnSetDishDisableAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetDishEnableAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetDishRemoveAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetDishUpdateCategoryAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetDishUpdateTopAsyncTaskListener;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Tasks.SetDishDeleteAsyncTask;
import com.braeco.braecowaiter.Tasks.SetDishDisableAsyncTask;
import com.braeco.braecowaiter.Tasks.SetDishEnableAsyncTask;
import com.braeco.braecowaiter.Tasks.SetDishUpdateCategoryAsyncTask;
import com.braeco.braecowaiter.Tasks.SetDishUpdateTopAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.MySlideOutDownAnimator;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeFragmentMenuMenu extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        MeFragmentMenuMenuAdapter.OnCheckListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;

    private TextView sale;
    private TextView change;
    private TextView delete;
    private TextView beTop;

    private ListView listView;
    private MeFragmentMenuMenuAdapter adapter;

    private int buttonPosition = -1;
    private Boolean isSingleEdit = false;
    private Boolean isEditing = false;
    private ArrayList<Boolean> isCheck = new ArrayList<>();

    private Context mContext;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_menu);

        mContext = this;

        buttonPosition = getIntent().getIntExtra("position", -1);

        title = (TitleLayout)findViewById(R.id.title_layout);
        if (title != null) title.setOnTitleActionListener(this);

        sale = (TextView)findViewById(R.id.sale);
        YoYo.with(Techniques.SlideOutDown).duration(0).playOn(sale);
        sale.setVisibility(View.GONE);
        sale.setOnClickListener(this);

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

        for (int i = 0; i < BraecoWaiterApplication.b[buttonPosition + 1] - BraecoWaiterApplication.b[buttonPosition]; i++) {
            isCheck.add(false);
        }

        new loading().execute();
    }

    private void setListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == listView.getCount() - 2) {
                    // new a category
                    if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) {
                        Intent intent = new Intent(mContext, MeFragmentMenuMenuEdit.class);
                        intent.putExtra("position", -1);
                        intent.putExtra("buttonPosition", buttonPosition);
                        startActivity(intent);
                    } else {
                        AuthorityManager.showDialog(mContext, "新建餐品");
                    }
                } else {
                    if (!isEditing) {
                        for (int i = 0; i < isCheck.size(); i++) isCheck.set(i, false);
                        isCheck.set(position, true);
                        adapter.setSelectable(true);
                        adapter.notifyDataSetChanged();
                        editOrNot();
                    }
                }
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == listView.getCount() - 2) {
                    // new a category
                    if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) {
                        Intent intent = new Intent(mContext, MeFragmentMenuMenuEdit.class);
                        intent.putExtra("position", -1);
                        intent.putExtra("buttonPosition", buttonPosition);
                        startActivity(intent);
                    } else {
                        AuthorityManager.showDialog(mContext, "新建餐品");
                    }
                } else if (position == listView.getCount() - 1) {
                    selectAll(((AnimCheckBox)BraecoWaiterUtils.getViewByPosition(listView.getCount() - 1, listView).findViewById(R.id.check)).isChecked());
                } else {
                    if (isEditing) {
                        View convertView = BraecoWaiterUtils.getViewByPosition(position, listView);
                        AnimCheckBox check = (AnimCheckBox) convertView.findViewById(R.id.check);
                        if (check.isChecked()) {
                            check.setChecked(false);
                        } else {
                            check.setChecked(true);
                        }
                        onCheck(position, check.isChecked());
                    } else {
                        int p = BraecoWaiterApplication.b[buttonPosition] + position;
                        if ("combo_sum".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type")) || "combo_static".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type"))) {
                            if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) {
                                Intent intent = new Intent(mContext, MeFragmentMenuSetEdit.class);
                                intent.putExtra("position", p);
                                intent.putExtra("buttonPosition", buttonPosition);
                                startActivity(intent);
                            } else {
                                AuthorityManager.showDialog(mContext, "编辑套餐");
                            }
                        } else {
                            if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) {
                                Intent intent = new Intent(mContext, MeFragmentMenuMenuEdit.class);
                                intent.putExtra("position", p);
                                intent.putExtra("buttonPosition", buttonPosition);
                                startActivity(intent);
                            } else {
                                AuthorityManager.showDialog(mContext, "编辑餐品");
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isEditing) {
            adapter.setSelectable(false);
            adapter.notifyDataSetChanged();
            editOrNot();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BraecoWaiterApplication.JUST_ADD_MENU) {
            finish();
        }
        if (BraecoWaiterApplication.JUST_UPDATE_MENU) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sale:
                if (AuthorityManager.ableTo(Authority.VISIBLE_DISH)) sell();
                else AuthorityManager.showDialog(mContext, "恢复/暂停售卖餐品");
                break;
            case R.id.betop:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) beTop();
                else AuthorityManager.showDialog(mContext, "置顶餐品");
                break;
            case R.id.delete:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) delete();
                else AuthorityManager.showDialog(mContext, "删除餐品");
                break;
            case R.id.change:
                if (AuthorityManager.ableTo(Authority.MANAGER_DISH)) move();
                else AuthorityManager.showDialog(mContext, "移动餐品");
                break;
        }
    }

    // sell or not for one group
    private void sell() {
        final ArrayList<Integer> selects = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                selects.add(i);
            }
        }
        if (selects.isEmpty()) {
            // no one selected
            BraecoWaiterUtils.showToast(mContext, "请选择需要更改售卖状态的餐品");
        } else {
            boolean isAllSaling = true;
            for (int i = selects.size() - 1; i >= 0; i--) {
                if (!(Boolean) BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("able")) {
                    isAllSaling = false;
                }
            }
            if (isAllSaling) {
                // if all the selected menus are saled
                // pause saling
                String content = "确定将以下餐品暂停售卖吗？\n";
                if (selects.size() == 1) {
                    content = "确定将 " + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(0)).get("name")
                            + " 暂停售卖吗？";
                } else {
                    for (int i = 0; i < selects.size(); i++) {
                        content += BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("name") + "\n";
                    }
                }
                new MaterialDialog.Builder(mContext)
                        .title("暂停售卖")
                        .content(content)
                        .positiveText("暂停售卖")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    progressDialog = new MaterialDialog.Builder(mContext)
                                            .title("正在暂停售卖")
                                            .content("请稍候")
                                            .cancelable(false)
                                            .progress(true, 0)
                                            .show();
                                    JSONArray data = new JSONArray();
                                    for (int i = 0; i < selects.size(); i++) {
                                        data.put(BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("id"));
                                    }
                                    new SetDishDisableAsyncTask(mOnSetDishDisableAsyncTaskListener, data.toString())
                                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        })
                        .show();
            } else {
                String content = "确定将以下餐品恢复售卖吗？\n";
                if (selects.size() == 1) {
                    content = "确定将 " + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(0)).get("name")
                            + " 恢复售卖吗？";
                } else {
                    for (int i = 0; i < selects.size(); i++) {
                        content += BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("name") + "\n";
                    }
                }
                new MaterialDialog.Builder(mContext)
                        .title("恢复售卖")
                        .content(content)
                        .positiveText("恢复售卖")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    progressDialog = new MaterialDialog.Builder(mContext)
                                            .title("正在恢复售卖")
                                            .content("请稍候")
                                            .cancelable(false)
                                            .progress(true, 0)
                                            .show();
                                    JSONArray data = new JSONArray();
                                    for (int i = 0; i < selects.size(); i++) {
                                        data.put(BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("id"));
                                    }
                                    new SetDishEnableAsyncTask(mOnSetDishEnableAsyncTaskListener, data.toString())
                                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private String[] items;
    private void move() {
        if (BraecoWaiterApplication.mButton.size() == 1) {
            BraecoWaiterUtils.showToast(mContext, "只有一个品类");
            return;
        }
        final ArrayList<Integer> selects = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                selects.add(i);
            }
        }
        if (selects.isEmpty()) {
            // no one selected
            BraecoWaiterUtils.showToast(mContext, "请选择需要移动的餐品");
        } else {
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < BraecoWaiterApplication.mButton.size(); i++) {
                if (buttonPosition != i) strings.add((String)BraecoWaiterApplication.mButton.get(i).get("button"));
            }
            items = new String[strings.size()];
            for (int i = 0; i < strings.size(); i++) items[i] = strings.get(i);
            new MaterialDialog.Builder(this)
                    .title("移动到")
                    .items(items)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                            String content = "确定将以下餐品移动到 " + items[which] + " 吗？\n";
                            if (selects.size() == 1) {
                                content = "确定将 " + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(0)).get("name")
                                        + " 移动到 " + items[which] + " 吗？\n";
                            } else {
                                for (int i = 0; i < selects.size(); i++) {
                                    content += BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("name") + "\n";
                                }
                            }
                            new MaterialDialog.Builder(mContext)
                                    .title("移动")
                                    .content(content)
                                    .positiveText("移动")
                                    .negativeText("取消")
                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            if (dialogAction == DialogAction.POSITIVE) {
                                                progressDialog = new MaterialDialog.Builder(mContext)
                                                        .title("正在移动")
                                                        .content("请稍候")
                                                        .cancelable(false)
                                                        .progress(true, 0)
                                                        .show();
                                                int id = -1;
                                                for (int i = 0; i < BraecoWaiterApplication.mButton.size(); i++) {
                                                    if (items[which].equals(BraecoWaiterApplication.mButton.get(i).get("button"))) {
                                                        id = (Integer)BraecoWaiterApplication.mButton.get(i).get("id");
                                                        break;
                                                    }
                                                }
                                                JSONObject data = new JSONObject();
                                                for (int i = 0; i < selects.size(); i++) {
                                                    try {
                                                        data.put("" + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("id"), id);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                new SetDishUpdateCategoryAsyncTask(mOnSetDishUpdateCategoryAsyncTaskListener, data.toString())
                                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                            }
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    })
                    .alwaysCallSingleChoiceCallback()
                    .show();

        }
    }

    private void delete() {
        final ArrayList<Integer> selects = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                selects.add(i);
            }
        }
        if (selects.isEmpty()) {
            // no one selected
            BraecoWaiterUtils.showToast(mContext, "请选择需要删除的餐品");
        } else {
            String content = "确定将以下餐品删除吗？\n";
            if (selects.size() == 1) {
                content = "确定将 " + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(0)).get("name")
                        + " 删除吗？";
            } else {
                for (int i = 0; i < selects.size(); i++) {
                    content += BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("name") + "\n";
                }
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
                                for (int i = 0; i < selects.size(); i++) {
                                    data.put(BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("id"));
                                }
                                new SetDishDeleteAsyncTask(mOnSetDishRemoveAsyncTaskListener, data.toString())
                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    })
                    .show();
        }
    }

    private void beTop() {
        final ArrayList<Integer> selects = new ArrayList<>();
        for (int i = 0; i < isCheck.size(); i++) {
            if (isCheck.get(i)) {
                selects.add(i);
            }
        }
        if (selects.isEmpty()) {
            // no one selected
            BraecoWaiterUtils.showToast(mContext, "请选择需要置顶的餐品");
        } else {
            String content = "确定将以下餐品置顶吗？\n";
            if (selects.size() == 1) {
                content = "确定将 " + BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(0)).get("name")
                        + " 置顶吗？";
            } else {
                for (int i = 0; i < selects.size(); i++) {
                    content += BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("name") + "\n";
                }
            }
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
                                for (int i = 0; i < selects.size(); i++) {
                                    data.put(BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("id"));
                                }
                                new SetDishUpdateTopAsyncTask(mOnSetDishUpdateTopAsyncTaskListener, data.toString())
                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onCheck(int position, boolean check) {
        if (isEditing) {
            isCheck.set(position, check);
            ArrayList<Integer> selects = new ArrayList<>();
            for (int i = 0; i < isCheck.size(); i++) {
                if (isCheck.get(i)) {
                    selects.add(i);
                }
            }
            if (selects.isEmpty()) {
                // no one selected
                sale.setText("恢复/暂停售卖");
                change.setText("移动");
                delete.setText("删除");
                beTop.setText("置顶");
            } else {
                boolean isAllPauseSale = true;
                boolean isAllSelling = true;
                for (int i = selects.size() - 1; i >= 0; i--) {
                    if ((Boolean) BraecoWaiterApplication.mSettingMenu.get(BraecoWaiterApplication.b[buttonPosition] + selects.get(i)).get("able")) {
                        isAllPauseSale = false;
                    } else {
                        isAllSelling = false;
                    }
                }
                if (isAllPauseSale) {
                    // if all the selected menus are paused to sale
                    sale.setText("恢复售卖");
                } else if (isAllSelling) {
                    // if all the selected menus are sale
                    sale.setText("暂停售卖");
                } else {
                    // some sale, some not
                    sale.setText("恢复售卖");
                }
            }
        }
    }

    @Override
    public void selectAll(boolean selected) {
        for (int i = 0; i < isCheck.size(); i++) isCheck.set(i, selected);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {
        listView.smoothScrollBy(0, 0);
        listView.setSelection(0);
    }

    @Override
    public void clickTitleEdit() {
        for (int i = 0; i < isCheck.size(); i++) isCheck.set(i, false);
        adapter.setSelectable(!isEditing);
        adapter.notifyDataSetChanged();
        editOrNot();
    }

    private void editOrNot() {
        if (!isEditing) {
            title.setEdit("完成");
            sale.setVisibility(View.VISIBLE);
            change.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            beTop.setVisibility(View.VISIBLE);
            BraecoWaiterUtils.delayBounceInUp(0, sale);
            BraecoWaiterUtils.delayBounceInUp(300, change);
            BraecoWaiterUtils.delayBounceInUp(600, delete);
            BraecoWaiterUtils.delayBounceInUp(900, beTop);
        } else {
            YoYo.with(new MySlideOutDownAnimator()).duration(700).playOn(sale);
            YoYo.with(new MySlideOutDownAnimator()).duration(700).delay(100).playOn(change);
            YoYo.with(new MySlideOutDownAnimator()).duration(700).delay(200).playOn(delete);
            YoYo.with(new MySlideOutDownAnimator()).duration(700).delay(300).playOn(beTop);
            title.setEdit("编辑");
        }
        isEditing = !isEditing;
    }

    private OnSetDishEnableAsyncTaskListener mOnSetDishEnableAsyncTaskListener = new OnSetDishEnableAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("恢复售卖成功，正在刷新");
            BraecoWaiterApplication.JUST_REFRESH_MENU = true;
            finish();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("恢复售卖失败");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetDishDisableAsyncTaskListener mOnSetDishDisableAsyncTaskListener = new OnSetDishDisableAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("暂停售卖成功，正在刷新");
            BraecoWaiterApplication.JUST_REFRESH_MENU = true;
            finish();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("暂停售卖失败");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetDishUpdateCategoryAsyncTaskListener mOnSetDishUpdateCategoryAsyncTaskListener = new OnSetDishUpdateCategoryAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("移动成功，正在刷新");
            BraecoWaiterApplication.JUST_REFRESH_MENU = true;
            finish();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("移动失败");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetDishRemoveAsyncTaskListener mOnSetDishRemoveAsyncTaskListener = new OnSetDishRemoveAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("删除成功，正在刷新");
            BraecoWaiterApplication.JUST_REFRESH_MENU = true;
            finish();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("删除失败");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnSetDishUpdateTopAsyncTaskListener mOnSetDishUpdateTopAsyncTaskListener = new OnSetDishUpdateTopAsyncTaskListener() {
        @Override
        public void success() {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("置顶成功，正在刷新");
            BraecoWaiterApplication.JUST_REFRESH_MENU = true;
            finish();
        }

        @Override
        public void fail(String message) {
            if (progressDialog != null) progressDialog.dismiss();
            BraecoWaiterUtils.showToast("置顶失败");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    public class loading extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            progressDialog = new MaterialDialog.Builder(mContext)
                    .title("加载中")
                    .content("请稍候")
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (progressDialog != null) progressDialog.dismiss();

            listView = (ListView)findViewById(R.id.list_view);
            if (listView != null) listView.setAdapter(adapter);

            setListener();
        }

        protected Boolean doInBackground(final String... args) {

            adapter = new MeFragmentMenuMenuAdapter(buttonPosition, mContext, MeFragmentMenuMenu.this, isCheck);

            return false;
        }
    }

}
