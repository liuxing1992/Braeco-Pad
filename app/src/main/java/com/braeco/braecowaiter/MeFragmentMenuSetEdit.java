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
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
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
import org.json.JSONArray;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MeFragmentMenuSetEdit extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        TitleLayout.OnTitleActionListener {

    private static final int CHANGE_PHOTO = 1;
    private static final int CHANGE_DETAIL = 2;
    private static final String[] SALES_NAME = new String[]{"无", "折扣优惠", "立减优惠", "第二杯半价", "限量供应"};
    private static final String[] SALES_TITLE = new String[]{"", "折扣力度", "立减金额", "", "限量数目"};
    private static final String[] SALES_CONTENT = new String[]{"", "折扣力度在10~99之间", "立减金额在1~50之间", "", "限量数目在1~99之间"};
    private static final String[] ALERT_STRING = new String[]{"是否保存您新建的套餐？", "是否保存您对该套餐的修改？"};
    private static final String[] SALES_INFO = new String[]{"none", "discount", "sale", "half", "limit"};
    private static String SIZE = null;

    private LinearLayout cName;
    private LinearLayout price;
    private LinearLayout eName;
    private LinearLayout tags;
    private LinearLayout set;
    private LinearLayout attributes;
    private LinearLayout more;

    private TitleLayout title;
    private ScrollView scrollView;
    private TextView emptyTip;
    private TextView cNameTV;
    private TextView priceTV;
    private TextView eNameTV;
    private TextView tagsTV;
    private TextView setTV;
    private TextView attributesTV;
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
    // -1 means no settings, -2 means second choice, other means first choice
    private Double priceDouble = -1d;
    private String eNameString = "";
    private String tagsString = "";
    private int setChoice = -1;
    private String moreString = "";

    private String token = "";
    private String key = "";

    private String oldCNameString = "";
    private Double oldPriceDouble = -1d;
    private String oldENameString = "";
    private String oldTagsString = "";
    private int oldSetChoice = -1;
    private String oldMoreString = "";

    private boolean typeSet = false;
    private boolean isSumType = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_set_edit);

        mContext = this;

        menuPosition = getIntent().getIntExtra("position", -1);
        buttonPosition = getIntent().getIntExtra("buttonPosition", -1);

        if (SIZE == null) {
            SIZE = "?imageView2/1/w/" + getResources().getDisplayMetrics().widthPixels
                    + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);
        }

        BraecoWaiterData.setAttributes = new ArrayList<>();
        if (menuPosition != -1) {
            Map<String, Object> menu = BraecoWaiterApplication.mSettingMenu.get(menuPosition);
            ArrayList<Map<String, Object>> originalCombo = (ArrayList<Map<String, Object>>)menu.get("combo");
            int comboNum = originalCombo.size();
            for (int i = 0; i < comboNum; i++) {
                Map<String, Object> comboName = new HashMap<>();
                comboName.put("data", originalCombo.get(i).get("name"));
                comboName.put("type", BraecoWaiterData.SET_ATTRIBUTE_NAME);
                BraecoWaiterData.setAttributes.add(comboName);

                Map<String, Object> comboBody = new HashMap<>();
                HashSet<Integer> ids = new HashSet<>();
                for (Integer v : (HashSet<Integer>)originalCombo.get(i).get("content")) {
                    ids.add(v);
                }
                comboBody.put("data1", ids);
                comboBody.put("data2", originalCombo.get(i).get("require"));
                if ("combo_sum".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                    comboBody.put("data3", originalCombo.get(i).get("discount"));
                } else {
                    // is static, add 100 to discount
                    comboBody.put("data3", 100);
                }
                comboBody.put("type", BraecoWaiterData.SET_ATTRIBUTE_BODY);
                BraecoWaiterData.setAttributes.add(comboBody);
            }
            Map<String, Object> addCombo = new HashMap<>();
            addCombo.put("type", BraecoWaiterData.SET_ATTRIBUTE_ADD);
            BraecoWaiterData.setAttributes.add(addCombo);
        }
        BraecoWaiterData.setLastIsSaved = false;
        BraecoWaiterData.setAttributeIsChanged = false;

        cName = (LinearLayout)findViewById(R.id.c_name);
        price = (LinearLayout)findViewById(R.id.price);
        eName = (LinearLayout)findViewById(R.id.e_name);
        tags = (LinearLayout)findViewById(R.id.tags);
        set = (LinearLayout)findViewById(R.id.set);
        attributes = (LinearLayout)findViewById(R.id.attributes);
        more = (LinearLayout)findViewById(R.id.more);

        image = (KenBurnsView)findViewById(R.id.image);
        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        cNameTV = (TextView)findViewById(R.id.c_name_text);
        priceTV = (TextView)findViewById(R.id.price_text);
        eNameTV = (TextView)findViewById(R.id.e_name_text);
        tagsTV = (TextView)findViewById(R.id.tags_text);
        setTV = (TextView)findViewById(R.id.set_text);

        attributesTV = (TextView)findViewById(R.id.attributes_text);
        moreTV = (TextView)findViewById(R.id.more_text);

        cName.setOnClickListener(this);
        price.setOnClickListener(this);
        eName.setOnClickListener(this);
        tags.setOnClickListener(this);
        attributes.setOnClickListener(this);
        set.setOnClickListener(this);
        more.setOnClickListener(this);
        emptyTip.setOnClickListener(this);
        image.setOnClickListener(this);

        title.setBack("套餐");
        title.setEdit("完成");
        if (menuPosition != -1) {
            title.setTitle("套餐修改");
            cNameString = (String)BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("name");
            cNameTV.setText(cNameString);
            if ("combo_sum".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("dc_type"))) {
                isSumType = true;
                typeSet = true;
                priceDouble = -2d;
                priceTV.setText("子项加权（¥" + String.format("%.2f", BraecoWaiterUtils.calculateComboSum(BraecoWaiterData.setAttributes, false)) + "起）");
            } else {
                isSumType = false;
                typeSet = true;
                priceDouble = (Double) BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("price");
                priceTV.setText("固定总价（¥" + priceDouble + "）");
            }

            eNameString = (String)BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("en_name");
            eNameTV.setText(eNameString);
            if (BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail") == null
                    || "null".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail"))) {
                moreString = "";
            } else {
                moreString = (String)BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("detail");
            }
            moreTV.setText(moreString);
            if (BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag") == null
                    || "null".equals(BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag"))) {
                tagsString = "";
            } else {
                tagsString = (String)BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("tag");
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
            int categoryId = (Integer)BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("categoryid");
            setChoice = categoryId;

            oldCNameString = cNameString;
            oldPriceDouble = priceDouble;
            oldENameString = eNameString;
            oldTagsString = tagsString;
            oldSetChoice = setChoice;
            oldMoreString = moreString;
        } else {
            title.setTitle(("新建套餐"));
            cNameTV.setText("");
            priceTV.setText("");
            eNameTV.setText("");
            tagsString = "";
            tagsTV.setText(tagsString);
            moreString = "";
            moreTV.setText(moreString);
            image.setVisibility(View.INVISIBLE);
            emptyTip.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        writeAttribute();
        writeSetChoice();
        if (isSumType) {
            priceTV.setText("子项加权（¥" + String.format("%.2f", BraecoWaiterUtils.calculateComboSum(BraecoWaiterData.setAttributes, false)) + "起）");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.set:
                changeSet();
                break;
            case R.id.more:
                changeMore();
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
        String title = "修改套餐中文名";
        final int min = BraecoWaiterUtils.MIN_C_NAME_LENGTH;
        final int max = BraecoWaiterUtils.MAX_C_NAME_LENGTH;
        final String hint = "套餐中文名";
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

    private int choice;
    private double minPrice;
    private double newPrice;
    private String[] priceChoices = new String[]{"固定总价", "子项加权"};
    private void changePrice() {
        minPrice = BraecoWaiterUtils.MIN_PRICE;
        if (typeSet) {
            // the type has been set
            if (isSumType) {
                // is combo_sum
                choice = 1;
            } else {
                choice = 0;
            }
        } else {
            choice = -1;
        }
        new MaterialDialog.Builder(mContext)
                .title("套餐总价")
                .items(priceChoices)
                .negativeText("取消")
                .itemsCallbackSingleChoice(choice, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                        if (which == 0) {
                            dialog.dismiss();
                            new MaterialDialog.Builder(mContext)
                                    .title("修改固定总价")
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
                                                isSumType = false;
                                                typeSet = true;
                                                priceDouble = newPrice;
                                                priceTV.setText("固定总价（¥" + String.format("%.2f", priceDouble) + "）");
                                            }
                                        }
                                    })
                                    .show();
                        } else {
                            dialog.dismiss();
                            new MaterialDialog.Builder(mContext)
                                    .title("子项加权")
                                    .content("套餐总价将根据各子项优惠折扣价格加总计算")
                                    .positiveText("确定")
                                    .negativeText("取消")
                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            if (dialogAction == DialogAction.POSITIVE) {
                                                isSumType = true;
                                                typeSet = true;
                                                priceDouble = -2D;
                                                priceTV.setText("子项加权（¥" + String.format("%.2f", BraecoWaiterUtils.calculateComboSum(BraecoWaiterData.setAttributes, false)) + "起）");
                                            }
                                        }
                                    })
                                    .show();
                        }
                        return true;
                    }
                })
                .show();
    }

    private void changeEName() {
        String title = "修改套餐英文名";
        final int min = BraecoWaiterUtils.MIN_E_NAME_LENGTH;
        final int max = BraecoWaiterUtils.MAX_E_NAME_LENGTH;
        final String hint = "套餐英文名";
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
        if (!typeSet) {
            // you should set the type of the set first
            new MaterialDialog.Builder(mContext)
                    .title("子项设置")
                    .content("请先设置套餐总价类型，再进行子项设置。")
                    .positiveText("确认")
                    .show();
        } else {
            if (!BraecoWaiterData.setLastIsSaved) {
                BraecoWaiterData.setAttributes = new ArrayList<>();
                if (menuPosition != -1) {
                    Map<String, Object> menu = BraecoWaiterApplication.mSettingMenu.get(menuPosition);
                    ArrayList<Map<String, Object>> originalCombo = (ArrayList<Map<String, Object>>)menu.get("combo");
                    int comboNum = originalCombo.size();
                    for (int i = 0; i < comboNum; i++) {
                        Map<String, Object> comboName = new HashMap<>();
                        comboName.put("data", originalCombo.get(i).get("name"));
                        comboName.put("type", BraecoWaiterData.SET_ATTRIBUTE_NAME);
                        BraecoWaiterData.setAttributes.add(comboName);

                        Map<String, Object> comboBody = new HashMap<>();
                        HashSet<Integer> ids = new HashSet<>();
                        for (Integer v : (HashSet<Integer>)originalCombo.get(i).get("content")) {
                            ids.add(v);
                        }
                        comboBody.put("data1", ids);
                        comboBody.put("data2", originalCombo.get(i).get("require"));
                        comboBody.put("data3", originalCombo.get(i).get("discount"));
                        comboBody.put("type", BraecoWaiterData.SET_ATTRIBUTE_BODY);
                        BraecoWaiterData.setAttributes.add(comboBody);
                    }
                }
                Map<String, Object> addCombo = new HashMap<>();
                addCombo.put("type", BraecoWaiterData.SET_ATTRIBUTE_ADD);
                BraecoWaiterData.setAttributes.add(addCombo);

                Intent intent = new Intent(mContext, MeFragmentMenuSetEditAttribute.class);
                intent.putExtra("position", menuPosition);
                intent.putExtra("setType", isSumType ? 0 : 1);
                startActivity(intent);
            } else {
                Intent intent = new Intent(mContext, MeFragmentMenuSetEditAttribute.class);
                intent.putExtra("position", menuPosition);
                intent.putExtra("setType", isSumType ? 0 : 1);
                startActivity(intent);
            }
        }
    }

    private void writeSetChoice() {
        if (setChoice != -1) {
            for (int i = BraecoWaiterApplication.mButton.size() - 1; i >= 0; i--) {
                if (BraecoWaiterApplication.mButton.get(i).get("id").equals(setChoice)) {
                    setTV.setText((String)BraecoWaiterApplication.mButton.get(i).get("button"));
                }
            }
        } else {
            setTV.setText("");
        }
    }

    private void writeAttribute() {
        String s = "";
        for (int i = 0; i < BraecoWaiterData.setAttributes.size(); i++) {
            if (BraecoWaiterData.SET_ATTRIBUTE_NAME.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                if ("".equals(s)) {
                    s += (String)BraecoWaiterData.setAttributes.get(i).get("data");
                } else {
                    s += "、" + (String)BraecoWaiterData.setAttributes.get(i).get("data");
                }
            }
        }
        attributesTV.setText(s);
    }

    private String[] setChoices = null;
    private void changeSet() {
        if (menuPosition != -1) {
            // when update, you cannot set the category
            // you have to use the move method
            new MaterialDialog.Builder(mContext)
                    .title("品类放置")
                    .content("更改套餐所在品类请在套餐设置界面编辑。")
                    .positiveText("确认")
                    .show();
        } else {
            int selected = -1;
            setChoices = new String[BraecoWaiterApplication.mButton.size() - 1];
            for (int i = 0; i < setChoices.length; i++) {
                setChoices[i] = (String)BraecoWaiterApplication.mButton.get(i + 1).get("button");
                if (BraecoWaiterApplication.mButton.get(i + 1).get("id").equals(setChoice)) {
                    selected = i;
                }
            }
            new MaterialDialog.Builder(mContext)
                    .title("品类放置")
                    .items(setChoices)
                    .negativeText("取消")
                    .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            setChoice = (Integer)BraecoWaiterApplication.mButton.get(which + 1).get("id");
                            writeSetChoice();
                            return true;
                        }
                    })
                    .show();
        }
    }

    private void changeMore() {
        Intent intent = new Intent(mContext, EditDetailActivity.class);
        intent.putExtra("back", "");
        intent.putExtra("title", "详情介绍");
        if ("".equals(cNameString)) intent.putExtra("dialog", "套餐详情介绍");
        else intent.putExtra("dialog", "套餐 " + cNameString + " 详情介绍");
        intent.putExtra("edit", "完成");
        intent.putExtra("hint", "详情介绍");
        intent.putExtra("fill", moreString);
        intent.putExtra("old", moreString);
        intent.putExtra("help", "套餐详情介绍能为顾客提供更多信息");
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
        if (oldSetChoice != setChoice) changed = true;
        if (BraecoWaiterData.setAttributeIsChanged) changed = true;
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
        if (oldSetChoice != setChoice) changed = true;
        if (BraecoWaiterData.setAttributeIsChanged) changed = true;
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
                                MeFragmentMenuSetEdit.super.onBackPressed();
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
            BraecoWaiterUtils.showToast(mContext, "套餐中文名不能为空");
            return;
        }
        if (null == priceDouble || priceDouble.equals(-1)) {
            BraecoWaiterUtils.showToast(mContext, "套餐总价不能为空");
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
            addSet();
        } else {
            updateSet();
        }
    }

    private void addSet() {
        try {
            JSONObject property = new JSONObject();
            JSONArray combos = new JSONArray();
            if (BraecoWaiterData.setAttributes.size() > 1) {
                for (int i = 0; i < BraecoWaiterData.setAttributes.size(); ) {
                    JSONObject combo = new JSONObject();
                    combo.put("name", BraecoWaiterData.setAttributes.get(i).get("data"));
                    combo.put("require", BraecoWaiterData.setAttributes.get(i + 1).get("data2"));
                    if (isSumType) {
                        // is sum type
                        combo.put("discount", BraecoWaiterData.setAttributes.get(i + 1).get("data3"));
                    }
                    HashSet<Integer> ids = (HashSet<Integer>) BraecoWaiterData.setAttributes.get(i + 1).get("data1");
                    ArrayList<Integer> idsInArray = new ArrayList<>();
                    for (Integer id : ids) idsInArray.add(id);
                    combo.put("content", new JSONArray(idsInArray));
                    combos.put(combo);
                    i += 2;
                    if (BraecoWaiterData.SET_ATTRIBUTE_ADD.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                        // end
                        break;
                    }
                }
            }
            property.put("dishname", cNameString);
            if (!"".equals(eNameString)) property.put("dishname2", eNameString);
            property.put("defaultprice", (isSumType ?  BraecoWaiterUtils.calculateComboSum(BraecoWaiterData.setAttributes, false) : priceDouble));
            property.put("combo", combos);

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + property.toString());
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + (isSumType ? "combo_sum" : "combo_static"));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + 0 + "");
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + ("".equals(tagsString) ? null : tagsString));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + setChoice);
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set: " + ("".equals(moreString) ? null : moreString));

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set encode: " + ("".equals(tagsString) ? null : URLEncoder.encode(tagsString, "utf-8")));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set encode: " + BraecoWaiterUtils.toUnicode(("".equals(tagsString) ? null : tagsString)));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set encode: " + ("".equals(moreString) ? null : URLEncoder.encode(moreString, "utf-8")));

            // property, dc_type, dc, tag, category, detail
            new AddSet(false)
                    .execute(
                            "http://brae.co/Dish/Add",
                            BraecoWaiterUtils.toUnicode(property.toString()),
                            isSumType ? "combo_sum" : "combo_static",
                            0 + "",
                            ("".equals(tagsString) ? null : tagsString),
                            setChoice + "",
                            ("".equals(moreString) ? null : moreString));
        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set json error");
        } catch (UnsupportedEncodingException u) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set json error");
        }
    }

    private String comboFucker(String string) {
        return "";
    }

    private void updateSet() {
        try {
            JSONObject property = new JSONObject();
            JSONArray combos = new JSONArray();
            if (BraecoWaiterData.setAttributes.size() > 1) {
                for (int i = 0; i < BraecoWaiterData.setAttributes.size(); ) {
                    JSONObject combo = new JSONObject();
                    combo.put("name", BraecoWaiterData.setAttributes.get(i).get("data"));
                    combo.put("require", BraecoWaiterData.setAttributes.get(i + 1).get("data2"));
                    if (isSumType) {
                        // is sum type
                        combo.put("discount", BraecoWaiterData.setAttributes.get(i + 1).get("data3"));
                    }
                    HashSet<Integer> ids = (HashSet<Integer>) BraecoWaiterData.setAttributes.get(i + 1).get("data1");
                    JSONArray idsInArray = new JSONArray();
                    for (Integer id : ids) {
                        idsInArray.put(id);
                    }
                    combo.put("content", idsInArray);
                    combos.put(combo);
                    i += 2;
                    if (BraecoWaiterData.SET_ATTRIBUTE_ADD.equals(BraecoWaiterData.setAttributes.get(i).get("type"))) {
                        // end
                        break;
                    }
                }
            }
            property.put("dishname", cNameString);
            if (!"".equals(eNameString)) property.put("dishname2", eNameString);
            property.put("dishid", BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"));
            property.put("defaultprice", (isSumType ? BraecoWaiterUtils.calculateComboSum(BraecoWaiterData.setAttributes, false) : priceDouble));
            property.put("combo", combos);

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + "http://brae.co/Dish/Update/All/" + BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + property.toString());
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + (isSumType ? "combo_sum" : "combo_static"));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + 0 + "");
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + ("".equals(tagsString) ? null : tagsString));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set: " + ("".equals(moreString) ? null : moreString));

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set encode: " + ("".equals(tagsString) ? null : URLEncoder.encode(tagsString, "utf-8")));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set encode: " + BraecoWaiterUtils.toUnicode(("".equals(tagsString) ? null : tagsString)));
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set encode: " + ("".equals(moreString) ? null : URLEncoder.encode(moreString, "utf-8")));

            // property, dc_type, dc, tag, category, detail
            new AddSet(true)
                    .execute(
                            "http://brae.co/Dish/Update/All/" + BraecoWaiterApplication.mSettingMenu.get(menuPosition).get("id"),
                            BraecoWaiterUtils.toUnicode(property.toString()),
                            isSumType ? "combo_sum" : "combo_static",
                            0 + "",
                            ("".equals(tagsString) ? null : tagsString),
                            -1 + "",
                            ("".equals(moreString) ? null : moreString));
        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set json error");
        } catch (UnsupportedEncodingException u) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update set json error");
        }
    }

    @Override
    public void clickTitleBack() {
        onBackPressed();
    }

    @Override
    public void doubleClickTitle() {
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void clickTitleEdit() {
        update();
    }

    private class AddSet extends AsyncTask<String, Void, String> {

        private boolean isUpdated = false;

        public AddSet(boolean isUpdated) {
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
                if (isUpdated) Log.d("BraecoWaiter", "Update set: " + result);
                else Log.d("BraecoWaiter", "Add set: " + result);
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

            if (isUpdated) Log.d("BraecoWaiter", "Update set: " + result);
            else Log.d("BraecoWaiter", "Add set: " + result);

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
                                uploadPicture(true, (Integer) BraecoWaiterApplication.mMenu.get(menuPosition).get("id"));
                            }
                        } else {
                            if (menuPosition == -1) {
                                BraecoWaiterUtils.showToast(mContext, "添加套餐 " + cNameString + " 成功");
                                BraecoWaiterApplication.JUST_ADD_MENU = true;
                            } else {
                                BraecoWaiterUtils.showToast(mContext, "修改套餐 " + cNameString + " 成功");
                                BraecoWaiterApplication.JUST_UPDATE_MENU = true;
                            }
                            finish();
                        }
                    } else if ("Invalid combo".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐格式不正确");
                    } else if ("Category not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐种类不存在");
                    } else if ("Dish not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐不存在");
                    } else if ("Invalid name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐中文名不合法");
                    } else if ("Invalid name2".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐英文名不合法");
                    } else if ("Invalid price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐基础价格不合法");
                    } else if ("Invalid tag".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐标签提醒不合法");
                    } else if ("Invalid detail".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐详情介绍不合法");
                    } else if ("Invalid group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性组名不合法");
                    } else if ("Invalid property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性名不合法");
                    } else if ("Invalid property price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐价差值不合法");
                    } else if ("Conflicting group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性组名冲突");
                    } else if ("Conflicting property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性名冲突");
                    } else if ("Too many properties in a group".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性数量超出限制");
                    } else if ("Too many groups".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "套餐属性组数量超出限制");
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add set json error1");
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
                .title("上传套餐图片中")
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
                                            BraecoWaiterUtils.showToast(mContext, "上传套餐图片成功");
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
                .title("上传套餐图片失败")
                .content("您新建的套餐已经保存，但是套餐图片由于网络问题上传失败")
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
