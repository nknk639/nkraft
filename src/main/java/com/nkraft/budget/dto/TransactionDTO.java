package com.nkraft.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * 取引情報をJavaScriptに渡すためのDTO.
 * `Transaction`エンティティから必要な情報のみを抽出します.
 */
@Data
public class TransactionDTO {

    private Long transactionId;
    private LocalDate transactionDate;
    private String memo;
    private String categoryName;
    private String budgetTransactionTypeName;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private String transactionStatus;
    
    public TransactionDTO(Long transactionId, LocalDate transactionDate, BigDecimal plannedAmount, String memo, String budgetTransactionTypeName, String categoryName) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.plannedAmount = plannedAmount;
        this.memo = memo;
        this.budgetTransactionTypeName = budgetTransactionTypeName;
        this.categoryName = categoryName;
    }
}