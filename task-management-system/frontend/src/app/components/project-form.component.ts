import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Project } from '../services/project.service';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.scss']
})
export class ProjectFormComponent {
  @Input() project: Project = { name: '', description: '', status: 'Active' };
  @Output() save = new EventEmitter<Project>();

  onSubmit(): void {
    this.save.emit(this.project);
  }
}
