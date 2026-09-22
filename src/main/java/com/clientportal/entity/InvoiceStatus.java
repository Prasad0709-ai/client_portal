package com.clientportal.entity;

public enum InvoiceStatus {
    UNPAID("Unpaid", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    PAID("Paid", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    OVERDUE("Overdue", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String displayName;
    private final String badgeClass;

    InvoiceStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
