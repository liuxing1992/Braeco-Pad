package com.braeco.braecowaiter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;

import java.util.HashSet;

public class MeFragmentSettingsPrintSettingsMeal extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        MeFragmentSettingsPrintSettingsMealAdapter.OnCategoryClickListener,
        MeFragmentSettingsPrintSettingsMealSubAdapter.OnMenuClickListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;

    private ListView listView;
    private MeFragmentSettingsPrintSettingsMealAdapter adapter;

    private HashSet<Integer> oldBan;
    private HashSet<Integer> oldBanCategory;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_print_settings_meal);

        mContext = this;

        oldBan = (HashSet<Integer>) BraecoWaiterApplication.modifyingPrinter.getBan().clone();
        oldBanCategory = (HashSet<Integer>) BraecoWaiterApplication.modifyingPrinter.getBanCategory().clone();

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        listView = (ListView)findViewById(R.id.list_view);
        listView.setFocusable(false);
        adapter = new MeFragmentSettingsPrintSettingsMealAdapter(this);
        listView.setAdapter(adapter);

        changeAllChecker();
    }

    private void selectOrUnSelectAll(boolean selected) {
        adapter.selectAll(selected);
        BraecoWaiterApplication.modifyingPrinter.getBan().clear();
        BraecoWaiterApplication.modifyingPrinter.getBanCategory().clear();
        if (!selected) {
            for (int i = 0; i < BraecoWaiterApplication.mButton.size(); i++) {
                BraecoWaiterApplication.modifyingPrinter.getBanCategory().add((Integer) BraecoWaiterApplication.mButton.get(i).get("id"));
            }
            for (int i = 0; i < BraecoWaiterApplication.mSettingMenu.size(); i++) {
                BraecoWaiterApplication.modifyingPrinter.getBan().add((Integer) BraecoWaiterApplication.mSettingMenu.get(i).get("id"));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void changeAllChecker() {
        adapter.setAllCheck();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                quit();
                break;
            case R.id.edit:
                // save
                finish();
                break;
        }
    }

    @Override
    public void onClick(int id, boolean check) {
        changeAllChecker();
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    private void quit() {
        if (!BraecoWaiterUtils.isSameHashSetForInteger(BraecoWaiterApplication.modifyingPrinter.getBan(), oldBan)
                || !BraecoWaiterUtils.isSameHashSetForInteger(BraecoWaiterApplication.modifyingPrinter.getBanCategory(), oldBanCategory)) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content("您对被允许打印餐品的修改尚未保存")
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                finish();
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                BraecoWaiterApplication.modifyingPrinter.setBan(oldBan);
                                BraecoWaiterApplication.modifyingPrinter.setBanCategory(oldBanCategory);
                                finish();
                            }
                            if (dialogAction == DialogAction.NEUTRAL) {
                                return;
                            }
                        }
                    })
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public void clickTitleBack() {
        quit();
    }

    @Override
    public void doubleClickTitle() {
        listView.smoothScrollBy(0, 0);
        listView.setSelection(0);
    }

    @Override
    public void clickTitleEdit() {
        finish();
    }

    @Override
    public void selectMeal() {
        changeAllChecker();
    }

    @Override
    public void selectCategory() {
        changeAllChecker();
    }

    @Override
    public void selectAll(boolean selected) {
        selectOrUnSelectAll(selected);
    }
}
