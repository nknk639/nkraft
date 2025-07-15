package com.nkraft.budget.entity;

import java.math.BigDecimal;

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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "borrows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "nkraftUser")
@EqualsAndHashCode(of = "borrowId")
public class Borrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borrow_id")
    private Long borrowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nkraft_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_borrows_nkraft_users"))
    private NkraftUser nkraftUser;

    @Column(name = "borrow_name", nullable = false)
    private String borrowName;

    private BigDecimal totalAmount;
    private BigDecimal repaidAmount;
}