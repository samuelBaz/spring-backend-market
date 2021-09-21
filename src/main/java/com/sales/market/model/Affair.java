package com.sales.market.model;

public enum Affair {
    INVENTARY_LOWER_BOUND("Notification: Inventory lower bound"),
    INVENTARY_UPPER_BOUND("Notification: Inventory upper bound");

    private String description;

    Affair(String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
