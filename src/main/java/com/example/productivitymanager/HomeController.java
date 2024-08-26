package com.example.productivitymanager;

import com.example.productivitymanager.model.Day;
import com.example.productivitymanager.model.Task;
import com.example.productivitymanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;


@Controller
public class HomeController {

    private final TaskService taskService;

    @Autowired
    public HomeController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Map<Day, List<Task>> tasksByDay = taskService.getTasksGroupedByDay();
        model.addAttribute("tasksByDay", tasksByDay);
        model.addAttribute("days", Day.values());

        return "index";
    }

}