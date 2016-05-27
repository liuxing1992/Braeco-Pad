package com.braeco.braecowaiter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

public class MeFragmentSettingsRemind extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private LinearLayout back;
    private LinearLayout notification;
    private LinearLayout sound;
    private LinearLayout vibrate;

    private AnimCheckBox notificationCheck;
    private AnimCheckBox soundCheck;
    private AnimCheckBox vibrateCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_settings_remind);

        notification = (LinearLayout)findViewById(R.id.notification);
        sound = (LinearLayout)findViewById(R.id.sound);
        vibrate = (LinearLayout)findViewById(R.id.vibrate);
        back = (LinearLayout)findViewById(R.id.back);

        notificationCheck = (AnimCheckBox)findViewById(R.id.notification_check);
        soundCheck = (AnimCheckBox)findViewById(R.id.sound_check);
        vibrateCheck = (AnimCheckBox)findViewById(R.id.vibrate_check);

        notificationCheck.setChecked(BraecoWaiterApplication.settingsNotification);
        soundCheck.setChecked(BraecoWaiterApplication.settingsSound);
        vibrateCheck.setChecked(BraecoWaiterApplication.settingsVibrate);
        if (notificationCheck.isChecked()) {
            sound.setEnabled(true);
            soundCheck.setEnabled(true);
            vibrate.setEnabled(true);
            vibrateCheck.setEnabled(true);
        } else {
            sound.setEnabled(false);
            soundCheck.setEnabled(false);
            vibrate.setEnabled(false);
            vibrateCheck.setEnabled(false);
        }

        notification.setOnClickListener(this);
        notificationCheck.setOnClickListener(this);
        sound.setOnClickListener(this);
        soundCheck.setOnClickListener(this);
        vibrate.setOnClickListener(this);
        vibrateCheck.setOnClickListener(this);
        back.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = getSharedPreferences("VALUE", MODE_PRIVATE).edit();
        switch (v.getId()) {
            case R.id.notification:
            case R.id.notification_check:
                if (notificationCheck.isChecked()) {
                    notificationCheck.setChecked(false);
                    BraecoWaiterApplication.settingsNotification = false;
                    sound.setEnabled(false);
                    soundCheck.setEnabled(false);
                    vibrate.setEnabled(false);
                    vibrateCheck.setEnabled(false);
                } else {
                    notificationCheck.setChecked(true);
                    BraecoWaiterApplication.settingsNotification = true;
                    sound.setEnabled(true);
                    soundCheck.setEnabled(true);
                    vibrate.setEnabled(true);
                    vibrateCheck.setEnabled(true);
                }
                editor.putBoolean("SETTINGS_NOTIFICATION", BraecoWaiterApplication.settingsNotification);
                break;
            case R.id.sound:
            case R.id.sound_check:
                if (soundCheck.isChecked()) {
                    soundCheck.setChecked(false);
                    BraecoWaiterApplication.settingsSound = false;
                } else {
                    soundCheck.setChecked(true);
                    BraecoWaiterApplication.settingsSound = true;
                }
                editor.putBoolean("SETTINGS_SOUND", BraecoWaiterApplication.settingsSound);
                break;
            case R.id.vibrate:
            case R.id.vibrate_check:
                if (vibrateCheck.isChecked()) {
                    vibrateCheck.setChecked(false);
                    BraecoWaiterApplication.settingsVibrate = false;
                } else {
                    vibrateCheck.setChecked(true);
                    BraecoWaiterApplication.settingsVibrate = true;
                }
                editor.putBoolean("SETTINGS_VIBRATE", BraecoWaiterApplication.settingsVibrate);
                break;
            case R.id.back:
                finish();
                break;
        }
        editor.commit();
    }
}
