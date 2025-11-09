package com.taskmanagement.service;

import com.taskmanagement.model.Task;
import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    Task createTask(Task task);
    Task updateTask(String id, Task task);
    void deleteTask(String id);
    Task getTaskById(String id);
}