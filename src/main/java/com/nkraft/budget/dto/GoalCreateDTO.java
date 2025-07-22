package com.nkraft.budget.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalCreateDTO {
    private String goalName;
    private BigDecimal targetAmount;
}