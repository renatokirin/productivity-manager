package com.example.productivitymanager.service;

import com.example.productivitymanager.model.Day;
import com.example.productivitymanager.model.Task;

import java.util.List;
import java.util.Map;

public interface TaskService {
    List<Task> getAllTasks();
    Task getTaskById(int id);
    Task saveTask(Task task);
    Task moveTask(int id, String direction);
    void deleteTask(int id);
    Map<Day, List<Task>> getTasksGroupedByDay();
}