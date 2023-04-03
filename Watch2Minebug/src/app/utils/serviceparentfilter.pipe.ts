import { Pipe, PipeTransform } from '@angular/core';
import {IService} from "./iservice";

@Pipe({
  name: 'serviceparentfilter'
})
export class ServiceparentfilterPipe implements PipeTransform {

  transform(items: IService[] | null, parentID:number = -1): IService[] | null {
    if (!items) return null;
    if (parentID == -1) {
      return items.filter(item => !item.parentID);
    } else {
      return items.filter(item => item.parentID === parentID);
    }
  }

}
