package com.householdfinance.finance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.householdfinance.finance.application.port.in.TransactionUseCase;
import com.householdfinance.finance.application.port.out.FinanceRepository;
import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import com.householdfinance.finance.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdempotentTransactionServiceTest {

    @Test
    void completesNewIncomeAndReturnsCreatedTransaction() {
        var delegate = mock(TransactionUseCase.class);
        var finance = mock(FinanceRepository.class);
        var idempotency = mock(IdempotencyRepository.class);
        var householdId = new HouseholdId(UUID.randomUUID());
        var accountId = new AccountId(UUID.randomUUID());
        var userId = UUID.randomUUID();
        var transaction = Transaction.record(householdId, accountId, null, TransactionType.INCOME,
                Money.of(new BigDecimal("100"), "USD"), Instant.now(), "Salary");
        when(idempotency.reserve(eq(userId), eq("TRANSACTION_INCOME"), eq("income-key"), any()))
                .thenAnswer(invocation -> record(userId,
                        IdempotencyRepository.IdempotencyRecord.Status.RESERVED, null,
                        invocation.getArgument(3)));
        when(delegate.record(eq(userId), eq(householdId), eq(accountId), isNull(), eq(TransactionType.INCOME),
                any(), any(), eq("Salary"))).thenReturn(transaction);

        var result = service(delegate, finance, idempotency).record(userId, householdId, accountId, null,
                TransactionType.INCOME, transaction.amount(), transaction.occurredAt(), "Salary", "income-key");

        assertThat(result).isSameAs(transaction);
        verify(idempotency).complete(any(), eq(transaction.id().value()));
    }

    @Test
    void replaysCompletedTransactionWithoutCallingDelegate() {
        var delegate = mock(TransactionUseCase.class);
        var finance = mock(FinanceRepository.class);
        var idempotency = mock(IdempotencyRepository.class);
        var transactionId = UUID.randomUUID();
        var transaction = Transaction.rehydrate(transactionId, new HouseholdId(UUID.randomUUID()),
                new AccountId(UUID.randomUUID()), null, null, TransactionType.EXPENSE,
                Money.of(new BigDecimal("25"), "USD"), Instant.now(), "Groceries", false);
        var userId = UUID.randomUUID();
        when(idempotency.reserve(eq(userId), eq("TRANSACTION_EXPENSE"), eq("expense-key"), any()))
                .thenAnswer(invocation -> record(userId,
                        IdempotencyRepository.IdempotencyRecord.Status.COMPLETED, transactionId,
                        invocation.getArgument(3)));
        when(finance.transaction(new TransactionId(transactionId))).thenReturn(Optional.of(transaction));

        var result = service(delegate, finance, idempotency).record(userId, transaction.householdId(),
                transaction.accountId(), null, TransactionType.EXPENSE, transaction.amount(),
                transaction.occurredAt(), "Groceries", "expense-key");

        assertThat(result).isSameAs(transaction);
        verifyNoInteractions(delegate);
        verify(idempotency, never()).complete(any(), any());
    }

    @Test
    void rejectsDifferentRequestWithSameKey() {
        var delegate = mock(TransactionUseCase.class);
        var finance = mock(FinanceRepository.class);
        var idempotency = mock(IdempotencyRepository.class);
        var userId = UUID.randomUUID();
        when(idempotency.reserve(eq(userId), eq("TRANSFER"), eq("transfer-key"), any()))
                .thenReturn(record(userId, IdempotencyRepository.IdempotencyRecord.Status.PROCESSING, null,
                        "different-hash"));

        assertThatThrownBy(() -> service(delegate, finance, idempotency).transfer(userId,
                new HouseholdId(UUID.randomUUID()), new AccountId(UUID.randomUUID()),
                new AccountId(UUID.randomUUID()), Money.of(new BigDecimal("10"), "USD"),
                Instant.now(), "Savings", "transfer-key"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Idempotency-Key was already used with a different request");
        verifyNoInteractions(delegate);
    }

    @Test
    void rejectsBlankOrOversizedKey() {
        var service = service(mock(TransactionUseCase.class), mock(FinanceRepository.class),
                mock(IdempotencyRepository.class));
        var args = new Object[] {UUID.randomUUID(), new HouseholdId(UUID.randomUUID()),
                new AccountId(UUID.randomUUID()), null, TransactionType.INCOME,
                Money.of(BigDecimal.ONE, "USD"), Instant.now(), "Income"};

        assertThatThrownBy(() -> service.record((UUID) args[0], (HouseholdId) args[1],
                (AccountId) args[2], null, TransactionType.INCOME, (Money) args[5],
                (Instant) args[6], (String) args[7], " "))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.record((UUID) args[0], (HouseholdId) args[1],
                (AccountId) args[2], null, TransactionType.INCOME, (Money) args[5],
                (Instant) args[6], (String) args[7], "x".repeat(201)))
                .isInstanceOf(DomainException.class);
    }

    private IdempotentTransactionService service(TransactionUseCase delegate, FinanceRepository finance,
                                                  IdempotencyRepository idempotency) {
        return new IdempotentTransactionService(delegate, finance, idempotency);
    }

    private IdempotencyRepository.IdempotencyRecord record(UUID userId,
            IdempotencyRepository.IdempotencyRecord.Status status, UUID resourceId) {
        return record(userId, status, resourceId, null);
    }

    private IdempotencyRepository.IdempotencyRecord record(UUID userId,
            IdempotencyRepository.IdempotencyRecord.Status status, UUID resourceId, String hash) {
        return new IdempotencyRepository.IdempotencyRecord(UUID.randomUUID(), userId, "operation", "key",
                hash == null ? "" : hash, status, resourceId, Instant.now());
    }
}
