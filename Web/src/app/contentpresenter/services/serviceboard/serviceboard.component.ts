import { Component, OnInit } from '@angular/core';
import {ServiceService} from "../../../utils/services/service-service.service";

@Component({
  selector: 'bug-serviceboard',
  templateUrl: './serviceboard.component.html',
  styleUrls: ['./serviceboard.component.scss']
})
export class ServiceboardComponent implements OnInit {

  constructor(public readonly serviceService:ServiceService) { }

  ngOnInit(): void {
    this.serviceService.reload().subscribe();
  }

}
