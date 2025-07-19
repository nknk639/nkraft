package com.nkraft.budget.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TransactionDateUpdateDTO {
    private Long transactionId;
    private LocalDate newDate;
}