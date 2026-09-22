package com.householdfinance.finance.adapter.in.rest;

import com.householdfinance.finance.domain.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class FinanceDtos {
    private FinanceDtos() {}

    public record MoneyDto(@NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String currency) {
        Money domain() { return Money.of(amount, currency); }
    }
    public record HouseholdRequest(@NotBlank String name) {}
    public record AccountRequest(@NotBlank String name, @NotBlank String type, @NotNull MoneyDto openingBalance) {}
    public record TransactionRequest(@NotNull UUID accountId, UUID categoryId, @NotBlank String type,
                                     @NotNull MoneyDto amount, String description) {}
    public record TransferRequest(@NotNull UUID fromAccountId, @NotNull UUID toAccountId,
                                  @NotNull MoneyDto amount, String description) {}
    public record CategoryRequest(@NotBlank String name) {}
    public record BillRequest(@NotBlank String name, @NotNull MoneyDto amount, @NotNull LocalDate dueDate) {}
    public record OccurrenceRequest(@NotNull LocalDate dueDate) {}
    public record PaymentRequest(@NotNull UUID accountId, String description) {}
    public record BudgetRequest(@NotNull UUID categoryId, @NotNull MoneyDto limit, @NotBlank String period,
                                @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
    public record HouseholdResponse(String id, String name) {}
    public record AccountResponse(String id, String name, String type, MoneyDto balance) {}
    public record TransactionResponse(String id, String accountId, String type, MoneyDto amount,
                                      String description, Instant occurredAt) {}
    public record CategoryResponse(String id, String name) {}
    public record BillResponse(String id, String name, MoneyDto amount, String dueDate) {}
    public record OccurrenceResponse(String id, String billId, String dueDate, String status, String paidBy) {}
    public record BudgetResponse(String id, String categoryId, MoneyDto limit, MoneyDto consumed) {}
}
