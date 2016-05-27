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

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

/**
 * Created by Weiping on 2015/12/1.
 */

public class ServiceFragment extends Fragment {

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

        View messageLayout = inflater.inflate(R.layout.fragment_service, container, false);

        viewPager = (ViewPager)messageLayout.findViewById(R.id.viewpager);
        smartTabLayout = (SmartTabLayout)messageLayout.findViewById(R.id.viewpagertab);

        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        pages.add(FragmentPagerItem.of("辅助点单", ServiceMenuFragment.class));
        pages.add(FragmentPagerItem.of("会员充值", ServiceVipFragment.class));
        pages.add(FragmentPagerItem.of("流水订单", ServiceRecordFragment.class));

        BraecoWaiterFragmentPagerItemAdapter adapter
                = new BraecoWaiterFragmentPagerItemAdapter(activity.getSupportFragmentManager(), pages);

        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(adapter);
//        smartTabLayout.setViewPager(viewPager);

        return messageLayout;
    }

}
