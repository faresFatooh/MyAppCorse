package com.hrtrack.app.models;

public class Module {
    private String title;
    private int iconRes; // معرف المورد الخاص بالأيقونة

    public Module(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }
}
