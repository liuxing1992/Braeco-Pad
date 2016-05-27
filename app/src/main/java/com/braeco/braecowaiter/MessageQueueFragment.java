package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Weiping on 2015/12/1.
 */

public class MessageQueueFragment extends Fragment {

    private Activity activity;

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
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MessageQueueFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_message_queue, container, false);

        return messageLayout;
    }

}
