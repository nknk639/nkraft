package com.nkraft.budget.entity;

public enum RecurringTransactionRuleType {
    // 要件定義書のENUM('毎月', '毎週')に対応
    // DBスキーマも同様に '毎月', '毎週' で管理することを想定
    // 必要であれば、DBとENUMで異なる値を定義し、Converterで変換する
    毎月,
    毎週
}