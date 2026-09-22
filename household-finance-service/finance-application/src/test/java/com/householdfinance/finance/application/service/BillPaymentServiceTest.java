package com.householdfinance.finance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.householdfinance.finance.application.port.in.BillUseCase;
import com.householdfinance.finance.application.port.out.FinanceRepository;
import com.householdfinance.finance.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BillPaymentServiceTest {

    @Test
    void paysOccurrenceByCreatingBillPaymentAndUpdatingAccount() {
        var repository = mock(FinanceRepository.class);
        var userId = UUID.randomUUID();
        var household = Household.create("Home", userId, Instant.now());
        var bill = HouseholdBill.create(household.id(), "Electricity",
                Money.of(new BigDecimal("80"), "USD"), LocalDate.now());
        var occurrence = BillOccurrence.create(bill.id(), LocalDate.now().plusDays(1));
        var account = FinancialAccount.open(household.id(), "Checking", AccountType.BANK,
                Money.of(new BigDecimal("100"), "USD"), Instant.now());

        when(repository.occurrence(occurrence.id())).thenReturn(java.util.Optional.of(occurrence));
        when(repository.bill(bill.id())).thenReturn(java.util.Optional.of(bill));
        when(repository.household(household.id())).thenReturn(java.util.Optional.of(household));
        when(repository.lockAccount(account.id())).thenReturn(java.util.Optional.of(account));
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(BillOccurrence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BillUseCase service = ServiceFactory.bill(repository);
        var paid = service.pay(userId, occurrence.id(), account.id(), "Electricity payment");

        assertThat(paid.status()).isEqualTo(OccurrenceStatus.PAID);
        assertThat(paid.paidBy()).isNotNull();
        assertThat(account.balance().amount()).isEqualByComparingTo("20");
        verify(repository).save(account);
        var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().type()).isEqualTo(TransactionType.BILL_PAYMENT);
        assertThat(transactionCaptor.getValue().amount().amount()).isEqualByComparingTo("80");
        verify(repository).save(occurrence);
    }

    @Test
    void rejectsPaymentWhenAccountHasInsufficientBalanceWithoutPersistingChanges() {
        var repository = mock(FinanceRepository.class);
        var userId = UUID.randomUUID();
        var household = Household.create("Home", userId, Instant.now());
        var bill = HouseholdBill.create(household.id(), "Electricity",
                Money.of(new BigDecimal("80"), "USD"), LocalDate.now());
        var occurrence = BillOccurrence.create(bill.id(), LocalDate.now().plusDays(1));
        var account = FinancialAccount.open(household.id(), "Checking", AccountType.BANK,
                Money.of(new BigDecimal("20"), "USD"), Instant.now());

        when(repository.occurrence(occurrence.id())).thenReturn(java.util.Optional.of(occurrence));
        when(repository.bill(bill.id())).thenReturn(java.util.Optional.of(bill));
        when(repository.household(household.id())).thenReturn(java.util.Optional.of(household));
        when(repository.lockAccount(account.id())).thenReturn(java.util.Optional.of(account));

        BillUseCase service = ServiceFactory.bill(repository);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> service.pay(userId, occurrence.id(), account.id(), null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient account balance");

        assertThat(occurrence.status()).isEqualTo(OccurrenceStatus.PENDING);
        assertThat(account.balance().amount()).isEqualByComparingTo("20");
        verify(repository, never()).save(any(Transaction.class));
        verify(repository, never()).save(any(BillOccurrence.class));
    }
}
