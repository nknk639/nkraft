package com.nkraft.budget.dto;

import com.nkraft.budget.entity.TransactionStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class TransactionSearchForm {
    private String keyword;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    private List<Long> accountIds;
    private List<Long> categoryIds;
    private List<TransactionStatus> statuses;
}