
import { Routes } from '@angular/router';
import { TaskListComponent } from './components/task-list.component';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';
import { ProjectListComponent } from './components/project-list.component';

export const routes: Routes = [
	{ path: '', component: TaskListComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent }
	,{ path: 'projects', component: ProjectListComponent }
];
