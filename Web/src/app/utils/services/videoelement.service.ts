import { WebSocketService } from './web-socket.service';
import { IVideoElement } from './../ivideoelement';
import { ServiceService } from './service-service.service';
import { Injectable, OnInit } from '@angular/core';
import { SimpleService } from './service.service';

@Injectable({
  providedIn: 'root'
})
export class VideoelementService extends SimpleService<IVideoElement, string> {

  webSocketService!:WebSocketService;

  

  protected override init() {
    super.init('videos');
    // this.webSocketService = this.injector.get(WebSocketService);

    // this.webSocketService.socket$.subscribe(message => {
    //   console.log("DEBUG WS: " + message);
    // });
    this.reload().subscribe(m => {
      console.log("fetched videos:  " + this.items$.value.length);
    });
    
    
  }

}
