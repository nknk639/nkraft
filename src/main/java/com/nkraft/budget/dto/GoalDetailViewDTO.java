package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDetailViewDTO {
    private GoalViewDTO goal;
    private List<TransactionDTO> savingTransactions;
    private BigDecimal mainAccountBalance;
    private List<TransactionDTO> mainAccountPlannedTransactions;
}