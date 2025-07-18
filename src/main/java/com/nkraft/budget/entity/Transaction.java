package com.nkraft.budget.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nkraft.user.entity.NkraftUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "account", "budgetTransactionType", "category"})
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nkraft_user_id", nullable = false)
    private NkraftUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_transaction_type_id", nullable = false)
    private BudgetTransactionType budgetTransactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "planned_amount", nullable = false)
    private BigDecimal plannedAmount;

    @Column(name = "actual_amount")
    private BigDecimal actualAmount;

    @Column(name = "transaction_status", nullable = false, columnDefinition = "ENUM('予定', '完了')")
    private TransactionStatus transactionStatus;

    @Column(name = "memo")
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transactions_recurring_transactions"))
    private RecurringTransaction recurringTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transactions_borrows"))
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_transactions_goals"))
    private Goal goal;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}