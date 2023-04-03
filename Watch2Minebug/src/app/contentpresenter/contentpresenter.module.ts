import { UserinfoComponent } from './userinfo/userinfo.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ContentpresenterRoutingModule } from './contentpresenter-routing.module';
import { ContentpresenterComponent } from './contentpresenter.component';
import { NavComponent } from './nav/nav.component';
import { NavItemComponent } from './nav/nav-item/nav-item.component';


@NgModule({
  declarations: [
    ContentpresenterComponent,
    NavComponent,
    NavItemComponent,
    UserinfoComponent
  ],
  imports: [
    CommonModule,
    ContentpresenterRoutingModule
  ]
})
export class ContentpresenterModule { }
