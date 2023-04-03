import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ServicepanelRoutingModule } from './servicepanel-routing.module';
import { ServicepanelComponent } from './servicepanel.component';
import { ServicenavComponent } from './servicenav/servicenav.component';
import { ConsoleComponent } from './console/console.component';
import { ConfigurationComponent } from './configuration/configuration.component';
import {FormsModule} from "@angular/forms";


@NgModule({
  declarations: [
    ServicepanelComponent,
    ServicenavComponent,
    ConsoleComponent,
    ConfigurationComponent
  ],
  imports: [
    CommonModule,
    ServicepanelRoutingModule,
    FormsModule
  ]
})
export class ServicepanelModule { }
