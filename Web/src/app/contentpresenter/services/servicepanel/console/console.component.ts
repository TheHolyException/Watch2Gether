import { Component, OnInit, AfterContentInit } from '@angular/core';
import {Status} from "../../../../utils/status";
import * as events from "events";
import {ServiceService} from "../../../../utils/services/service-service.service";
import {tap} from "rxjs";
import {ServicepanelComponent} from "../servicepanel.component";

@Component({
  selector: 'bug-console',
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.scss']
})
export class ConsoleComponent implements AfterContentInit {

  public status:Status = Status.STARTING;
  inputText: string = '';

  constructor(private readonly serviceService:ServiceService,
              private readonly servicePanel:ServicepanelComponent) { }

  ngAfterContentInit(): void {
    this.serviceService.webSocketService.socket$.next({ channel: 'service', command: 'subscribe-console', service: `${this.servicePanel.service?.id!}`})
  }

  onSubmit($event: any) {
    this.serviceService.sendCommand(this.inputText, this.servicePanel.service?.id!).subscribe(x => {
      this.inputText = '';
    })
  }
}
