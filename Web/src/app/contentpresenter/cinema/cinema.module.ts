import { VideoelementComponent } from './videoelement/videoelement.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CinemaRoutingModule } from './cinema-routing.module';
import { CinemaComponent } from './cinema.component';
import {UtilsModule} from "../../utils/utils.module";
import { CinemaItemComponent } from './cinema-item/cinema-item.component';


@NgModule({
  declarations: [
    CinemaComponent,
    VideoelementComponent,
    CinemaItemComponent
  ],
  imports: [
    CommonModule,
    CinemaRoutingModule,
    UtilsModule
  ]
})
export class CinemaModule { }
