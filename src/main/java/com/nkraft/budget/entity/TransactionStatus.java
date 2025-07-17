package com.nkraft.budget.entity;

import java.util.stream.Stream;

/**
 * 取引の状態を表すEnum。
 * DB上の日本語文字列('予定', '完了')とJavaコード上のEnumをマッピングします。
 */
public enum TransactionStatus {
    PLANNED("予定"),
    COMPLETED("完了");

    private final String dbValue;

    TransactionStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return this.dbValue;
    }

    public static TransactionStatus fromDbValue(String dbValue) {
        return Stream.of(TransactionStatus.values())
                .filter(status -> status.getDbValue().equals(dbValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown database value: " + dbValue));
    }
}