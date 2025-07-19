package com.nkraft.budget.dto;

import com.nkraft.budget.entity.RecurringTransactionRuleType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecurringTransactionJsDTO {
    private Long id;
    private String name;
    private Long accountId;
    private Long budgetTransactionTypeId;
    private Long categoryId;
    private BigDecimal amount;
    private String memo;
    private RecurringTransactionRuleType ruleType;
    private Integer dayOfMonth;
    private Integer dayOfWeek;
}