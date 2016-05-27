package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MessageFragment extends Fragment {

    private TextView orderNumberTV;
    private TextView queueNumberTV;
    private TextView serviceNumberTV;

    private ViewPager viewPager;
    private SmartTabLayout smartTabLayout;

    private AppCompatActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            activity = (AppCompatActivity)context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View messageLayout = inflater.inflate(R.layout.fragment_message, container, false);

        orderNumberTV = (TextView)messageLayout.findViewById(R.id.order_num);
        queueNumberTV = (TextView)messageLayout.findViewById(R.id.queue_num);
        serviceNumberTV = (TextView)messageLayout.findViewById(R.id.service_num);

        viewPager = (ViewPager)messageLayout.findViewById(R.id.viewpager);
        smartTabLayout = (SmartTabLayout)messageLayout.findViewById(R.id.viewpagertab);

        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        pages.add(FragmentPagerItem.of("订单", MessageBookFragment.class));
        pages.add(FragmentPagerItem.of("排位", MessageQueueFragment.class));
        pages.add(FragmentPagerItem.of("服务", MessageServiceFragment.class));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(activity.getSupportFragmentManager(), pages);

        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(adapter);
//        smartTabLayout.setViewPager(viewPager);

        setNumber();

        return messageLayout;
    }

    public void setNumber() {
        if (serviceNumberTV == null || orderNumberTV == null) return;
        if (BraecoWaiterApplication.serve.size() == 0) {
            serviceNumberTV.setVisibility(View.GONE);
        } else {
            serviceNumberTV.setVisibility(View.VISIBLE);
            serviceNumberTV.setText(String.valueOf(BraecoWaiterApplication.serve.size()));
        }
        if (BraecoWaiterApplication.allOrder.size() == 0) {
            orderNumberTV.setVisibility(View.GONE);
        } else {
            orderNumberTV.setVisibility(View.VISIBLE);
            orderNumberTV.setText(String.valueOf(BraecoWaiterApplication.allOrder.size()));
        }
    }

    // tell main activity to change the number of message
    public interface OnMessageNumberChangeListener {
        void updateMessageNumber(int size);
    }

    private void showToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }

}
