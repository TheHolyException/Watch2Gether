import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ServicesRoutingModule } from './services-routing.module';
import { ServicesComponent } from './services.component';
import { ServiceboardComponent } from './serviceboard/serviceboard.component';
import { ServiceitemComponent } from './serviceboard/serviceitem/serviceitem.component';
import {UtilsModule} from "../../utils/utils.module";


@NgModule({
  declarations: [
    ServicesComponent,
    ServiceboardComponent,
    ServiceitemComponent
  ],
  imports: [
    CommonModule,
    ServicesRoutingModule,
    UtilsModule
  ]
})
export class ServicesModule { }
