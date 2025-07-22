package com.nkraft.budget.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BorrowCreateDTO {
    private String borrowName;
    private BigDecimal totalAmount;
    private Long depositAccountId;
}