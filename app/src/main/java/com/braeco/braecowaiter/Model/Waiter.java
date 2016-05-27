package com.braeco.braecowaiter.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.braeco.braecowaiter.BraecoWaiterApplication;

/**
 * Created by Weiping on 2016/5/14.
 */

@SuppressLint("CommitPrefEdits")
public class Waiter {

    public static final String PREFERENCES_NAME = "VALUE";

    public static final String DEFAULT_NICK_NAME = "";
    public static final String DEFAULT_SID = "";
    public static final String DEFAULT_LAST_USE_VERSION = "";
    public static final long DEFAULT_AUTHORITY = -1;

    private SharedPreferences mSharedPreferences = null;

    private String nickName = DEFAULT_NICK_NAME;
    private String sid = DEFAULT_SID;
    private String lastUseVersion = DEFAULT_LAST_USE_VERSION;
    private long authority = DEFAULT_AUTHORITY;

    private boolean useMemberBalance = false;

    public String getNickName() {
        nickName = getSharedPreferences().getString("USER_NAME", DEFAULT_NICK_NAME);
        return nickName;
    }

    public void setNickName(String nickName) {
        BraecoWaiterApplication.getAppContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString("USER_NAME", nickName)
                .commit();
        this.nickName = nickName;
    }

    public void cleanNickName() {
        setNickName(DEFAULT_NICK_NAME);
    }

    public String getSid() {
        sid = getSharedPreferences().getString("SID", DEFAULT_SID);
        return sid;
    }

    public void setSid(String sid) {
        BraecoWaiterApplication.getAppContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString("SID", sid)
                .commit();
        this.sid = sid;
    }

    public void cleanSid() {
        setSid(DEFAULT_SID);
    }

    public String getLastUseVersion() {
        lastUseVersion = getSharedPreferences().getString("LAST_USE_VERSION", DEFAULT_LAST_USE_VERSION);
        return lastUseVersion;
    }

    public void setLastUseVersion(String lastUseVersion) {
        BraecoWaiterApplication.getAppContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString("LAST_USE_VERSION", lastUseVersion)
                .commit();
        this.lastUseVersion = lastUseVersion;
    }

    public void cleanLastUseVersion() {
        setLastUseVersion(DEFAULT_LAST_USE_VERSION);
    }

    public long getAuthority() {
        authority = getSharedPreferences().getLong("AUTHORITY", DEFAULT_AUTHORITY);
        return authority;
    }

    public void setAuthority(long authority) {
        BraecoWaiterApplication.getAppContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putLong("AUTHORITY", authority)
                .commit();
        this.authority = authority;
    }

    public void cleanAuthority() {
        setAuthority(DEFAULT_AUTHORITY);
    }

    public boolean isUseMemberBalance() {
        useMemberBalance = getSharedPreferences().getBoolean("USE_MEMBER_BALANCE", false);
        return useMemberBalance;
    }

    public void setUseMemberBalance(boolean useMemberBalance) {
        BraecoWaiterApplication.getAppContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean("USE_MEMBER_BALANCE", useMemberBalance)
                .commit();
        this.useMemberBalance = useMemberBalance;
    }

    private SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) mSharedPreferences = BraecoWaiterApplication.getAppContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return mSharedPreferences;
    }

    private static Waiter ourInstance = new Waiter();

    public static Waiter getInstance() {
        return ourInstance;
    }

    private Waiter() {
    }
}
