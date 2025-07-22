package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalSummaryDTO {
    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
}