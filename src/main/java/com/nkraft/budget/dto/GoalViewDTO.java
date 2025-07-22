package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalViewDTO {
    private GoalSummaryDTO goal;
    private BigDecimal progressRate;
    private int progressRatePercent;
    private boolean isCompleted;
}