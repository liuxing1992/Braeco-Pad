package com.braeco.braecowaiter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;

import java.util.HashSet;

public class MeFragmentMenuSetEditAttributeCombo extends BraecoAppCompatActivity
        implements
        MeFragmentMenuSetEditAttributeComboAdapter.OnCategoryClickListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;

    private ListView listView;
    private MeFragmentMenuSetEditAttributeComboAdapter adapter;

    private int comboPosition = -1;

    private HashSet<Integer> oldCombos;
    private HashSet<Integer> nowCombos;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_set_edit_attribute_combo);

        mContext = this;

        comboPosition = getIntent().getIntExtra("position", -1);

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        nowCombos = BraecoWaiterUtils.copySet((HashSet<Integer>)BraecoWaiterData.setAttributes.get(comboPosition).get("data1"));
        oldCombos = BraecoWaiterUtils.copySet(nowCombos);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setFocusable(false);
        adapter = new MeFragmentMenuSetEditAttributeComboAdapter(nowCombos, this);
        listView.setAdapter(adapter);

        adapter.setAllCheck();
    }

    @Override
    public void onBackPressed() {
        if (!BraecoWaiterUtils.isSameHashSetForInteger(oldCombos, nowCombos)) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content("您对子项集合的修改尚未保存")
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                materialDialog.dismiss();
                                save();
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                materialDialog.dismiss();
                                finish();
                            }
                            if (dialogAction == DialogAction.NEUTRAL) {
                                return;
                            }
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void save() {
        if (!BraecoWaiterUtils.isSameHashSetForInteger(oldCombos, nowCombos)) {
            BraecoWaiterData.setAttributes.get(comboPosition).put("data1", nowCombos);
            BraecoWaiterData.setLastIsSaved = true;
            BraecoWaiterData.setAttributeIsChanged = true;
        }
        finish();
    }


    @Override
    public void selectMeal() {
        adapter.setAllCheck();
    }

    @Override
    public void selectCategory() {
        adapter.setAllCheck();
    }

    @Override
    public void selectAll(boolean selected) {
        adapter.selectAll(selected);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void clickTitleBack() {
        onBackPressed();
    }

    @Override
    public void doubleClickTitle() {
        listView.smoothScrollBy(0, 0);
        listView.setSelection(0);
    }

    @Override
    public void clickTitleEdit() {
        save();
    }
}
