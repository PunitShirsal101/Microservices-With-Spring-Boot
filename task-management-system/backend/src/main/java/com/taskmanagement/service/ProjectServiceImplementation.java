package com.taskmanagement.service;

import com.taskmanagement.model.Project;
import com.taskmanagement.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImplementation implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImplementation(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(String id, Project project) {
        project.setId(id);
        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(String id) {
        projectRepository.deleteById(id);
    }

    @Override
    public Project getProjectById(String id) {
        return projectRepository.findById(id).orElse(null);
    }
}