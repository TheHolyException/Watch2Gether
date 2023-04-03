import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ServicepanelComponent } from './servicepanel.component';
import {ConsoleComponent} from "./console/console.component";
import {ConfigurationComponent} from "./configuration/configuration.component";

const routes: Routes = [
  {
    path: '',
    component: ServicepanelComponent,
    children: [
      {
        path: 'console',
        component: ConsoleComponent
      },
      {
        path: 'configuration',
        component: ConfigurationComponent
      }
    ]
  }
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServicepanelRoutingModule { }
