package com.nkraft.budget.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.nkraft.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 口座マスタエンティティ
 */
@Entity
@Table(name = "accounts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    /**
     * 口座ID (主キー)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    /**
     * ユーザー (外部キー)
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 口座名
     */
    @Column(name = "account_name", nullable = false)
    private String accountName;

    /**
     * 現在の残高
     */
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    /**
     * メイン口座フラグ
     */
    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    /**
     * 貯金用口座フラグ
     */
    @Column(name = "is_savings", nullable = false)
    private boolean isSavings;
}