package com.householdfinance.finance.domain;

public enum MembershipStatus {
    ACTIVE,
    INVITED,
    REMOVED,

    /**
     * Deprecated legacy value retained for compatibility with older persisted data.
     */
    INACTIVE
}
