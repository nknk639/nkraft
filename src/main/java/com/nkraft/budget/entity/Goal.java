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
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "nkraftUser")
@EqualsAndHashCode(of = "goalId")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nkraft_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_goals_nkraft_users"))
    private NkraftUser nkraftUser;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    private BigDecimal targetAmount;
    private BigDecimal savedAmount;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}