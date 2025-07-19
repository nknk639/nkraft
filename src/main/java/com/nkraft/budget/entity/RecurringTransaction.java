package com.nkraft.budget.entity;

import java.math.BigDecimal;

import com.nkraft.user.entity.NkraftUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 繰り返し予定を管理するエンティティ。
 * 要件定義書 4. データモデルに基づいています。
 */
@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "account", "budgetTransactionType", "category"})
@EqualsAndHashCode(of = "id")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurring_transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nkraft_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recurring_transactions_nkraft_users"))
    private NkraftUser user;

    @Column(name = "recurring_transaction_name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recurring_transactions_accounts"))
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_transaction_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recurring_transactions_budget_transaction_types"))
    private BudgetTransactionType budgetTransactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_recurring_transactions_categories"))
    private Category category;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "memo")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, columnDefinition = "ENUM('毎月', '毎週')")
    private RecurringTransactionRuleType ruleType;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}