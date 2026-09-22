package com.householdfinance.finance.adapter.in.rest;

import static com.householdfinance.finance.adapter.in.rest.FinanceDtos.*;

import com.householdfinance.finance.application.port.in.*;
import com.householdfinance.finance.domain.*;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class FinanceController {
    private final HouseholdUseCase households;
    private final AccountUseCase accounts;
    private final TransactionUseCase transactions;
    private final BillUseCase bills;
    private final BudgetUseCase budgets;
    private final CategoryUseCase categories;
    private final ReportUseCase reports;

    FinanceController(HouseholdUseCase households, AccountUseCase accounts, TransactionUseCase transactions,
                      BillUseCase bills, BudgetUseCase budgets, CategoryUseCase categories, ReportUseCase reports) {
        this.households = households;
        this.accounts = accounts;
        this.transactions = transactions;
        this.bills = bills;
        this.budgets = budgets;
        this.categories = categories;
        this.reports = reports;
    }

    private UUID user(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @PostMapping("/households")
    ResponseEntity<HouseholdResponse> createHousehold(Authentication authentication,
                                                       @Valid @RequestBody HouseholdRequest request) {
        var household = households.create(user(authentication), request.name());
        return ResponseEntity.status(201).body(new HouseholdResponse(household.id().toString(), household.name()));
    }

    @GetMapping("/households/{id}")
    HouseholdResponse getHousehold(Authentication authentication, @PathVariable UUID id) {
        var household = households.get(user(authentication), new HouseholdId(id));
        return new HouseholdResponse(household.id().toString(), household.name());
    }

    @PostMapping("/households/{id}/accounts")
    ResponseEntity<AccountResponse> createAccount(Authentication authentication, @PathVariable UUID id,
                                                   @Valid @RequestBody AccountRequest request) {
        var account = accounts.open(user(authentication), new HouseholdId(id),
                request.name(), AccountType.valueOf(request.type().toUpperCase()),
                request.openingBalance().domain());
        return ResponseEntity.status(201).body(account(account));
    }

    @GetMapping("/households/{id}/accounts")
    List<AccountResponse> listAccounts(Authentication authentication, @PathVariable UUID id) {
        return accounts.list(user(authentication), new HouseholdId(id)).stream().map(this::account).toList();
    }

    @GetMapping("/households/{id}/categories")
    List<CategoryResponse> listCategories(Authentication authentication, @PathVariable UUID id) {
        return categories.list(user(authentication), new HouseholdId(id)).stream().map(this::category).toList();
    }

    @PostMapping("/households/{id}/transactions")
    ResponseEntity<TransactionResponse> createTransaction(Authentication authentication, @PathVariable UUID id,
                                                           @Valid @RequestBody TransactionRequest request) {
        var transaction = transactions.record(user(authentication), new HouseholdId(id),
                new AccountId(request.accountId()), request.categoryId(),
                TransactionType.valueOf(request.type().toUpperCase()), request.amount().domain(),
                Instant.now(), request.description());
        return ResponseEntity.status(201).body(transaction(transaction));
    }

    @GetMapping("/households/{id}/transactions")
    List<TransactionResponse> listTransactions(Authentication authentication, @PathVariable UUID id) {
        return transactions.list(user(authentication), new HouseholdId(id)).stream()
                .map(this::transaction).toList();
    }

    @PostMapping("/households/{id}/transfers")
    ResponseEntity<TransactionResponse> transfer(Authentication authentication, @PathVariable UUID id,
                                                  @Valid @RequestBody TransferRequest request) {
        var transaction = transactions.transfer(user(authentication), new HouseholdId(id),
                new AccountId(request.fromAccountId()), new AccountId(request.toAccountId()),
                request.amount().domain(), Instant.now(), request.description());
        return ResponseEntity.status(201).body(transaction(transaction));
    }

    @PostMapping("/households/{id}/bills")
    ResponseEntity<BillResponse> createBill(Authentication authentication, @PathVariable UUID id,
                                             @Valid @RequestBody BillRequest request) {
        var bill = bills.create(user(authentication), new HouseholdId(id), request.name(),
                request.amount().domain(), request.dueDate());
        return ResponseEntity.status(201).body(bill(bill));
    }

    @GetMapping("/households/{id}/bills")
    List<BillResponse> listBills(Authentication authentication, @PathVariable UUID id) {
        return bills.list(user(authentication), new HouseholdId(id)).stream().map(this::bill).toList();
    }

    @PostMapping("/households/{id}/bills/{billId}/occurrences")
    ResponseEntity<OccurrenceResponse> createOccurrence(Authentication authentication, @PathVariable UUID billId,
                                                         @Valid @RequestBody OccurrenceRequest request) {
        var occurrence = bills.occurrence(user(authentication), new BillId(billId), request.dueDate());
        return ResponseEntity.status(201).body(occurrence(occurrence));
    }

    @PostMapping("/households/{id}/bill-occurrences/{occurrenceId}/payments")
    OccurrenceResponse payOccurrence(Authentication authentication, @PathVariable UUID occurrenceId,
                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                     @Valid @RequestBody PaymentRequest request) {
        return occurrence(bills.pay(user(authentication), new OccurrenceId(occurrenceId),
                new AccountId(request.accountId()), request.description(), idempotencyKey));
    }

    @PostMapping("/households/{id}/budgets")
    ResponseEntity<BudgetResponse> createBudget(Authentication authentication, @PathVariable UUID id,
                                                 @Valid @RequestBody BudgetRequest request) {
        var budget = budgets.create(user(authentication), new HouseholdId(id),
                new CategoryId(request.categoryId()), request.limit().domain(),
                BudgetPeriod.valueOf(request.period().toUpperCase()),
                request.startDate(), request.endDate());
        return ResponseEntity.status(201).body(budget(budget));
    }

    @GetMapping("/households/{id}/reports/summary")
    ReportUseCase.Report summary(Authentication authentication, @PathVariable UUID id,
                                 @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return reports.summary(user(authentication), new HouseholdId(id), from, to);
    }

    private AccountResponse account(FinancialAccount account) {
        return new AccountResponse(account.id().toString(), account.name(), account.type().name(),
                new MoneyDto(account.balance().amount(), account.currency().getCurrencyCode()));
    }

    private TransactionResponse transaction(Transaction transaction) {
        return new TransactionResponse(transaction.id().toString(), transaction.accountId().toString(),
                transaction.type().name(), new MoneyDto(transaction.amount().amount(),
                transaction.amount().currency().getCurrencyCode()), transaction.description(),
                transaction.occurredAt());
    }

    private CategoryResponse category(Category category) {
        return new CategoryResponse(category.id().toString(), category.name());
    }

    private BillResponse bill(HouseholdBill bill) {
        return new BillResponse(bill.id().toString(), bill.name(),
                new MoneyDto(bill.amount().amount(), bill.amount().currency().getCurrencyCode()),
                bill.dueDate().toString());
    }

    private OccurrenceResponse occurrence(BillOccurrence occurrence) {
        return new OccurrenceResponse(occurrence.id().toString(), occurrence.billId().toString(),
                occurrence.dueDate().toString(), occurrence.status().name(),
                occurrence.paidBy() == null ? null : occurrence.paidBy().toString());
    }

    private BudgetResponse budget(Budget budget) {
        return new BudgetResponse(budget.id().toString(), budget.categoryId().toString(),
                new MoneyDto(budget.limit().amount(), budget.limit().currency().getCurrencyCode()),
                new MoneyDto(budget.consumed().amount(), budget.consumed().currency().getCurrencyCode()));
    }
}
