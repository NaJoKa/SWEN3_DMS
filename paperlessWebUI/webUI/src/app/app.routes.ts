import { Routes } from '@angular/router';
import {Documents} from './components/documents/documents';
import {Dashboard} from './components/dashboard/dashboard';
import {Upload} from './components/upload/upload';
import {Login} from './components/login/login';
import {Register} from './components/register/register';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: Dashboard },
  { path: 'documents', component: Documents },
  { path: 'upload', component: Upload },
  { path: 'login', component: Login},
  { path: 'register', component: Register}
];
