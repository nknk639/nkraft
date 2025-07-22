package com.nkraft.budget.dto;

import lombok.Data;

@Data
public class AccountUpdateDTO {
    private String accountName;
    private boolean isMain;
    private boolean isSavings;
}