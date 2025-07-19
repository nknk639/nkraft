package com.nkraft.budget.dto;

import com.nkraft.budget.entity.RecurringTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionViewDTO {
    private RecurringTransaction recurringTransaction;
    private LocalDate latestTransactionDate;
}