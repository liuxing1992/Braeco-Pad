package com.braeco.braecowaiter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braeco.braecowaiter.Interfaces.OnGet401AsyncTaskListener;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Model.Waiter;
import com.braeco.braecowaiter.Tasks.Get401AsyncTask;
import com.squareup.picasso.Picasso;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MeFragment extends Fragment implements View.OnClickListener {

    private LinearLayout shop;
    private LinearLayout data;
    private LinearLayout order;
    private LinearLayout vip;
    private LinearLayout menu;
    private LinearLayout activity;
    private LinearLayout settings;
    private LinearLayout test401;

    private TextView waiterName;
    private TextView shopName;

    private ImageView pigImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View me = inflater.inflate(R.layout.fragment_me, container, false);

        shop = (LinearLayout)me.findViewById(R.id.shop);
        data = (LinearLayout)me.findViewById(R.id.data);
        order = (LinearLayout)me.findViewById(R.id.order);
        vip = (LinearLayout)me.findViewById(R.id.vip);
        menu = (LinearLayout)me.findViewById(R.id.menu);
        activity = (LinearLayout)me.findViewById(R.id.activity);
        settings = (LinearLayout)me.findViewById(R.id.settings);
        test401 = (LinearLayout)me.findViewById(R.id.test_401);

        waiterName = (TextView)me.findViewById(R.id.waiter_name);
        shopName = (TextView)me.findViewById(R.id.shop_name);

        pigImage = (ImageView)me.findViewById(R.id.pig_image);
        Picasso.with(getActivity())
                .load(BraecoWaiterApplication.waiterLogo)
                .placeholder(R.drawable.empty_logo)
                .error(R.drawable.empty_logo)
                .into(pigImage);

        waiterName.setText("管理员 " + Waiter.getInstance().getNickName() + " " + BraecoWaiterApplication.phone);
        shopName.setText(BraecoWaiterApplication.shopName);

        shop.setOnClickListener(this);
        data.setOnClickListener(this);
        order.setOnClickListener(this);
        vip.setOnClickListener(this);
        menu.setOnClickListener(this);
        activity.setOnClickListener(this);
        settings.setOnClickListener(this);
        test401.setOnClickListener(this);

        return me;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shop:
                getActivity().startActivity(new Intent(getActivity(), MeFragmentShop.class));
                break;
            case R.id.data:
                if (AuthorityManager.ableTo(Authority.STATISTICS)) {
                    getActivity().startActivity(new Intent(getActivity(), MeFragmentData.class));
                } else {
                    AuthorityManager.showDialog(getContext(), "查看数据统计");
                }
                break;
            case R.id.order:
                if (AuthorityManager.ableTo(Authority.VIEW_RECORD)) {
                    getActivity().startActivity(new Intent(getActivity(), MeFragmentOrder.class));
                } else {
                    AuthorityManager.showDialog(getContext(), "查看流水订单");
                }
                break;
            case R.id.vip:
                getActivity().startActivity(new Intent(getActivity(), MeFragmentVip.class));
                break;
            case R.id.menu:
                getActivity().startActivity(new Intent(getActivity(), MeFragmentMenu.class));
                break;
            case R.id.activity:
                getActivity().startActivity(new Intent(getActivity(), MeFragmentActivity.class));
                break;
            case R.id.settings:
                getActivity().startActivity(new Intent(getActivity(), MeFragmentSettings.class));
                break;
            case R.id.test_401:
                new Get401AsyncTask(mOnGet401AsyncTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    private OnGet401AsyncTaskListener mOnGet401AsyncTaskListener = new OnGet401AsyncTaskListener() {
        @Override
        public void success() {

        }

        @Override
        public void fail(String message) {

        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(getContext());
        }
    };
}
