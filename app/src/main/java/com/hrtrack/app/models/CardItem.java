package com.hrtrack.app.models;

public class CardItem {
    private final int iconRes;
    private final String title;
    private final String fragmentTag;

    public CardItem(int iconRes, String title, String fragmentTag) {
        this.iconRes = iconRes;
        this.title = title;
        this.fragmentTag = fragmentTag;
    }

    public int getIconRes() { return iconRes; }
    public String getTitle() { return title; }
    public String getFragmentTag() { return fragmentTag; }
}

