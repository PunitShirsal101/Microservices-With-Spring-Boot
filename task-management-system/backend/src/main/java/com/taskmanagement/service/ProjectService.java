package com.taskmanagement.service;

import com.taskmanagement.model.Project;
import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();
    Project createProject(Project project);
    Project updateProject(String id, Project project);
    void deleteProject(String id);
    Project getProjectById(String id);
}