import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Project, ProjectService } from '../services/project.service';
import { ProjectFormComponent } from './project-form.component';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, ProjectFormComponent],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  selectedProject: Project | null = null;
  showForm = false;

  constructor(private readonly projectService: ProjectService) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getProjects().subscribe(projects => {
      this.projects = projects;
    });
  }

  addProject(): void {
    this.selectedProject = { name: '', description: '', status: 'Active' };
    this.showForm = true;
  }

  editProject(project: Project): void {
    this.selectedProject = { ...project };
    this.showForm = true;
  }

  saveProject(project: Project): void {
    if (project.id) {
      this.projectService.updateProject(project).subscribe(() => this.loadProjects());
    } else {
      this.projectService.addProject(project).subscribe(() => this.loadProjects());
    }
    this.showForm = false;
    this.selectedProject = null;
  }

  deleteProject(id: number): void {
    this.projectService.deleteProject(id).subscribe(() => this.loadProjects());
  }

  cancel(): void {
    this.showForm = false;
    this.selectedProject = null;
  }
}
