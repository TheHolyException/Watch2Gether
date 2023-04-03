import { Injectable } from '@angular/core';
import {SimpleService} from "./service.service";
import {IService} from "../iservice";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {WebSocketService} from "./web-socket.service";

@Injectable({
  providedIn: 'root'
})
export class ServiceService extends SimpleService<IService, number> {


  webSocketService!:WebSocketService;

  protected override init() {
    super.init('service');

    //this.webSocketService = this.injector.get(WebSocketService);

    this.webSocketService.socket$.subscribe(message => {
      console.log(message);
    })

    //window.setInterval(() => {
      //this.webSocketService.socket$.next({ channel: 'service', command: 'subscribe-console', service: '2'});
    //}, 5000, 5000)
  }

  public sendCommand(command:string, serviceID:number) : Observable<IService> {
    return this.http.post<IService>(`${this.api}${serviceID}/command`, command);
  }

  public sendAction(action:string, serviceID:number) {
    return this.http.post<IService>(`${this.api}${serviceID}/action`, action);
  }

}
