package com.braeco.braecowaiter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

import java.util.HashSet;

public class MeFragmentSettingsPrintSettingsTable extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;
    private ScrollView scrollView;
    private AnimCheckBox allCheck;

    private ExpandedGridView gridView;
    private MeFragmentSettingsPrintSettingsTableAdapter adapter;

    private HashSet<String> oldBanTable;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_print_settings_table);

        mContext = this;

        oldBanTable = (HashSet<String>) BraecoWaiterApplication.modifyingPrinter.getBanTable().clone();

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        allCheck = (AnimCheckBox)findViewById(R.id.check);
        allCheck.setOnClickListener(this);

        gridView = (ExpandedGridView) findViewById(R.id.grid_view);
        gridView.setFocusable(false);
        adapter = new MeFragmentSettingsPrintSettingsTableAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
                if (check == null) return;  // null view
                check.setChecked(!check.isChecked());
            }
        });
        gridView.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < gridView.getCount(); i++) {
                    View view = getViewByPosition(i, gridView);
                    AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
                    if (check == null) continue;  // null view
                    check.setChecked(!BraecoWaiterApplication.modifyingPrinter.getBanTable().contains(BraecoWaiterApplication.tables.get(i).getId()));
                }
            }
        });

        changeAllChecker();
    }

    private void selectOrUnSelectAll() {
        allCheck.setChecked(!allCheck.isChecked());
        for (int i = 0; i < gridView.getCount(); i++) {
            View view = getViewByPosition(i, gridView);
            AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
            if (check == null) continue;  // null view
            check.setChecked(allCheck.isChecked());
        }
        BraecoWaiterApplication.modifyingPrinter.getBanTable().clear();
        if (!allCheck.isChecked()) {
            for (int i = 0; i < BraecoWaiterApplication.tables.size(); i++) {
                BraecoWaiterApplication.modifyingPrinter.getBanTable().add(BraecoWaiterApplication.tables.get(i).getId());
            }
        }
    }

    private void changeAllChecker() {
        if (BraecoWaiterApplication.modifyingPrinter.getBanTable().size() == 0) {
            allCheck.setChecked(true);
        } else {
            allCheck.setChecked(false);
        }
    }

    private void writeSelectedTables() {
        BraecoWaiterApplication.modifyingPrinter.getBanTable().clear();
        for (int i = 0; i < gridView.getCount(); i++) {
            View view = getViewByPosition(i, gridView);
            AnimCheckBox check = (AnimCheckBox)view.findViewById(R.id.check);
            if (check == null) continue;  // null view
            if (!check.isChecked()) {
                BraecoWaiterApplication.modifyingPrinter.getBanTable().add(BraecoWaiterApplication.tables.get(i).getId());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all:
            case R.id.check:
                selectOrUnSelectAll();
                break;
        }
    }

    private View getViewByPosition(int pos, GridView gridView) {
        final int firstListItemPosition = gridView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + gridView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return gridView.getAdapter().getView(pos, null, gridView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return gridView.getChildAt(childIndex);
        }
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    private void quit() {
        writeSelectedTables();
        if (!BraecoWaiterUtils.isSameHashSetForString(BraecoWaiterApplication.modifyingPrinter.getBanTable(), oldBanTable)) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content("您对被允许打印桌位的修改尚未保存")
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
                                BraecoWaiterApplication.modifyingPrinter.setBanTable(oldBanTable);
                                finish();
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
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void clickTitleEdit() {
        writeSelectedTables();
        finish();
    }
}
