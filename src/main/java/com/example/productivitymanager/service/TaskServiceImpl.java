package com.example.productivitymanager.service;

import com.example.productivitymanager.exception.InvalidDirectionException;
import com.example.productivitymanager.exception.TaskNotFoundException;
import com.example.productivitymanager.model.Day;
import com.example.productivitymanager.model.Task;
import com.example.productivitymanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService  {

    @Autowired
    private TaskRepository taskRepository;

    public Task saveTask(Task task) {
        if (task.getId() == 0) {
            Integer maxHeightIndex = taskRepository.findMaxHeightIndexByDay(task.getDay());
            task.setHeightIndex(maxHeightIndex == null ? 0 : maxHeightIndex + 1);

            return taskRepository.save(task);
        }

        Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task with id: " + task.getId() + " not found "));

        task.setHeightIndex(existingTask.getHeightIndex());

        return taskRepository.save(task);
    }

    public void deleteTask(int taskId) {
        Task taskToDelete = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id: " + taskId + " not found "));

        Day day = taskToDelete.getDay();
        int deletedTaskHeight = taskToDelete.getHeightIndex();

        taskRepository.delete(taskToDelete);

        // update heightIndex for all tasks below the deleted task
        List<Task> tasksToUpdate = taskRepository.findByDayAndHeightIndexGreaterThan(day, deletedTaskHeight);
        for (Task task : tasksToUpdate) {
            task.setHeightIndex(task.getHeightIndex() - 1);
            taskRepository.save(task);
        }
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(int id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id: " + id + " not found "));
    }

    public Map<Day, List<Task>> getTasksGroupedByDay() {
        List<Task> allTasks = getAllTasks();
        return allTasks.stream()
                .collect(Collectors.groupingBy(Task::getDay));
    }



    public Task moveTask(int taskId, String direction) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with id: " + taskId + " not found "));

        Day currentDay = task.getDay();
        Day[] days = Day.values();
        int currentDayIndex = currentDay.ordinal();
        int currentHeightIndex = task.getHeightIndex();

        List<Task> tasksInDay = taskRepository.findByDayOrderByHeightIndexAsc(currentDay);
        int maxHeightIndex = tasksInDay.size() - 1;

        switch (direction) {
            case "left":
            case "right":
                Day newDay = direction.equals("left") ?
                        (currentDayIndex > 0 ? days[currentDayIndex - 1] : currentDay) :
                        (currentDayIndex < days.length - 1 ? days[currentDayIndex + 1] : currentDay);

                if (newDay != currentDay) {
                    updateHeightsAfterRemoval(currentDay, currentHeightIndex);
                    task.setDay(newDay);
                    task.setHeightIndex(getNewHeightIndexForDay(newDay));
                }
                break;
            case "up":
                if (currentHeightIndex > 0) {
                    Task aboveTask = tasksInDay.get(currentHeightIndex - 1);
                    swapHeightIndices(task, aboveTask);
                }
                break;
            case "down":
                if (currentHeightIndex < maxHeightIndex) {
                    Task belowTask = tasksInDay.get(currentHeightIndex + 1);
                    swapHeightIndices(task, belowTask);
                }
                break;
            default:
                throw new InvalidDirectionException("Invalid direction");
        }

        return taskRepository.save(task);
    }

    private void updateHeightsAfterRemoval(Day day, int removedIndex) {
        List<Task> tasksToUpdate = taskRepository.findByDayAndHeightIndexGreaterThan(day, removedIndex);
        for (Task task : tasksToUpdate) {
            task.setHeightIndex(task.getHeightIndex() - 1);
            taskRepository.save(task);
        }
    }

    private void swapHeightIndices(Task task1, Task task2) {
        int tempIndex = task1.getHeightIndex();
        task1.setHeightIndex(task2.getHeightIndex());
        task2.setHeightIndex(tempIndex);
        taskRepository.save(task1);
        taskRepository.save(task2);
    }

    private int getNewHeightIndexForDay(Day day) {
        Integer maxHeightIndex = taskRepository.findMaxHeightIndexByDay(day);
        return maxHeightIndex == null ? 0 : maxHeightIndex + 1;
    }

}
