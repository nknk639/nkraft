package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowSummaryDTO {
    private Long borrowId;
    private String borrowName;
    private BigDecimal totalAmount;
    private BigDecimal repaidAmount;
}