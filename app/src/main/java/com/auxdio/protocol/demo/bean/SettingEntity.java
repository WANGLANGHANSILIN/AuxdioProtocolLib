package com.auxdio.protocol.demo.bean;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class SettingEntity {
    private String itemName;
    private boolean isSeletor;

    public SettingEntity(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isSeletor() {
        return isSeletor;
    }

    public void setSeletor(boolean seletor) {
        isSeletor = seletor;
    }

    @Override
    public String toString() {
        return "SettingEntity{" +
                "itemName='" + itemName + '\'' +
                '}';
    }
}
