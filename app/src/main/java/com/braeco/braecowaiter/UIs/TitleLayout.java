package com.braeco.braecowaiter.UIs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.braeco.braecowaiter.R;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Weiping on 2016/5/16.
 */
public class TitleLayout extends FrameLayout {

    private Context mContext;
    private OnTitleActionListener mListener;

    private MaterialIconView back;
    private TextView backText;
    private TextView title;
    private TextView edit;

    public TitleLayout(Context context) {
        super(context);

        mContext = context;
        init(null);
    }

    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (mContext instanceof OnTitleActionListener) mListener = (OnTitleActionListener) mContext;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ui_title_layout, this);

        TypedArray ta = attrs == null ?
                null : getContext().obtainStyledAttributes(attrs, R.styleable.TitleLayout);
        boolean hasBackText = true;
        boolean hasTitleText = true;
        boolean hasEditText = true;
        String backString = "";
        String titleString = "";
        String editString = "";

        if (ta != null) {
            hasBackText = ta.getBoolean(R.styleable.TitleLayout_nbHasBackText, true);
            hasTitleText = ta.getBoolean(R.styleable.TitleLayout_nbHasTitleText, true);
            hasEditText = ta.getBoolean(R.styleable.TitleLayout_nbHasEditText, true);
            backString = ta.getString(R.styleable.TitleLayout_nbBackText);
            titleString = ta.getString(R.styleable.TitleLayout_nbTitleText);
            editString = ta.getString(R.styleable.TitleLayout_nbEditText);
        }

        back = (MaterialIconView)findViewById(R.id.back);
        backText = (TextView)findViewById(R.id.back_text);
        title = (TextView)findViewById(R.id.title_text);
        edit = (TextView)findViewById(R.id.edit_text);

        backText.setText(backString);
        title.setText(titleString);
        edit.setText(editString);

        if ("".equals(backString) || !hasBackText) backText.setVisibility(GONE);
        if ("".equals(editString) || !hasEditText) edit.setVisibility(GONE);
        if (!hasTitleText) title.setVisibility(GONE);

        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.clickTitleBack();
            }
        });

        title.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                if (mListener != null) mListener.doubleClickTitle();
            }
        });

        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.clickTitleEdit();
            }
        });
    }

    public void setTitle(String titleString) {
        title.setText(titleString);
    }

    public void setEdit(String editString) {
        edit.setText(editString);
    }

    public void setBack(String backString) {
        backText.setText(backString);
    }

    public void setOnTitleActionListener(OnTitleActionListener listener) {
        mListener = listener;
    }

    public interface OnTitleActionListener {
        void clickTitleBack();
        void doubleClickTitle();
        void clickTitleEdit();
    }
}
