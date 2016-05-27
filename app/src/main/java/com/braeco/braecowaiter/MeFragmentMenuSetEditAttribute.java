package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MeFragmentMenuSetEditAttribute extends BraecoAppCompatActivity
        implements
        MeFragmentMenuSetEditAttributeAdapter.OnClickViewListener,
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;
    private ListView listView;
    private MeFragmentMenuSetEditAttributeAdapter adapter;

    private Context mContext;

    private ArrayList<Map<String, Object>> oldAttributes;
    private int menuPosition = -1;
    // -1 for not set, 0 for combo_sum, 1 for combo_static
    private int setType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_set_edit_attribute);

        mContext = this;
        oldAttributes = BraecoWaiterUtils.copyList(BraecoWaiterData.setAttributes);
        menuPosition = getIntent().getIntExtra("position", -1);
        setType = getIntent().getIntExtra("setType", -1);

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        listView = (ListView)findViewById(R.id.list_view);
        adapter = new MeFragmentMenuSetEditAttributeAdapter(this);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnClickView(int position, Integer action) {
        if (BraecoWaiterData.DELETE_SET.equals(action)) {
            deleteSetAttribute(position);
        } else if (BraecoWaiterData.ADD_SET.equals(action)) {
            addSetAttribute();
        } else if (BraecoWaiterData.CHANGE_SET_NAME.equals(action)) {
            changeSetAttributeName(position);
        } else if (BraecoWaiterData.CHANGE_SET.equals(action)) {
            Intent intent = new Intent(mContext, MeFragmentMenuSetEditAttributeCombo.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (BraecoWaiterData.CHANGE_SET_SIZE.equals(action)) {
            changeSetAttributeSize(position);
        } else if (BraecoWaiterData.CHANGE_SET_DISCOUNT.equals(action)) {
            changeSetAttributeDiscount(position);
        }
    }

    private MaterialDialog inputDialog;
    private void addSetAttribute() {
        String title = "添加子项";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_GROUP_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_GROUP_NAME;
        final String hint = "子项名";
        final String fill = "";
        inputDialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .negativeText("取消")
                .positiveText("确认")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(hint, fill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(String.valueOf(input)));
                        String pre = BraecoWaiterUtils.invalidString(input);
                        if (!"".equals(pre)) BraecoWaiterUtils.showToast(mContext, pre);
                        dialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        "",
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            String newName = materialDialog.getInputEditText().getText().toString();
                            if (existSameName(newName)) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(materialDialog.getInputEditText());
                                BraecoWaiterUtils.showToast(mContext, "存在同名属性");
                            } else {
                                Map<String, Object> map = new HashMap<>();
                                map.put("data", materialDialog.getInputEditText().getText().toString());
                                map.put("type", BraecoWaiterData.SET_ATTRIBUTE_NAME);
                                BraecoWaiterData.setAttributes.add(BraecoWaiterData.setAttributes.size() - 1, map);
                                map = new HashMap<>();
                                map.put("data1", new HashSet<Integer>());
                                map.put("data2", -2);
                                if (setType == -1) {
                                    map.put("data3", -1);
                                } else if (setType == 1) {
                                    map.put("data3", -2);  // -2 in data3 means combo_static
                                } else if (setType == 0) {
                                    map.put("data3", -1);
                                }

                                map.put("type", BraecoWaiterData.SET_ATTRIBUTE_BODY);
                                BraecoWaiterData.setAttributes.add(BraecoWaiterData.setAttributes.size() - 1, map);
                                adapter.notifyDataSetChanged();
                                materialDialog.dismiss();
                            }
                        }
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(fill));
                        String pre = BraecoWaiterUtils.invalidString(fill);
                        inputDialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        pre,
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .alwaysCallInputCallback()
                .show();
        int b = inputDialog.getInputEditText().getPaddingBottom();
        int t = inputDialog.getInputEditText().getPaddingTop();
        inputDialog.getInputEditText().setPadding(20, t, 20, b);
    }

    private void changeSetAttributeName(final int position) {
        String title = "修改子项名";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_GROUP_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_GROUP_NAME;
        final String hint = "子项名";
        final String fill = (String)BraecoWaiterData.setAttributes.get(position).get("data");
        inputDialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .negativeText("取消")
                .positiveText("确认")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(hint, fill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(String.valueOf(input)));
                        String pre = BraecoWaiterUtils.invalidString(input);
                        if (!"".equals(pre)) BraecoWaiterUtils.showToast(mContext, pre);
                        dialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        "",
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            String newName = materialDialog.getInputEditText().getText().toString();
                            if (existSameName(newName)) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(materialDialog.getInputEditText());
                                BraecoWaiterUtils.showToast(mContext, "存在同名属性");
                            } else {
                                BraecoWaiterData.setAttributes.get(position).put("data", newName);
                                adapter.notifyDataSetChanged();
                                materialDialog.dismiss();
                            }
                        }
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(fill));
                        String pre = BraecoWaiterUtils.invalidString(fill);
                        inputDialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        pre,
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .alwaysCallInputCallback()
                .show();
        int b = inputDialog.getInputEditText().getPaddingBottom();
        int t = inputDialog.getInputEditText().getPaddingTop();
        inputDialog.getInputEditText().setPadding(20, t, 20, b);
    }

    private int newSize = -1;
    private void changeSetAttributeSize(final int position) {
        newSize = -1;
        String hint = "选取份数";
        new MaterialDialog.Builder(mContext)
                .title("修改选取份数")
                .content("请填写每份套餐将从该子项选取的份数（单位：份）")
                .positiveText("确认")
                .negativeText("取消")
                .neutralText("可任意添加或不添加")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .alwaysCallInputCallback()
                .input(hint, "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!BraecoWaiterUtils.isInvalidMenuData(String.valueOf(input))) {
                            if ("".equals(String.valueOf(input))) {
                                newSize = -1;
                            } else {
                                try {
                                    newSize = Integer.parseInt(String.valueOf(input));
                                } catch (NumberFormatException n) {
                                    newSize = -1;
                                    n.printStackTrace();
                                }
                            }
                            if (!(BraecoWaiterFinal.MIN_SET_ATTRIBUTE_SIZE <= newSize && newSize <= BraecoWaiterFinal.MAX_SET_ATTRIBUTE_SIZE)) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            } else {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            }
                        } else {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            BraecoWaiterData.setAttributes.get(position).put("data2", newSize);
                            adapter.notifyDataSetChanged();
                        } else if (dialogAction == DialogAction.NEUTRAL) {
                            BraecoWaiterData.setAttributes.get(position).put("data2", 0);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private int newDiscount = -1;
    private void changeSetAttributeDiscount(final int position) {
        newDiscount = -1;
        String hint = "优惠折扣";
        new MaterialDialog.Builder(mContext)
                .title("修改优惠折扣")
                .content("请填写优惠折扣，仅在总价为“子项加权”时生效（单位：%，如填写90则代表9折）")
                .positiveText("确认")
                .negativeText("取消")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .alwaysCallInputCallback()
                .input(hint, "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!BraecoWaiterUtils.isInvalidMenuData(String.valueOf(input))) {
                            if ("".equals(String.valueOf(input))) {
                                newDiscount = -1;
                            } else {
                                try {
                                    newDiscount = Integer.parseInt(String.valueOf(input));
                                } catch (NumberFormatException n) {
                                    newDiscount = -1;
                                    n.printStackTrace();
                                }
                            }
                            if (!(BraecoWaiterFinal.MIN_SET_ATTRIBUTE_DISCOUNT <= newDiscount && newDiscount <= BraecoWaiterFinal.MAX_SET_ATTRIBUTE_DISCOUNT)) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            } else {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            }
                        } else {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            BraecoWaiterData.setAttributes.get(position).put("data3", newDiscount);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private void deleteSetAttribute(final int position) {
        String name = (String)BraecoWaiterData.setAttributes.get(position).get("data");
        new MaterialDialog.Builder(mContext)
                .title("确认删除")
                .content("确定删除子项" + name + "吗？")
                .positiveText("确定")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            ArrayList<Map<String, Object>> newAttributes = new ArrayList<Map<String, Object>>();
                            for (int i = 0; i < BraecoWaiterData.setAttributes.size(); i++) {
                                if (i < position || i > position + 1) {
                                    newAttributes.add(BraecoWaiterData.setAttributes.get(i));
                                }
                            }
                            BraecoWaiterData.setAttributes = newAttributes;
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private boolean existSameName(String newName) {
        for (int i = BraecoWaiterData.setAttributes.size() - 1; i >= 0; i--) {
            if (BraecoWaiterData.SET_ATTRIBUTE_NAME.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                if (newName.equals(BraecoWaiterData.setAttributes.get(i).get("data"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (!BraecoWaiterUtils.isSame(oldAttributes, BraecoWaiterData.setAttributes)) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content("您对子项的修改尚未保存")
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                if (save(true)) MeFragmentMenuSetEditAttribute.super.onBackPressed();
                                else return;
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                BraecoWaiterData.setAttributes = BraecoWaiterUtils.copyList(oldAttributes);
                                MeFragmentMenuSetEditAttribute.super.onBackPressed();
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

    private boolean save(boolean finish) {
        if (!judgeNoEmpty()) {
            BraecoWaiterUtils.showToast(mContext, "存在未填写完整的子项");
            return false;
        }
        if (!BraecoWaiterUtils.isSame(oldAttributes, BraecoWaiterData.setAttributes)) BraecoWaiterData.setAttributeIsChanged = true;
        if (!finish) oldAttributes = BraecoWaiterUtils.copyList(BraecoWaiterData.setAttributes);
        BraecoWaiterData.setLastIsSaved = true;
        return true;
        // we need not do anything here
        // when we update or create a new menu to server
        // we can use the attributes directly
    }

    private boolean judgeNoEmpty() {
        boolean ans = true;
        int position = -1;
        for (int i = 0; i < BraecoWaiterData.setAttributes.size(); i++) {
            if (BraecoWaiterData.SET_ATTRIBUTE_NAME.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                if ("".equals(BraecoWaiterData.setAttributes.get(i).get("data"))) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
            } else if (BraecoWaiterData.SET_ATTRIBUTE_BODY.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                if (BraecoWaiterData.setAttributes.get(i).get("data1") == null) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data1);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
                if (BraecoWaiterData.setAttributes.get(i).get("data2").equals(-2)) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data2);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
                if (BraecoWaiterData.setAttributes.get(i).get("data3").equals(-1)) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data3);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
            }
        }
        if (position != -1) listView.setSelectionFromTop(position, 0);
        return ans;
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
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
        if (save(true)) super.onBackPressed();
    }
}
