package com.nkraft.budget.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RepaymentCreateDTO {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate repaymentDate;
    private BigDecimal repaymentAmount;
    private Long sourceAccountId;
    private String memo;
}