import { Injectable } from '@angular/core';
import {SimpleService} from "./service.service";
import {IIcon} from "../iicon";

@Injectable({
  providedIn: 'root'
})

export class IconService extends SimpleService<IIcon, number>{

  //Temporary disabled!
  protected override init() {
    super.init('icons');
    //this.reload().subscribe();
  }


}
