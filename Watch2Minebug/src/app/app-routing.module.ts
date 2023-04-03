import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {UserGuard} from "./utils/user.guard";

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./contentpresenter/contentpresenter.module').then(m => m.ContentpresenterModule)
  },
  {
    path: 'login',
    loadChildren: () => import('./login/login.module').then(m => m.LoginModule)
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
