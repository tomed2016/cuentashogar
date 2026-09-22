package com.householdfinance.finance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.householdfinance.finance.application.port.in.BillUseCase;
import com.householdfinance.finance.application.port.out.FinanceRepository;
import com.householdfinance.finance.application.port.out.IdempotencyRepository;
import com.householdfinance.finance.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdempotentBillServiceTest {

    @Test
    void replaysCompletedPaymentWithoutCallingDelegate() {
        var delegate = mock(BillUseCase.class);
        var finance = mock(FinanceRepository.class);
        var idempotency = mock(IdempotencyRepository.class);
        var occurrence = occurrence();
        var userId = UUID.randomUUID();
        var record = new IdempotencyRepository.IdempotencyRecord(
                UUID.randomUUID(), userId, "BILL_PAYMENT", "payment-key",
                "hash", IdempotencyRepository.IdempotencyRecord.Status.COMPLETED,
                occurrence.id().value(), Instant.now());
        when(idempotency.reserve(any(), eq("BILL_PAYMENT"), eq("payment-key"), any()))
                .thenAnswer(invocation -> new IdempotencyRepository.IdempotencyRecord(
                        record.id(), record.userId(), record.operation(), record.key(),
                        invocation.getArgument(3), record.status(), record.resourceId(), record.createdAt()));
        when(finance.occurrence(occurrence.id())).thenReturn(Optional.of(occurrence));

        var result = new IdempotentBillService(delegate, finance, idempotency)
                .pay(userId, occurrence.id(), accountId(), "Electricity", "payment-key");

        assertThat(result).isSameAs(occurrence);
        verifyNoInteractions(delegate);
        verify(idempotency, never()).complete(any(), any());
    }

    @Test
    void rejectsSameKeyWhenRequestHashChanges() {
        var delegate = mock(BillUseCase.class);
        var finance = mock(FinanceRepository.class);
        var idempotency = mock(IdempotencyRepository.class);
        var record = new IdempotencyRepository.IdempotencyRecord(
                UUID.randomUUID(), UUID.randomUUID(), "BILL_PAYMENT", "payment-key",
                "different-hash", IdempotencyRepository.IdempotencyRecord.Status.PROCESSING,
                null, Instant.now());
        when(idempotency.reserve(any(), eq("BILL_PAYMENT"), eq("payment-key"), any())).thenReturn(record);

        assertThatThrownBy(() -> new IdempotentBillService(delegate, finance, idempotency)
                .pay(record.userId(), occurrence().id(), accountId(), "Electricity", "payment-key"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Idempotency-Key was already used with a different request");

        verifyNoInteractions(delegate);
    }

    private BillOccurrence occurrence() {
        var household = Household.create("Home", UUID.randomUUID(), Instant.now());
        var bill = HouseholdBill.create(household.id(), "Electricity",
                Money.of(new BigDecimal("80"), "USD"), LocalDate.now());
        return BillOccurrence.create(bill.id(), LocalDate.now().plusDays(1));
    }

    private AccountId accountId() {
        return new AccountId(UUID.randomUUID());
    }
}
