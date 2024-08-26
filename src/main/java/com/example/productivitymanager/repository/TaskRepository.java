package com.example.productivitymanager.repository;

import com.example.productivitymanager.model.Day;
import com.example.productivitymanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("SELECT MAX(t.heightIndex) FROM Task t WHERE t.day = :day")
    Integer findMaxHeightIndexByDay(@Param("day") Day day);

    List<Task> findByDayOrderByHeightIndexAsc(Day day);

    List<Task> findByDayAndHeightIndexGreaterThan(Day day, int heightIndex);
}
