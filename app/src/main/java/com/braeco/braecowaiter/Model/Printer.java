package com.braeco.braecowaiter.Model;

import com.braeco.braecowaiter.BraecoWaiterUtils;

import java.util.HashSet;

/**
 * Created by Weiping on 2016/5/12.
 */
public class Printer {

    private long id = -1;
    private int width = -1;
    private int size = -1;
    private int page = -1;
    private String name;
    private String remark;
    private int offset = -1;
    private boolean separate;
    private HashSet<Integer> ban;
    private HashSet<Integer> banCategory;
    private HashSet<String> banTable;

    public Printer(long id, int width, int page, String name, String remark, HashSet<Integer> ban, HashSet<Integer> banCategory, HashSet<String> banTable, boolean separate) {
        this.id = id;
        this.width = width;
        this.page = page;
        this.name = name;
        this.remark = remark;
        this.ban = ban;
        this.banCategory = banCategory;
        this.banTable = banTable;
        this.separate = separate;
    }

    public Printer(long id, int width, int size, int page, String name, String remark, int offset, HashSet<Integer> ban, HashSet<Integer> banCategory, HashSet<String> banTable, boolean separate) {
        this.id = id;
        this.width = width;
        this.size = size;
        this.page = page;
        this.name = name;
        this.remark = remark;
        this.offset = offset;
        this.ban = ban;
        this.banCategory = banCategory;
        this.banTable = banTable;
        this.separate = separate;
    }

    public Printer() {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Printer newPrinter = new Printer();
        newPrinter.setId(getId());
        newPrinter.setWidth(getWidth());
        newPrinter.setSize(getSize());
        newPrinter.setPage(getPage());
        newPrinter.setName(getName());
        newPrinter.setRemark(getRemark());
        newPrinter.setOffset(getOffset());
        newPrinter.setBan((HashSet<Integer>) getBan().clone());
        newPrinter.setBanCategory((HashSet<Integer>) getBanCategory().clone());
        newPrinter.setBanTable((HashSet<String>) getBanTable().clone());
        newPrinter.setSeparate(isSeparate());
        return newPrinter;
    }

    @Override
    public boolean equals(Object o) {
        Printer anotherPrinter = (Printer) o;
        boolean sameRemark;
        if (getRemark() == null) sameRemark = anotherPrinter.getRemark() == null;
        else sameRemark = getRemark().equals(anotherPrinter.getRemark());
        return getId() == anotherPrinter.getId()
                && getWidth() == anotherPrinter.getWidth()
                && getSize() == anotherPrinter.getSize()
                && getPage() == anotherPrinter.getPage()
                && getName().equals(anotherPrinter.getName())
                && sameRemark
                && getOffset() == anotherPrinter.getOffset()
                && BraecoWaiterUtils.isSameHashSetForInteger(getBan(), anotherPrinter.getBan())
                && BraecoWaiterUtils.isSameHashSetForInteger(getBanCategory(), anotherPrinter.getBanCategory())
                && BraecoWaiterUtils.isSameHashSetForString(getBanTable(), anotherPrinter.getBanTable())
                && isSeparate() == anotherPrinter.isSeparate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isSeparate() {
        return separate;
    }

    public void setSeparate(boolean separate) {
        this.separate = separate;
    }

    public HashSet<Integer> getBan() {
        return ban;
    }

    public void setBan(HashSet<Integer> ban) {
        this.ban = ban;
    }

    public HashSet<Integer> getBanCategory() {
        return banCategory;
    }

    public void setBanCategory(HashSet<Integer> banCategory) {
        this.banCategory = banCategory;
    }

    public HashSet<String> getBanTable() {
        return banTable;
    }

    public void setBanTable(HashSet<String> banTable) {
        this.banTable = banTable;
    }
}
