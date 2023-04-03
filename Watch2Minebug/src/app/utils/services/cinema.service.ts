import { Injectable } from '@angular/core';
import { ICinema } from '../icinema';
import { SimpleService } from './service.service';

@Injectable({
  providedIn: 'root'
})
export class CinemaService extends SimpleService<ICinema, string> {

  protected override init(): void {
    super.init('cinema');

    this.reload().subscribe(m => {
      console.log("Fetched cinemas: " + this.items$.value.length);
    })
  }

}
