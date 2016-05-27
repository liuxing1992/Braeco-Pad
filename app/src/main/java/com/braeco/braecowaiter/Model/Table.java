package com.braeco.braecowaiter.Model;

/**
 * Created by Weiping on 2016/5/14.
 */
public class Table {

    private String id;
    private boolean used = false;

    public Table() {
    }

    public Table(String id, boolean used) {
        this.id = id;
        this.used = used;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new Table(id, used);
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) return ((Table)o).getId() == null;
        else return id.equals(((Table)o).getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
