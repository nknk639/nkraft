package com.nkraft.budget.entity.converter;

import com.nkraft.budget.entity.TransactionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * TransactionStatus enum と DBの '予定'/'完了' 文字列を相互変換するJPAコンバータ。
 * autoApply=true により、全てのTransactionStatus型のエンティティ属性に自動的に適用されます。
 */
@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, String> {

    /**
     * EnumからDBの文字列へ変換します。
     */
    @Override
    public String convertToDatabaseColumn(TransactionStatus attribute) {
        // attributeがnullの場合はDBにもNULLを保存
        return attribute == null ? null : attribute.getDbValue();
    }

    /**
     * DBの文字列からEnumへ変換します。
     */
    @Override
    public TransactionStatus convertToEntityAttribute(String dbData) {
        // dbDataがnullの場合はEntityの属性もnullにする
        return dbData == null ? null : TransactionStatus.fromDbValue(dbData);
    }
}