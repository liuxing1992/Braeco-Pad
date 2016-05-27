package com.braeco.braecowaiter.Model;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.braeco.braecowaiter.Model.ActivityCollector;

/**
 * Created by Weiping on 2016/5/20.
 */

public class BraecoAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.finish(this);
    }

}
