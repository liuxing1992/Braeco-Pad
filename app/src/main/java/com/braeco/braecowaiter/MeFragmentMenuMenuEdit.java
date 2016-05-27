package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MeFragmentMenuMenuEdit extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private static final int CHANGE_PHOTO = 1;
    private static final int CHANGE_DETAIL = 2;
    private static final String[] SALES_NAME = new String[]{"无", "折扣优惠", "立减优惠", "第二杯半价", "限量供应", "仅在套餐中出现"};
    private static final String[] SALES_TITLE = new String[]{"", "折扣力度", "立减金额", "", "限量数目"};
    private static final String[] SALES_CONTENT = new String[]{"", "折扣力度在10~99之间", "立减金额在1~50之间", "", "限量数目在1~99之间"};
    private static final String[] ALERT_STRING = new String[]{"是否保存您新建的餐品？", "是否保存您对该餐品的修改？"};
    private static final String[] SALES_INFO = new String[]{"none", "discount", "sale", "half", "limit", "combo_only"};
    private static String SIZE = null;

    private LinearLayout cName;
    private LinearLayout price;
    private LinearLayout eName;
    private LinearLayout tags;
    private LinearLayout attributes;
    private LinearLayout sales;
    private LinearLayout more;

    private TextView title;
    private TextView finish;
    private TextView emptyTip;
    private TextView cNameTV;
    private TextView priceTV;
    private TextView eNameTV;
    private TextView tagsTV;
    private TextView attributesTV;
    private TextView salesTV;
    private TextView moreTV;

    private KenBurnsView image;

    private int buttonPosition = -1;
    private int menuPosition = -1;
    private Context mContext;

    private boolean changed = false;
    private boolean pictureChange = false;

    private boolean noPicture = true;
    private String imageString = "";
    private String cNameString = "";
    private Double priceDouble = -1d;
    private String eNameString = "";
    private String tagsString = "";
    private int saleChoice = 0;
    private int saleNumber = -1;
    private String moreString = "";

    private String token = "";
    private String key = "";

    private String oldCNameString = "";
    private Double oldPriceDouble = -1d;
    private String oldENameString = "";
    private String oldTagsString = "";
    private int oldSaleChoice = 0;
    private int oldSaleNumber = -1;
    private String oldMoreString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_menu_edit);

        mContext = this;

        menuPosition = getIntent().getIntExtra("position", -1);
        buttonPosition = getIntent().getIntExtra("buttonPosition", -1);

        if (SIZE == null) {
            SIZE = "?imageView2/1/w/" + getResources().getDisplayMetrics().widthPixels
                    + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);
        }

        BraecoWaiterData.attributes = new ArrayList<>();
        if (menuPosition != -1) {
            Map<String, Object> menu = BraecoWaiterApplication.mSettingMenu.get(menuPosition);
            int attributeGroupNum = (Integer) menu.get("num_shuxing");
            String[] attributeGroupName = (String[])menu.get("shuxingName");
            ArrayList<Double>[] attributePrice = (ArrayList<Double>[])menu.get("addshuxing");
            ArrayList<String>[] attributeItemName = (ArrayList<String>[])menu.get("shuxing");
            int[] attributeGroupSize = (int[])menu.get("res");
            for (int i = 0; i < attributeGroupNum; i++) {
                Map<String, Object> group = new HashMap<>();
                group.put("data", attributeGroupName[i]);
                group.put("type", BraecoWaiterData.ATTRIBUTE_GROUP);
                BraecoWaiterData.attributes.add(group);
                for (int j = 0; j < attributeGroupSize[i]; j++) {
                    Map<String, Object> attribute = new HashMap<>();
                    attribute.put("data1", attributeItemName[i].get(j));
                    attribute.put("data2", String.format("%.2f", attributePrice[i].get(j)));
                    attribute.put("type", BraecoWaiterData.ATTRIBUTE);
                    BraecoWaiterData.attributes.add(attribute);
                }
                Map<String, Object> addAttribute = new HashMap<>();
                addAttribute.put("type", BraecoWaiterData.ATTRIBUTE_ADD);
                BraecoWaiterData.attributes.add(addAttribute);
            }
            Map<String, Object> addGroup = new HashMap<>();
            addGroup.put("type", BraecoWaiterData.ATTRIBUTE_GROUP_ADD);
            BraecoWaiterData.attributes.add(addGroup);
        }
        BraecoWaiterData.lastIsSaved = false;
        BraecoWaiterData.attributeIsChanged = false;

        cName = (LinearLayout)findViewById(R.id.c_name);
        price = (LinearLayout)findViewById(R.id.price);
        eName = (LinearLayout)findViewById(R.id.e_name);
        tags = (LinearLayout)findViewById(R.id.tags);
        attributes = (LinearLayout)findViewById(R.id.attributes);
        sales = (LinearLayout)findViewById(R.id.sale);
        more = (LinearLayout)findViewById(R.id.more);

        image = (KenBurnsView)findViewById(R.id.image);
        title = (TextView)findViewById(R.id.title);
        finish = (TextView)findViewById(R.id.edit);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        cNameTV = (TextView)findViewById(R.id.c_name_text);
        priceTV = (TextView)findViewById(R.id.price_text);
        eNameTV = (TextView)findViewById(R.id.e_name_text);
        tagsTV = (TextView)findViewById(R.id.tags_text);
        attributesTV = (TextView)findViewById(R.id.attributes_text);
        salesTV = (TextView)findViewById(R.id.sale_text);
        moreTV = (TextView)findViewById(R.id.more_text);

        finish.setOnClickListener(this);
        cName.setOnClickListener(this);
        price.setOnClickListener(this);
        eName.setOnClickListener(this);
        tags.setOnClickListener(this);
        attributes.setOnClickListener(this);
        sales.setOnClickListener(this);
        more.setOnClickListener(this);
        emptyTip.setOnClickListener(this);
        image.setOnClickListener(this);

        finish.setText("完成");
        if (menuPosition != -1) {
            title.setText("餐品修改");
            cNameString = (String) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("name");
            cNameTV.setText(cNameString);
            priceDouble = (Double) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("price");
            priceTV.setText("¥ " + String.format("%.2f", priceDouble));
            eNameString = (String) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("en_name");
            eNameTV.setText(eNameString);
            if (BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail") == null
                    || "null".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail"))) {
                moreString = "";
            } else {
                moreString = (String) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail");
            }
            moreTV.setText(moreString);
            if (BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag") == null
                    || "null".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag"))) {
                tagsString = "";
            } else {
                tagsString = (String) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag");
            }
            tagsTV.setText(tagsString);
            if (BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("img") == null
                    || "null".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("img"))) {
                image.setVisibility(View.INVISIBLE);
                emptyTip.setVisibility(View.VISIBLE);
                noPicture = true;
            } else {
                Picasso.with(mContext)
                        .load(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("img") + SIZE)
                        .into(image);
                image.setVisibility(View.VISIBLE);
                emptyTip.setVisibility(View.INVISIBLE);
                noPicture = false;
            }
            if ("sale".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                saleChoice = 2;
                saleNumber = (int) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc");
            }
            if ("discount".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                saleChoice = 1;
                saleNumber = (int) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc");
            }
            if ("half".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                saleChoice = 3;
                saleNumber = (int) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc");
            }
            if ("limit".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                saleChoice = 4;
                saleNumber = (int) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc");
            }
            if ("combo_only".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                saleChoice = 5;
                saleNumber = (int) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc");
            }
            writeSaleTV();

            oldCNameString = cNameString;
            oldPriceDouble = priceDouble;
            oldENameString = eNameString;
            oldTagsString = tagsString;
            oldSaleChoice = saleChoice;
            oldSaleNumber = saleNumber;
            oldMoreString = moreString;
        } else {
            title.setText(("新建餐品"));
            cNameTV.setText("");
            priceTV.setText("");
            eNameTV.setText("");
            tagsString = "";
            tagsTV.setText(tagsString);
            moreString = "";
            moreTV.setText(moreString);
            image.setVisibility(View.INVISIBLE);
            emptyTip.setVisibility(View.VISIBLE);

            writeSaleTV();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        writeAttribute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.image:
            case R.id.empty_tip:
                changePicture();
                break;
            case R.id.c_name:
                changeCName();
                break;
            case R.id.price:
                changePrice();
                break;
            case R.id.e_name:
                changeEName();
                break;
            case R.id.tags:
                changeTag();
                break;
            case R.id.attributes:
                changeAttributes();
                break;
            case R.id.sale:
                changeSale();
                break;
            case R.id.more:
                changeMore();
                break;
            case R.id.edit:
                update();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CHANGE_PHOTO) {
            if (data != null) {
                imageString = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS).get(0);
                File imgFile = new File(imageString);
                if(imgFile.exists()) {
                    noPicture = false;
                    pictureChange = true;
                    changed = true;
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    int width = mContext.getResources().getDisplayMetrics().widthPixels;
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, width, myBitmap.getHeight() * width / myBitmap.getWidth(), true);
                    image.setImageBitmap(scaledBitmap);
                    image.setVisibility(View.VISIBLE);
                    emptyTip.setVisibility(View.INVISIBLE);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == CHANGE_DETAIL) {
            boolean changed = data.getBooleanExtra("changed", false);
            String backString = data.getStringExtra("new");
            if ("".equals(backString)) return;
            moreString = backString;
            moreTV.setText(moreString);
        }
    }

    private void changePicture() {
        if (noPicture) {
            PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
            intent.setPhotoCount(1);
            intent.setShowCamera(true);
            startActivityForResult(intent, CHANGE_PHOTO);
        } else {
            new MaterialDialog.Builder(mContext)
                    .title("操作")
                    .positiveText("换张新照片")
                    .positiveColorRes(R.color.primaryBrown)
//                    .negativeText("删除该照片")
//                    .negativeColorRes(R.color.primaryBrown)
                    .neutralText("取消")
                    .neutralColorRes(R.color.refund_button)
                    .forceStacking(true)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                                intent.setPhotoCount(1);
                                intent.setShowCamera(true);
                                startActivityForResult(intent, CHANGE_PHOTO);
                            } else if (dialogAction == DialogAction.NEGATIVE) {
                                // delete this picture
                                image.setVisibility(View.INVISIBLE);
                                emptyTip.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .show();
        }
    }

    private MaterialDialog inputDialog;
    private void changeCName() {
        String title = "修改餐品中文名";
        final int min = BraecoWaiterUtils.MIN_C_NAME_LENGTH;
        final int max = BraecoWaiterUtils.MAX_C_NAME_LENGTH;
        final String hint = "餐品中文名";
        final String fill = cNameString;
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
                            cNameString = materialDialog.getInputEditText().getText().toString();
                            cNameTV.setText(cNameString);
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
    }

    private double minPrice;
    private double newPrice;
    private void changePrice() {
        minPrice = BraecoWaiterUtils.MIN_PRICE;
        if (saleChoice == 2) {
            minPrice = saleNumber;
        }
        new MaterialDialog.Builder(mContext)
                .title("修改基础价格")
                .negativeText("取消")
                .positiveText("确认")
                .content("价格在 ¥ " + String.format("%.0f", minPrice) + "~" + String.format("%.0f", BraecoWaiterUtils.MAX_PRICE) + "之间")
                .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .input((priceDouble == -1 ? "" : String.format("%.2f", priceDouble)), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
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
                        if (!(minPrice <= newPrice && newPrice <= BraecoWaiterUtils.MAX_PRICE)) {
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
                            priceDouble = newPrice;
                            priceTV.setText("¥ " + String.format("%.2f", priceDouble));
                        }
                    }
                })
                .show();
    }

    private void changeEName() {
        String title = "修改餐品英文名";
        final int min = BraecoWaiterUtils.MIN_E_NAME_LENGTH;
        final int max = BraecoWaiterUtils.MAX_E_NAME_LENGTH;
        final String hint = "餐品英文名";
        final String fill = eNameString;
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
                            eNameString = materialDialog.getInputEditText().getText().toString();
                            eNameTV.setText(eNameString);
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
    }

    private void changeTag() {
        String title = "修改标签提醒";
        final int min = BraecoWaiterUtils.MIN_TAG_LENGTH;
        final int max = BraecoWaiterUtils.MAX_TAG_LENGTH;
        final String hint = "标签提醒";
        final String fill = tagsString;
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
                            tagsString = materialDialog.getInputEditText().getText().toString();
                            tagsTV.setText(tagsString);
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
    }

    private void changeAttributes() {
        if (!BraecoWaiterData.lastIsSaved) {
            BraecoWaiterData.attributes = new ArrayList<>();
            if (menuPosition != -1) {
                Map<String, Object> menu = BraecoWaiterApplication.mSettingMenu.get(menuPosition);
                int attributeGroupNum = (Integer) menu.get("num_shuxing");
                String[] attributeGroupName = (String[])menu.get("shuxingName");
                ArrayList<Double>[] attributePrice = (ArrayList<Double>[])menu.get("addshuxing");
                ArrayList<String>[] attributeItemName = (ArrayList<String>[])menu.get("shuxing");
                int[] attributeGroupSize = (int[])menu.get("res");
                for (int i = 0; i < attributeGroupNum; i++) {
                    Map<String, Object> group = new HashMap<>();
                    group.put("data", attributeGroupName[i]);
                    group.put("type", BraecoWaiterData.ATTRIBUTE_GROUP);
                    BraecoWaiterData.attributes.add(group);
                    for (int j = 0; j < attributeGroupSize[i]; j++) {
                        Map<String, Object> attribute = new HashMap<>();
                        attribute.put("data1", attributeItemName[i].get(j));
                        attribute.put("data2", String.format("%.2f", attributePrice[i].get(j)));
                        attribute.put("type", BraecoWaiterData.ATTRIBUTE);
                        BraecoWaiterData.attributes.add(attribute);
                    }
                    Map<String, Object> addAttribute = new HashMap<>();
                    addAttribute.put("type", BraecoWaiterData.ATTRIBUTE_ADD);
                    BraecoWaiterData.attributes.add(addAttribute);
                }
            }
            Map<String, Object> addGroup = new HashMap<>();
            addGroup.put("type", BraecoWaiterData.ATTRIBUTE_GROUP_ADD);
            BraecoWaiterData.attributes.add(addGroup);
            Intent intent = new Intent(mContext, MeFragmentMenuMenuEditAttribute.class);
            intent.putExtra("position", menuPosition);
            startActivity(intent);
        } else {
            Intent intent = new Intent(mContext, MeFragmentMenuMenuEditAttribute.class);
            intent.putExtra("position", menuPosition);
            startActivity(intent);
        }
    }

    private void writeAttribute() {
        String s = "";
        for (int i = 0; i < BraecoWaiterData.attributes.size(); i++) {
            if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                if ("".equals(s)) {
                    s += (String) BraecoWaiterData.attributes.get(i).get("data");
                } else {
                    s += "、" + (String) BraecoWaiterData.attributes.get(i).get("data");
                }
            }
        }
        attributesTV.setText(s);
    }

    private int newSaleNumber = -1;
    private void changeSale() {
        new MaterialDialog.Builder(mContext)
                .title("促销工具")
                .items(SALES_NAME)
                .negativeText("取消")
                .itemsCallbackSingleChoice(saleChoice, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                        if (which == 0 || which == 3 || which == 5) {
                            saleChoice = which;
                            saleNumber = -1;
                            writeSaleTV();
                            return true;
                        }
                        String content = SALES_CONTENT[which];
                        if (which == 2 && priceDouble != -1d && priceDouble < BraecoWaiterUtils.MAX_MINUS) {
                            if (priceDouble < BraecoWaiterUtils.MIN_MINUS) {
                                BraecoWaiterUtils.showToast(mContext, "立减金额至少为1，不适用于当前餐品价格");
                                saleChoice = 0;
                                saleNumber = -1;
                                writeSaleTV();
                                return true;
                            }
                            content = "立减金额在1.00~" + String.format("%.2f", priceDouble) + "之间";
                        }
                        new MaterialDialog.Builder(mContext)
                                .title(SALES_TITLE[which])
                                .content(content)
                                .negativeText("取消")
                                .positiveText("确认")
                                .inputType(InputType.TYPE_CLASS_NUMBER)
                                .input("", "", new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        // Do something
                                        newSaleNumber = -1;
                                        try {
                                            newSaleNumber = Integer.parseInt(String.valueOf(input));
                                        } catch (NumberFormatException n) {
                                            newSaleNumber = -1;
                                        }
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                        if (which == 1 && !(BraecoWaiterUtils.MIN_DISCOUNT <= newSaleNumber
                                                && newSaleNumber <= BraecoWaiterUtils.MAX_DISCOUNT)) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        }
                                        if (which == 2 && !(BraecoWaiterUtils.MIN_MINUS <= newSaleNumber
                                                && newSaleNumber <= BraecoWaiterUtils.MAX_MINUS
                                                && (priceDouble == -1d || (priceDouble != -1d && newSaleNumber <= priceDouble)))) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        }
                                        if (which == 4 && !(BraecoWaiterUtils.MIN_LIMIT <= newSaleNumber
                                                && newSaleNumber <= BraecoWaiterUtils.MAX_LIMIT)) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        }
                                    }
                                })
                                .alwaysCallInputCallback()
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        if (dialogAction == DialogAction.POSITIVE) {
                                            saleChoice = which;
                                            saleNumber = newSaleNumber;
                                            writeSaleTV();
                                        }
                                    }
                                })
                                .show();


                        dialog.dismiss();
                        return true;
                    }
                })
                .alwaysCallSingleChoiceCallback()
                .show();
    }

    private void writeSaleTV() {
        salesTV.setVisibility(View.VISIBLE);
        switch (saleChoice) {
            case 0:
                salesTV.setVisibility(View.INVISIBLE);
                break;
            case 1:
                salesTV.setText("折扣优惠 " + String.format("%.1f", 1.0d * saleNumber / 10) + "折");
                break;
            case 2:
                salesTV.setText("立减优惠 " + saleNumber + "元");
                break;
            case 3:
                salesTV.setText("第二杯半价");
                break;
            case 4:
                salesTV.setText("限量供应 " + saleNumber + "件");
                break;
            case 5:
                salesTV.setText("仅在套餐中出现");
                break;
        }
    }

    private void changeMore() {
        Intent intent = new Intent(mContext, EditDetailActivity.class);
        intent.putExtra("back", "");
        intent.putExtra("title", "详情介绍");
        if ("".equals(cNameString)) intent.putExtra("dialog", "餐品详情介绍");
        else intent.putExtra("dialog", "餐品 " + cNameString + " 详情介绍");
        intent.putExtra("edit", "完成");
        intent.putExtra("hint", "详情介绍");
        intent.putExtra("fill", moreString);
        intent.putExtra("old", moreString);
        intent.putExtra("help", "餐品详情介绍能为顾客提供更多信息");
        intent.putExtra("min", BraecoWaiterUtils.MIN_DETAIL_LENGTH);
        intent.putExtra("max", BraecoWaiterUtils.MAX_DETAIL_LENGTH);
        intent.putExtra("count", true);

        startActivityForResult(intent, CHANGE_DETAIL);
    }

    public void update() {
        if (!oldCNameString.equals(cNameString)) changed = true;
        if (!oldPriceDouble.equals(priceDouble)) changed = true;
        if (!oldENameString.equals(eNameString)) changed = true;
        if (!oldTagsString.equals(tagsString)) changed = true;
        if (BraecoWaiterData.attributeIsChanged) changed = true;
        if (oldSaleChoice != saleChoice) changed = true;
        if (oldSaleNumber != saleNumber) changed = true;
        if (!oldMoreString.equals(moreString)) changed = true;

        if (!changed) onBackPressed();
        else save();
    }

    @Override
    public void onBackPressed() {

        if (!oldCNameString.equals(cNameString)) changed = true;
        if (!oldPriceDouble.equals(priceDouble)) changed = true;
        if (!oldENameString.equals(eNameString)) changed = true;
        if (!oldTagsString.equals(tagsString)) changed = true;
        if (BraecoWaiterData.attributeIsChanged) changed = true;
        if (oldSaleChoice != saleChoice) changed = true;
        if (oldSaleNumber != saleNumber) changed = true;
        if (!oldMoreString.equals(moreString)) changed = true;

        if (changed) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content((buttonPosition == -1 ? ALERT_STRING[0] : ALERT_STRING[1]))
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                save();
//                                MeFragmentMenuMenuEdit.super.onBackPressed();
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                MeFragmentMenuMenuEdit.super.onBackPressed();
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

    private MaterialDialog progressDialog;
    private void save() {
        if (null == cNameString || "".equals(cNameString)) {
            BraecoWaiterUtils.showToast(mContext, "餐品中文名不能为空");
            return;
        }
        if (null == priceDouble || priceDouble.equals(-1)) {
            BraecoWaiterUtils.showToast(mContext, "基础价格不能为空");
            return;
        }
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("保存中")
                .content("请稍候")
                .cancelable(false)
                .progress(true, 0)
                .show();
        // we have to upload the data of the menu to get the id
        // then use the id to upload picture
        if (menuPosition == -1) {
            addMenu();
        } else {
            updateMenu();
        }
    }

    private void addMenu() {
        try {
            JSONObject property = new JSONObject();
            if (BraecoWaiterData.attributes.size() > 1) {
                int counterGroup = 0;
                for (int i = 0; i < BraecoWaiterData.attributes.size(); ) {
                    if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                        JSONObject group = new JSONObject();
                        group.put("groupname", BraecoWaiterData.attributes.get(i).get("data"));
                        int counterAttribute = 0;
                        int j = i + 1;
                        for (; j < BraecoWaiterData.attributes.size()
                                && BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(j).get("type")); j++) {
                            JSONObject attribute = new JSONObject();
                            attribute.put("name", BraecoWaiterData.attributes.get(j).get("data1"));
                            attribute.put("price", BraecoWaiterData.attributes.get(j).get("data2"));
                            group.put(counterAttribute++ + "", attribute);
                        }
                        property.put(counterGroup++ + "", group);
                        i = j;
                    } else {
                        i++;
                    }
                }
            }
            property.put("dishname", cNameString);
            if (!"".equals(eNameString)) property.put("dishname2", eNameString);
            property.put("defaultprice", priceDouble);

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + property.toString());
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + SALES_INFO[saleChoice]);
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + saleNumber + "");
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + ("".equals(tagsString) ? null : tagsString));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + BraecoWaiterApplication.mButton.get(buttonPosition).get("id"));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu: " + ("".equals(moreString) ? null : moreString));

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu encode: " + ("".equals(tagsString) ? null : URLEncoder.encode(tagsString, "utf-8")));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu encode: " + BraecoWaiterUtils.toUnicode(("".equals(tagsString) ? null : tagsString)));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu encode: " + ("".equals(moreString) ? null : URLEncoder.encode(moreString, "utf-8")));

            // property, dc_type, dc, tag, category, detail
            new AddMenu(false)
                    .execute(
                            "http://brae.co/Dish/Add",
                            BraecoWaiterUtils.toUnicode(property.toString()),
                            SALES_INFO[saleChoice],
                            saleNumber + "",
                            ("".equals(tagsString) ? null : tagsString),
                            BraecoWaiterApplication.mButton.get(buttonPosition).get("id") + "",
                            ("".equals(moreString) ? null : moreString));
        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu json error");
        } catch (UnsupportedEncodingException u) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu json error");
        }
    }

    private void updateMenu() {
        try {
            JSONObject property = new JSONObject();
            if (BraecoWaiterData.attributes.size() > 1) {
                int counterGroup = 0;
                for (int i = 0; i < BraecoWaiterData.attributes.size(); ) {
                    if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(BraecoWaiterData.attributes.get(i).get("type"))) {
                        JSONObject group = new JSONObject();
                        group.put("groupname", BraecoWaiterData.attributes.get(i).get("data"));
                        int counterAttribute = 0;
                        int j = i + 1;
                        for (; j < BraecoWaiterData.attributes.size()
                                && BraecoWaiterData.ATTRIBUTE.equals(BraecoWaiterData.attributes.get(j).get("type")); j++) {
                            JSONObject attribute = new JSONObject();
                            attribute.put("name", BraecoWaiterData.attributes.get(j).get("data1"));
                            attribute.put("price", BraecoWaiterData.attributes.get(j).get("data2"));
                            group.put(counterAttribute++ + "", attribute);
                        }
                        property.put(counterGroup++ + "", group);
                        i = j;
                    } else {
                        i++;
                    }
                }
            }
            property.put("dishname", cNameString);
            if (!"".equals(eNameString)) property.put("dishname2", eNameString);
            property.put("dishid", BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"));
            property.put("defaultprice", priceDouble);

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + "http://brae.co/Dish/Update/All/" + BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + property.toString());
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + SALES_INFO[saleChoice]);
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + saleNumber + "");
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + ("".equals(tagsString) ? null : tagsString));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu: " + ("".equals(moreString) ? null : moreString));

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu encode: " + ("".equals(tagsString) ? null : URLEncoder.encode(tagsString, "utf-8")));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu encode: " + BraecoWaiterUtils.toUnicode(("".equals(tagsString) ? null : tagsString)));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu encode: " + ("".equals(moreString) ? null : URLEncoder.encode(moreString, "utf-8")));

            // property, dc_type, dc, tag, category, detail
            new AddMenu(true)
                    .execute(
                            "http://brae.co/Dish/Update/All/" + BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"),
                            BraecoWaiterUtils.toUnicode(property.toString()),
                            SALES_INFO[saleChoice],
                            saleNumber + "",
                            ("".equals(tagsString) ? null : tagsString),
                            -1 + "",
                            ("".equals(moreString) ? null : moreString));
        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu json error");
        } catch (UnsupportedEncodingException u) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update menu json error");
        }
    }

    private class AddMenu extends AsyncTask<String, Void, String> {

        private boolean isUpdated = false;

        public AddMenu(boolean isUpdated) {
            this.isUpdated = isUpdated;
        }

        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                if (isUpdated) Log.d("BraecoWaiter", "Update menu: " + result);
                else Log.d("BraecoWaiter", "Add menu: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("property", params[1]));
            pairList.add(new BasicNameValuePair("dc_type", params[2]));
            pairList.add(new BasicNameValuePair("dc", params[3]));
            pairList.add(new BasicNameValuePair("tag", params[4]));
            if (!isUpdated) pairList.add(new BasicNameValuePair("category", params[5]));
            pairList.add(new BasicNameValuePair("detail", params[6]));
            try {
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(
                        pairList, "UTF-8");
                e.setContentEncoding(HTTP.UTF_8);
                HttpEntity requestHttpEntity = e;

                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (isUpdated) Log.d("BraecoWaiter", "Update menu: " + result);
            else Log.d("BraecoWaiter", "Add menu: " + result);

            progressDialog.dismiss();

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        if (pictureChange) {
                            if (menuPosition == -1) {
                                uploadPicture(true, array.getInt("id"));
                            } else {
                                uploadPicture(true, (Integer) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"));
                            }
                        } else {
                            if (menuPosition == -1) {
                                BraecoWaiterUtils.showToast(mContext, "添加餐品 " + cNameString + " 成功");
                                BraecoWaiterApplication.JUST_ADD_MENU = true;
                            } else {
                                BraecoWaiterUtils.showToast(mContext, "修改餐品 " + cNameString + " 成功");
                                BraecoWaiterApplication.JUST_UPDATE_MENU = true;
                            }
                            finish();
                        }
                    } else if ("Category not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品种类不存在");
                    } else if ("Dish not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品不存在");
                    } else if ("Invalid name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品中文名不合法");
                    } else if ("Invalid name2".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品英文名不合法");
                    } else if ("Invalid price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品基础价格不合法");
                    } else if ("Invalid tag".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品标签提醒不合法");
                    } else if ("Invalid detail".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品详情介绍不合法");
                    } else if ("Invalid group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组名不合法");
                    } else if ("Invalid property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性名不合法");
                    } else if ("Invalid property price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品价差值不合法");
                    } else if ("Conflicting group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组名冲突");
                    } else if ("Conflicting property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性名冲突");
                    } else if ("Too many properties in a group".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性数量超出限制");
                    } else if ("Too many groups".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组数量超出限制");
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu json error1");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }

    private int menuId = -1;
    private void uploadPicture(boolean isUpdated, int id) {
        menuId = id;
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("上传餐品图片中")
                .content("请耐心等待")
                .progress(false, 100, true)
                .cancelable(false)
                .show();
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "http://brae.co/pic/upload/token/dishadd/" + id);
        if (!isUpdated)
            new GetToken()
                    .execute("http://brae.co/pic/upload/token/dishadd/" + id);
        else
            new GetToken()
                    .execute("http://brae.co/pic/upload/token/dishupdate/" + id);
    }

    private class GetToken extends AsyncTask<String, Void, String> {

        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "GetToken:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        token = array.getString("token");
                        key = array.getString("key");

                        // true update
                        UploadManager uploadManager = new UploadManager();
                        uploadManager.put(imageString, key, token,
                                new UpCompletionHandler() {
                                    @Override
                                    public void complete(String key, ResponseInfo info, JSONObject res) {
                                        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Upload picture: " + "key: " + key + " res: " + res);
                                        // finish uploading
                                        if (info.isOK()) {
                                            progressDialog.dismiss();
                                            BraecoWaiterUtils.showToast(mContext, "上传餐品图片成功");
                                            BraecoWaiterApplication.JUST_ADD_MENU = true;
                                            finish();
                                        } else {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            failToUpload();
                                        }
                                    }
                                },
                                new UploadOptions(null, null, false,
                                        new UpProgressHandler(){
                                            public void progress(String key, double percent){
                                                progressDialog.setProgress((int)(percent * 100));
                                            }
                                        },
                                        new UpCancellationSignal() {
                                            public boolean isCancelled() {
                                                return false;
                                            }
                                        }));

                    } else {
                        if (progressDialog != null) progressDialog.dismiss();
                        failToUpload();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    if (progressDialog != null) progressDialog.dismiss();
                    failToUpload();
                }
            } else {
                if (progressDialog != null) progressDialog.dismiss();
                failToUpload();
            }
        }
    }

    private void failToUpload() {
        new MaterialDialog.Builder(mContext)
                .title("上传餐品图片失败")
                .content("您新建的餐品已经保存，但是餐品图片由于网络问题上传失败")
                .positiveText("确认")
                .negativeText("重试")
                .negativeColorRes(R.color.primaryBrown)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            uploadPicture((menuPosition != -1), menuId);
                        }
                        if (dialogAction == DialogAction.POSITIVE) {
                            BraecoWaiterApplication.JUST_ADD_MENU = true;
                            finish();
                        }
                    }
                })
                .show();
    }

}
