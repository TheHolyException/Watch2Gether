import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContentpresenterComponent } from './contentpresenter.component';

const routes: Routes = [
  {
    path: '',
    component: ContentpresenterComponent,
    children: [
      {
        path: 'home',
        loadChildren: () => import('./home/home.module').then(m => m.HomeModule)
      },
      {
        path: 'cinema',
        loadChildren: () => import('./cinema/cinema.module').then(m => m.CinemaModule)
      },
      {
        path: 'services',
        loadChildren: () => import('./services/services.module').then(m => m.ServicesModule)
      }
    ]
  }
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContentpresenterRoutingModule { }
