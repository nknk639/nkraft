package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowDetailViewDTO {
    private BorrowViewDTO borrow;
    private List<TransactionDTO> repaymentTransactions;
    private BigDecimal mainAccountBalance;
    private List<TransactionDTO> mainAccountPlannedTransactions;
}