package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeFragmentMenuMenuEditAttribute extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        MeFragmentMenuMenuEditAttributeAdapter.OnClickViewListener {

    private LinearLayout back;

    private TextView finish;

    private ListView listView;
    private MeFragmentMenuMenuEditAttributeAdapter adapter;

    private Context mContext;

    private ArrayList<Map<String, Object>> oldAttributes;
    private int menuPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_menu_edit_attribute);

        mContext = this;
        oldAttributes = BraecoWaiterUtils.copyList(BraecoWaiterData.attributes);
        menuPosition = getIntent().getIntExtra("position", -1);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        finish = (TextView)findViewById(R.id.edit);
        finish.setOnClickListener(this);

        listView = (ListView)findViewById(R.id.list_view);
        adapter = new MeFragmentMenuMenuEditAttributeAdapter(this);
        listView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.edit:
                if (save(true)) super.onBackPressed();
                break;
        }
    }

    @Override
    public void OnClickView(int position, Integer action) {
        if (BraecoWaiterData.DELETE.equals(action)) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                deleteAttributeGroup(position);
            } else if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                deleteAttribute(position);
            }
        } else if (BraecoWaiterData.ADD.equals(action)) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP_ADD.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                addAttributeGroup();
            } else if (BraecoWaiterData.ATTRIBUTE_ADD.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                addAttribute(position);
            }
        } else if (BraecoWaiterData.CHANGE_NAME.equals(action)) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                changeAttributeGroupName(position);
            } else if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                changeAttributeName(position);
            }
        } else if (BraecoWaiterData.CHANGE_PRICE.equals(action)) {
            if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(position).get("type"))) {
                changeAttributePrice(position);
            }
        }
    }

    private MaterialDialog inputDialog;
    private void addAttributeGroup() {
        int counter = 0;
        for (int i = 0; i < BraecoWaiterData.attributes.size(); i++) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                counter++;
            }
        }
        if (counter >= 40) {
            BraecoWaiterUtils.showToast(mContext, "餐品最多属性组为40");
            return;
        }
        String title = "添加属性";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_GROUP_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_GROUP_NAME;
        final String hint = "属性名";
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
                                map.put("type", BraecoWaiterData.ATTRIBUTE_GROUP);
                                BraecoWaiterData.attributes.add(BraecoWaiterData.attributes.size() - 1, map);
                                map = new HashMap<>();
                                map.put("data1", "");
                                map.put("data2", "0");
                                map.put("type", BraecoWaiterData.ATTRIBUTE);
                                BraecoWaiterData.attributes.add(BraecoWaiterData.attributes.size() - 1, map);
                                map = new HashMap<>();
                                map.put("type", BraecoWaiterData.ATTRIBUTE_ADD);
                                BraecoWaiterData.attributes.add(BraecoWaiterData.attributes.size() - 1, map);
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

    private double newPrice = -1;
    private void addAttribute(final int position) {
        int counter = 0;
        for (int i = position - 1; i >= 0; i--) {
            if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                counter++;
            }
        }
        if (counter >= 20) {
            BraecoWaiterUtils.showToast(mContext, "属性组的选项数目最大为20");
            return;
        }
        String title = "添加选项";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_NAME;
        final String hint = "选项名";
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
                            final String newName = materialDialog.getInputEditText().getText().toString();
                            if (existSameName(newName)) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(materialDialog.getInputEditText());
                                BraecoWaiterUtils.showToast(mContext, "存在同名选项");
                            } else {
                                new MaterialDialog.Builder(mContext)
                                        .title("添加价差")
                                        .negativeText("取消")
                                        .positiveText("确认")
                                        .content("¥ " + String.format("%.2f", BraecoWaiterFinal.MIN_ATTRIBUTE_PRICE) + "~" + String.format("%.2f", BraecoWaiterFinal.MAX_ATTRIBUTE_PRICE))
                                        .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                                        .input("价差", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                if ("".equals(String.valueOf(input))) {
                                                    newPrice = -1;
                                                } else {
                                                    try {
                                                        newPrice = Double.parseDouble(String.valueOf(input));
                                                    } catch (NumberFormatException n) {
                                                        newPrice = -1;
                                                        n.printStackTrace();
                                                    }
                                                }
                                                if (!(BraecoWaiterFinal.MIN_ATTRIBUTE_PRICE <= newPrice && newPrice <= BraecoWaiterFinal.MAX_ATTRIBUTE_PRICE)) {
                                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                                } else {
                                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                }
                                            }
                                        })
                                        .alwaysCallInputCallback()
                                        .onAny(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                if (dialogAction == DialogAction.POSITIVE) {
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("data1", newName);
                                                    map.put("data2", String.format("%.2f", newPrice));
                                                    map.put("type", BraecoWaiterData.ATTRIBUTE);
                                                    BraecoWaiterData.attributes.add(position, map);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        })
                                        .show();
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

    private void changeAttributeGroupName(final int position) {
        String title = "修改属性";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_GROUP_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_GROUP_NAME;
        final String hint = "属性名";
        final String fill = (String)BraecoWaiterData.attributes.get(position).get("data");
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
                                BraecoWaiterData.attributes.get(position).put("data", newName);
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

    private void changeAttributeName(final int position) {
        String title = "修改选项";
        final int min = BraecoWaiterFinal.MIN_ATTRIBUTE_NAME;
        final int max = BraecoWaiterFinal.MAX_ATTRIBUTE_NAME;
        final String hint = "选项名";
        final String fill = (String)BraecoWaiterData.attributes.get(position).get("data1");
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
                                BraecoWaiterUtils.showToast(mContext, "存在同名选项");
                            } else {
                                BraecoWaiterData.attributes.get(position).put("data1", newName);
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

    private void changeAttributePrice(final int position) {
        newPrice = -1;
        String hint = (String)BraecoWaiterData.attributes.get(position).get("data2");
        new MaterialDialog.Builder(mContext)
                .title("修改价差")
                .positiveText("确认")
                .negativeText("取消")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .alwaysCallInputCallback()
                .input(hint, "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!BraecoWaiterUtils.isInvalidMenuData(String.valueOf(input))) {
                            if ("".equals(String.valueOf(input))) {
                                newPrice = -1;
                            } else {
                                try {
                                    newPrice = Double.parseDouble(String.valueOf(input));
                                } catch (NumberFormatException n) {
                                    newPrice = -1;
                                    n.printStackTrace();
                                }
                            }
                            if (!(BraecoWaiterFinal.MIN_ATTRIBUTE_PRICE <= newPrice && newPrice <= BraecoWaiterFinal.MAX_ATTRIBUTE_PRICE)) {
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
                            BraecoWaiterData.attributes.get(position).put("data2", String.format("%.2f", newPrice));
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private void deleteAttributeGroup(final int position) {
        String name = (String)BraecoWaiterData.attributes.get(position).get("data");
        new MaterialDialog.Builder(mContext)
                .title("确认删除")
                .content("确定删除属性组" + name + "吗？该属性组之下所有选项和价差都会被删除")
                .positiveText("确定")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            int end = -1;
                            for (int i = position; i < BraecoWaiterData.attributes.size(); i++) {
                                if (BraecoWaiterData.ATTRIBUTE_ADD.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                                    end = i;
                                    break;
                                }
                            }
                            ArrayList<Map<String, Object>> newAttributes = new ArrayList<Map<String, Object>>();
                            for (int i = 0; i < BraecoWaiterData.attributes.size(); i++) {
                                if (i < position || i > end) {
                                    newAttributes.add(BraecoWaiterData.attributes.get(i));
                                }
                            }
                            BraecoWaiterData.attributes = newAttributes;
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private void deleteAttribute(final int position) {
        String name = (String)BraecoWaiterData.attributes.get(position).get("data1");
        new MaterialDialog.Builder(mContext)
                .title("确认删除")
                .content("确定删除选项" + name + "吗？")
                .positiveText("确定")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(position - 1).get("type"))
                                    && BraecoWaiterData.ATTRIBUTE_ADD.equals(BraecoWaiterData.attributes.get(position + 1).get("type"))) {
                                BraecoWaiterData.attributes.remove(position - 1);
                                BraecoWaiterData.attributes.remove(position - 1);
                                BraecoWaiterData.attributes.remove(position - 1);
                            } else {
                                BraecoWaiterData.attributes.remove(position);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .show();
    }

    private boolean existSameName(String newName) {
        for (int i = BraecoWaiterData.attributes.size() - 1; i >= 0; i--) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                if (newName.equals(BraecoWaiterData.attributes.get(i).get("data"))) {
                    return true;
                }
            }
            if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                if (newName.equals(BraecoWaiterData.attributes.get(i).get("data1"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (!BraecoWaiterUtils.isSame(oldAttributes, BraecoWaiterData.attributes)) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content("您对属性和差价的修改尚未保存")
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                if (save(true)) MeFragmentMenuMenuEditAttribute.super.onBackPressed();
                                else return;
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                BraecoWaiterData.attributes = BraecoWaiterUtils.copyList(oldAttributes);
                                MeFragmentMenuMenuEditAttribute.super.onBackPressed();
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
            BraecoWaiterUtils.showToast(mContext, "存在未填写的选项或价差");
            return false;
        }
        if (!BraecoWaiterUtils.isSame(oldAttributes, BraecoWaiterData.attributes)) BraecoWaiterData.attributeIsChanged = true;
        if (!finish) oldAttributes = BraecoWaiterUtils.copyList(BraecoWaiterData.attributes);
        BraecoWaiterData.lastIsSaved = true;
        return true;
        // we need not do anything here
        // when we update or create a new menu to server
        // we can use the attributes directly
    }

    private boolean judgeNoEmpty() {
        boolean ans = true;
        int position = -1;
        for (int i = 0; i < BraecoWaiterData.attributes.size(); i++) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                if ("".equals(BraecoWaiterData.attributes.get(i).get("data"))) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
            } else if (BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                if ("".equals(BraecoWaiterData.attributes.get(i).get("data1"))) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data1);
//                    YoYo.with(Techniques.Shake)
//                            .duration(700)
//                            .playOn(tv);
                    if (position == -1) position = i;
                }
                if ("".equals(BraecoWaiterData.attributes.get(i).get("data2"))) {
                    ans = false;
                    View view = getViewByPosition(i, listView);
                    TextView tv = (TextView)view.findViewById(R.id.data2);
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
}



