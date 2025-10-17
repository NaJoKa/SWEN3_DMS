import { Routes } from '@angular/router';
import {Documents} from './components/documents/documents';
import {Dashboard} from './components/dashboard/dashboard';
import {Upload} from './components/upload/upload';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: Dashboard },
  { path: 'documents', component: Documents },
  { path: 'upload', component: Upload },
];
