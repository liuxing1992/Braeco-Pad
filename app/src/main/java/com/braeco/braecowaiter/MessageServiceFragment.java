package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

/**
 * Created by Weiping on 2015/12/1.
 */
public class MessageServiceFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {

    private Activity activity;

    private SuperRecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            activity = (Activity)context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MessageServiceFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_message_service, container, false);

        recyclerView = (SuperRecyclerView) messageLayout.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);

        BraecoWaiterApplication.messageServiceFragmentRecyclerViewAdapter
                = new MessageServiceFragmentRecyclerViewAdapter();

        recyclerView.setAdapter(BraecoWaiterApplication.messageServiceFragmentRecyclerViewAdapter);

        return messageLayout;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Todo
                if (recyclerView != null)
                    recyclerView.getSwipeToRefresh().setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {

    }

}
