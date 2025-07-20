package com.nkraft.budget.dto;

import com.nkraft.budget.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class ReportDTO {
    private YearMonth targetMonth;
    private YearMonth prevMonth;
    private YearMonth nextMonth;
    private boolean isOldestMonth;

    private BigDecimal totalPlanned;
    private BigDecimal totalActual;
    private BigDecimal totalDifference;

    private List<CategoryReportDTO> categoryReports;
    private List<Transaction> transactions;
}