package com.nkraft.budget.repository;

import com.nkraft.budget.entity.Goal;
import com.nkraft.user.entity.NkraftUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByNkraftUserAndIsDeletedFalseOrderByGoalIdDesc(NkraftUser user);
    Optional<Goal> findByGoalIdAndNkraftUserAndIsDeletedFalse(Long goalId, NkraftUser user);
}