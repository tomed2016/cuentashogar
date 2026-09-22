package com.householdfinance.finance.domain;

public enum MemberRole {
    ADMIN,
    MEMBER,

    // Compatibility constants expected by the specification
    HOUSEHOLD_ADMIN,
    HOUSEHOLD_MEMBER,
    HOUSEHOLD_VIEWER;

    public boolean isAdmin() {
        return this == ADMIN || this == HOUSEHOLD_ADMIN;
    }

    public boolean isMember() {
        return this == MEMBER || this == HOUSEHOLD_MEMBER;
    }
}
