package com.nkraft.budget.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountCreateDTO {
    private String accountName;
    private BigDecimal initialBalance;
    private boolean isMain;
    private boolean isSavings;
}