package com.braeco.braecowaiter.Model;

/**
 * Created by Weiping on 2016/5/14.
 */
public class Vip {

    private int id;
    private String phone;
    private String nickname;
    private String level;
    private int exp = 0;
    private double balance = 0;
    private long date;
    private int dinnerId;

    public Vip(int id, String phone, String nickname, String level, int exp, double balance, long date, int dinnerId) {
        this.id = id;
        this.phone = phone;
        this.nickname = nickname;
        this.level = level;
        this.exp = exp;
        this.balance = balance;
        this.date = date;
        this.dinnerId = dinnerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDinnerId() {
        return dinnerId;
    }

    public void setDinnerId(int dinnerId) {
        this.dinnerId = dinnerId;
    }
}
