package com.householdfinance.finance.domain;

import java.util.Objects;
import java.util.UUID;

public final class HouseholdMember {
    private final UUID userId;
    private MemberRole role;
    private MembershipStatus status = MembershipStatus.ACTIVE;

    public HouseholdMember(UUID id, MemberRole r) {
        userId = Objects.requireNonNull(id);
        role = Objects.requireNonNull(r);
    }

    public static HouseholdMember rehydrate(UUID id, MemberRole role, MembershipStatus status) {
        var member = new HouseholdMember(id, role);
        member.status = status;
        return member;
    }

    public UUID userId() {
        return userId;
    }

    public MemberRole role() {
        return role;
    }

    public MembershipStatus status() {
        return status;
    }

    public boolean active() {
        return status == MembershipStatus.ACTIVE;
    }

    public boolean admin() {
        return active() && role.isAdmin();
    }

    public void deactivate() {
        // preserve legacy INACTIVE for backward compatibility
        status = MembershipStatus.INACTIVE;
    }

    public void promote() {
        role = MemberRole.ADMIN;
    }
}
