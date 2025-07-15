package com.nkraft.budget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = "name") // typeName -> name に変更
@Entity
@Table(name = "budget_transaction_types")
public class BudgetTransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_transaction_type_id")
    private Long id;

    @Column(name = "budget_transaction_type_name", nullable = false, unique = true) // typeName -> budget_transaction_type_name に変更
    private String name; // typeName -> name に変更

}