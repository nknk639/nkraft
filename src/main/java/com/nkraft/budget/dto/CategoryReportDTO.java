package com.nkraft.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryReportDTO {
    private String categoryName;
    private BigDecimal totalPlanned;
    private BigDecimal totalActual;
}