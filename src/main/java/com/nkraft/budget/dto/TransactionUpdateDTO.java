package com.nkraft.budget.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 取引更新のためのリクエストパラメータを格納するDTO（Data Transfer Object）。
 */
@Data
public class TransactionUpdateDTO {
    private Long transactionId;
    private String transactionDate;
    private BigDecimal plannedAmount;
    private Long budgetTransactionTypeId;
    private Long categoryId;
    private String memo;

    // TODO: 必要に応じてバリデーションルールを追加
    // 例: @NotNull(message = "取引日は必須です")

    // 以下はLombokのアノテーション@Dataで自動生成されるため、明示的な記述は不要
    // public Long getTransactionId() { ... }
    // public void setTransactionId(Long transactionId) { ... }
    // ... (他のフィールドに対するGetter/Setter)
}