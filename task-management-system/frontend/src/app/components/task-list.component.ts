import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Task, TaskService } from '../services/task.service';
import { TaskFormComponent } from './task-form.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, TaskFormComponent],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.scss']
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  selectedTask: Task | null = null;
  showForm = false;

  constructor(private readonly taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.taskService.getTasks().subscribe(tasks => {
      this.tasks = tasks;
    });
  }

  addTask(): void {
    this.selectedTask = { title: '', description: '', status: 'Pending' };
    this.showForm = true;
  }

  editTask(task: Task): void {
    this.selectedTask = { ...task };
    this.showForm = true;
  }

  saveTask(task: Task): void {
    if (task.id) {
      this.taskService.updateTask(task).subscribe(() => this.loadTasks());
    } else {
      this.taskService.addTask(task).subscribe(() => this.loadTasks());
    }
    this.showForm = false;
    this.selectedTask = null;
  }

  deleteTask(id: number): void {
    this.taskService.deleteTask(id).subscribe(() => this.loadTasks());
  }

  cancel(): void {
    this.showForm = false;
    this.selectedTask = null;
  }
}
