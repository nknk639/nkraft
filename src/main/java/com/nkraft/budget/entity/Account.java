package com.nkraft.budget.entity;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nkraft.user.entity.NkraftUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 口座情報を管理するエンティティ。
 * 要件定義書 4. データモデル に基づいています。
 */
@Entity
@Table(name = "accounts")
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = "nkraftUser")
@EqualsAndHashCode(of = "accountId")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nkraft_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accounts_nkraft_users"))
    @JsonIgnore
    private NkraftUser nkraftUser;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "is_main")
    private Boolean isMain;

    @Column(name = "is_savings")
    private Boolean isSavings;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}