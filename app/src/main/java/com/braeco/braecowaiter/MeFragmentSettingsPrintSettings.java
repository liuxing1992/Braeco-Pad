package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnGetTableAsyncTaskListener;
import com.braeco.braecowaiter.Interfaces.OnSetPrinterAsyncTaskListener;
import com.braeco.braecowaiter.Model.Printer;
import com.braeco.braecowaiter.Tasks.GetTableAsyncTask;
import com.braeco.braecowaiter.Tasks.SetPrinterAsyncTask;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;

public class MeFragmentSettingsPrintSettings extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        TitleLayout.OnTitleActionListener {

    private MaterialDialog mInputDialog;
    private MaterialDialog mLoadingDialog;

    private Context mContext;

    private Printer mOldPrinter;
    private int mPosition = -1;

    private TitleLayout title;
    private ScrollView scrollView;

    private View separate;
    private TextView separateTextView;
    private View page;
    private TextView pageTextView;
    private View width;
    private TextView widthTextView;
    private View size;
    private TextView sizeTextView;
    private View offset;
    private TextView offsetTextView;
    private View remark;
    private TextView remarkTextView;
    private View meal;
    private View table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_print_settings);

        mContext = this;

        mPosition = getIntent().getIntExtra("position", -1);
        if (mPosition == -1 || BraecoWaiterApplication.printers == null) {
            finish();
            return;
        }
        mOldPrinter = BraecoWaiterApplication.printers.get(mPosition);
        try {
            BraecoWaiterApplication.modifyingPrinter = (Printer) mOldPrinter.clone();
        } catch (CloneNotSupportedException e) {
            finish();
            e.printStackTrace();
        }

        title = (TitleLayout) findViewById(R.id.title_layout);
        title.setTitle(BraecoWaiterApplication.modifyingPrinter.getName());
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        separate = findViewById(R.id.separate);
        separate.setOnClickListener(this);
        separateTextView = (TextView)findViewById(R.id.separate_text);
        page = findViewById(R.id.page);
        page.setOnClickListener(this);
        pageTextView = (TextView)findViewById(R.id.page_text);
        width = findViewById(R.id.width);
        width.setOnClickListener(this);
        widthTextView = (TextView)findViewById(R.id.width_text);
        size = findViewById(R.id.size);
        size.setOnClickListener(this);
        sizeTextView = (TextView)findViewById(R.id.size_text);
        offset = findViewById(R.id.offset);
        offset.setOnClickListener(this);
        offsetTextView = (TextView)findViewById(R.id.offset_text);
        remark = findViewById(R.id.remark);
        remark.setOnClickListener(this);
        remarkTextView = (TextView)findViewById(R.id.remark_text);
        meal = findViewById(R.id.meal);
        meal.setOnClickListener(this);
        table = findViewById(R.id.table);
        table.setOnClickListener(this);

        if (BraecoWaiterApplication.modifyingPrinter.getWidth() == 0) {
            width.setVisibility(View.GONE);
            offset.setVisibility(View.GONE);
            size.setVisibility(View.GONE);
        }

        setText();
    }

    private void setText() {
        separateTextView.setText(BraecoWaiterApplication.modifyingPrinter.isSeparate() ? "每个餐品单独一票" : "所有餐品合并一票");
        pageTextView.setText("每单" + BraecoWaiterApplication.modifyingPrinter.getPage() + "联");
        widthTextView.setText(BraecoWaiterApplication.modifyingPrinter.getWidth() + "mm");
        sizeTextView.setText(BraecoWaiterApplication.modifyingPrinter.getSize() + "");
        offsetTextView.setText(BraecoWaiterApplication.modifyingPrinter.getOffset() + "");
        remarkTextView.setText(BraecoWaiterApplication.modifyingPrinter.getRemark());
    }

    private void quit() {
        if (mOldPrinter.equals(BraecoWaiterApplication.modifyingPrinter)) {
            finish();
        } else {
            // ask whether change
            new MaterialDialog.Builder(this)
                    .title("保存")
                    .content("您已对打印机 " + BraecoWaiterApplication.modifyingPrinter.getName() + " 做出了修改，请问是否保存修改？")
                    .positiveText("保存")
                    .negativeText("不保存")
                    .neutralText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which.equals(DialogAction.POSITIVE)) {
                                save();
                            } else if (which.equals(DialogAction.NEGATIVE)) {
                                finish();
                            }
                        }
                    })
                    .show();
        }
    }

    private void save() {
        mLoadingDialog = new MaterialDialog.Builder(mContext)
                .title("保存中……")
                .content("正在保存打印机信息，请耐心等候。")
                .cancelable(true)
                .negativeText("取消")
                .progress(true, 0)
                .show();
        new SetPrinterAsyncTask(mOnSetPrinterAsyncTaskListener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.separate:
                new MaterialDialog.Builder(this)
                        .title("打印方式")
                        .positiveText("所有餐品合并一票")
                        .negativeText("每个餐品单独一票")
                        .negativeColorRes(R.color.primaryBrown)
                        .neutralText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which.equals(DialogAction.POSITIVE)) {
                                    BraecoWaiterApplication.modifyingPrinter.setSeparate(false);
                                } else if (which.equals(DialogAction.NEGATIVE)) {
                                    BraecoWaiterApplication.modifyingPrinter.setSeparate(true);
                                }
                                setText();
                            }
                        })
                        .show();
                break;
            case R.id.page:
                String[] pages = new String[20];
                for (int i = 1; i <= 20; i++) pages[i - 1] = "每单" + i + "联";
                new MaterialDialog.Builder(this)
                        .title("打印联数")
                        .items(pages)
                        .negativeText("取消")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                BraecoWaiterApplication.modifyingPrinter.setPage(which + 1);
                                setText();
                            }
                        })
                        .show();
                break;
            case R.id.width:
                String[] widths = new String[90];
                for (int i = 1; i <= 90; i++) widths[i - 1] = i + "mm";
                new MaterialDialog.Builder(this)
                        .title("纸张宽度")
                        .items(widths)
                        .negativeText("取消")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                BraecoWaiterApplication.modifyingPrinter.setWidth(which + 1);
                                setText();
                            }
                        })
                        .show();
                break;
            case R.id.size:
                String[] sizes = new String[64];
                for (int i = 1; i <= 64; i++) sizes[i - 1] = i + "";
                new MaterialDialog.Builder(this)
                        .title("字号大小")
                        .items(sizes)
                        .negativeText("取消")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                BraecoWaiterApplication.modifyingPrinter.setSize(which + 1);
                                setText();
                            }
                        })
                        .show();
                break;
            case R.id.offset:
                String[] offsets = new String[128];
                for (int i = 0; i < 128; i++) offsets[i] = i + "";
                new MaterialDialog.Builder(this)
                        .title("偏移量调节")
                        .items(offsets)
                        .negativeText("取消")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                BraecoWaiterApplication.modifyingPrinter.setOffset(which);
                                setText();
                            }
                        })
                        .show();
                break;
            case R.id.remark:
                String title = "修改备注";
                final int min = 0;
                final int max = 64;
                final String hint = "打印机备注";
                final String fill = BraecoWaiterApplication.modifyingPrinter.getRemark();
                mInputDialog = new MaterialDialog.Builder(mContext)
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
                                    BraecoWaiterApplication.modifyingPrinter.setRemark(materialDialog.getInputEditText().getText().toString());
                                    setText();
                                }
                            }
                        })
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                int count = BraecoWaiterUtils.textCounter(String.valueOf(fill));
                                String pre = BraecoWaiterUtils.invalidString(fill);
                                mInputDialog.setContent(
                                        BraecoWaiterUtils.getDialogContent(mContext,
                                                pre,
                                                count + "/" + min + "-" + max,
                                                (min <= count && count <= max)));
                                mInputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                if (!(min <= count && count <= max) || pre.length() != 0) {
                                    mInputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                }
                            }
                        })
                        .alwaysCallInputCallback()
                        .show();
                break;
            case R.id.meal:
                startActivity(new Intent(mContext, MeFragmentSettingsPrintSettingsMeal.class));
                break;
            case R.id.table:
                if (BraecoWaiterApplication.tables == null) {
                    // We haven't get the tables.
                    BraecoWaiterUtils.showToast(mContext, "桌位为空，正在刷新，请稍后再试");
                    new GetTableAsyncTask(mOnGetTableAsyncTaskListener)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                } else {
                    startActivity(new Intent(mContext, MeFragmentSettingsPrintSettingsTable.class));
                }
                break;
        }
    }

    private OnSetPrinterAsyncTaskListener mOnSetPrinterAsyncTaskListener = new OnSetPrinterAsyncTaskListener() {
        @Override
        public void success() {
            if (mLoadingDialog != null) mLoadingDialog.dismiss();
            BraecoWaiterApplication.printers.set(mPosition, BraecoWaiterApplication.modifyingPrinter);
            new MaterialDialog.Builder(mContext)
                    .title("修改成功")
                    .content("修改打印机 " + BraecoWaiterApplication.modifyingPrinter.getName() + " 信息成功。")
                    .cancelable(true)
                    .positiveText("确定")
                    .show()
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
        }

        @Override
        public void fail(String message) {
            if (mLoadingDialog != null) mLoadingDialog.dismiss();
            new MaterialDialog.Builder(mContext)
                    .title("修改失败")
                    .content("修改打印机 " + BraecoWaiterApplication.modifyingPrinter.getName() + " 信息失败（" + message + "），是否重试？")
                    .cancelable(true)
                    .positiveText("重试")
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which.equals(DialogAction.POSITIVE)) {
                                dialog.dismiss();
                                save();
                            }
                        }
                    })
                    .show();
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    private OnGetTableAsyncTaskListener mOnGetTableAsyncTaskListener = new OnGetTableAsyncTaskListener() {
        @Override
        public void success() {
            BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "桌位刷新成功");
        }

        @Override
        public void fail(String message) {
            BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "桌位刷新失败（" + message + "）");
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

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
        save();
    }
}
