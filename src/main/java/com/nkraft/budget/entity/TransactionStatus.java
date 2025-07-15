package com.nkraft.budget.entity;

public enum TransactionStatus {
    // 要件定義書のENUM('予定', '完了')に対応
    // DBスキーマは 'PLANNED', 'COMPLETED' で管理することを想定
    PLANNED,
    COMPLETED
}