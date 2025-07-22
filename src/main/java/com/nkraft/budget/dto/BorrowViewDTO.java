package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowViewDTO {
    private BorrowSummaryDTO borrow;
    private BigDecimal repaymentRate;
    private int repaymentRatePercent;
    private boolean isCompleted;
}