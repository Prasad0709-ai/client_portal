package com.clientportal.entity;

public enum ProjectStatus {
    PLANNING("Planning", "bg-sky-500/10 text-sky-400 border-sky-500/20"),
    IN_PROGRESS("In Progress", "bg-amber-500/10 text-amber-400 border-amber-500/20"),
    ON_HOLD("On Hold", "bg-purple-500/10 text-purple-400 border-purple-500/20"),
    COMPLETED("Completed", "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"),
    CANCELLED("Cancelled", "bg-rose-500/10 text-rose-400 border-rose-500/20");

    private final String displayName;
    private final String badgeClass;

    ProjectStatus(String displayName, String badgeClass) {
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
