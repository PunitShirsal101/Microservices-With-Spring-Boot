import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task } from '../services/task.service';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.scss']
})
export class TaskFormComponent {
  @Input() task: Task = { title: '', description: '', status: 'Pending' };
  @Output() save = new EventEmitter<Task>();

  onSubmit(): void {
    this.save.emit(this.task);
  }
}
