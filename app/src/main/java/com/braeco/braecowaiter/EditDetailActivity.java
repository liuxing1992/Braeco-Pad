package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;

public class EditDetailActivity extends BraecoAppCompatActivity
        implements
        TitleLayout.OnTitleActionListener {

    private TitleLayout title;

    private ScrollView scrollView;
    private EditText input;
    private TextView help;
    private TextView number;

    private Context mContext;

    private String oldInputString = "";
    private String dialogContent = "";
    private int min;
    private int max;
    private boolean chineseIsDoubleCount = false;
    private boolean exceed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_detail);

        mContext = this;

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        scrollView = (ScrollView)findViewById(R.id.scrollView);
        input = (EditText)findViewById(R.id.edittext);
        help = (TextView)findViewById(R.id.helper);
        number = (TextView)findViewById(R.id.number);

        title.setBack(getIntent().getStringExtra("back"));
        title.setTitle(getIntent().getStringExtra("title"));
        title.setEdit(getIntent().getStringExtra("edit"));
        dialogContent = getIntent().getStringExtra("dialog");
        input.setText(getIntent().getStringExtra("hint"));
        input.setText(getIntent().getStringExtra("fill"));
        oldInputString = getIntent().getStringExtra("old");
        help.setText(getIntent().getStringExtra("help"));
        min = getIntent().getIntExtra("min", 0);
        max = getIntent().getIntExtra("max", -1);
        chineseIsDoubleCount = getIntent().getBooleanExtra("count", true);

        input.setSelection(input.getText().toString().length());

        setNumberText();

        scrollView.fullScroll(View.FOCUS_DOWN);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setNumberText();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        clickTitleBack();
    }

    private void setNumberText() {
        int count = -1;
        if (chineseIsDoubleCount) {
            count = BraecoWaiterUtils.textCounter(input.getText().toString());
        } else {
            count =input.getText().toString().length();
        }
        number.setText(count + "/" + min + "-" + max);
        if (min <= count && count <= max) {
            number.setTextColor(ContextCompat.getColor(mContext, R.color.text_120));
            exceed = false;
        } else {
            number.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            exceed = true;
        }
    }

    @Override
    public void clickTitleBack() {
        if (oldInputString.equals(input.getText().toString())) {
            Intent intent = new Intent();
            intent.putExtra("new", oldInputString);
            intent.putExtra("changed", false);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            new MaterialDialog.Builder(mContext)
                    .title("保存")
                    .content("是否保存您对" + dialogContent + "的修改？")
                    .positiveText("保存")
                    .negativeText("不保存")
                    .neutralText("我再想想")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                if (exceed) {
                                    materialDialog.dismiss();
                                    new MaterialDialog.Builder(mContext)
                                            .title("字数不合法")
                                            .content("您对" + dialogContent + "的文本修改字数不合法")
                                            .positiveText("确认")
                                            .show();
                                } else {
                                    Intent intent = new Intent();
                                    intent.putExtra("new", input.getText().toString());
                                    intent.putExtra("changed", true);
                                    setResult(Activity.RESULT_OK, intent);
                                    BraecoWaiterUtils.showToast(mContext, "保存成功");
                                    finish();
                                }
                            } else if (dialogAction == DialogAction.NEGATIVE) {
                                Intent intent = new Intent();
                                intent.putExtra("new", oldInputString);
                                intent.putExtra("changed", false);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void doubleClickTitle() {
        scrollView.fullScroll(View.FOCUS_UP);
    }

    @Override
    public void clickTitleEdit() {
        if (exceed) {
            new MaterialDialog.Builder(mContext)
                    .title("字数不合法")
                    .content("您对" + dialogContent + "的文本修改字数不合法")
                    .positiveText("确认")
                    .show();
        } else {
            Intent intent = new Intent();
            intent.putExtra("new", input.getText().toString());
            intent.putExtra("changed", true);
            setResult(Activity.RESULT_OK, intent);
            BraecoWaiterUtils.showToast(mContext, "保存成功");
            finish();
        }
    }
}
