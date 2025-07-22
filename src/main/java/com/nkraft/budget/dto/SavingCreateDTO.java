package com.nkraft.budget.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavingCreateDTO {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate savingDate;
    private BigDecimal savingAmount;
    private Long sourceAccountId;
    private String memo;
}