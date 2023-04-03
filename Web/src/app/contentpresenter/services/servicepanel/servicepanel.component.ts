import { Component, OnInit } from '@angular/core';
import {IService} from "../../../utils/iservice";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceService} from "../../../utils/services/service-service.service";

@Component({
  selector: 'bug-servicepanel',
  templateUrl: './servicepanel.component.html',
  styleUrls: ['./servicepanel.component.scss']
})
export class ServicepanelComponent implements OnInit {

  public service?: IService;

  constructor(private readonly activatedRoute: ActivatedRoute,
              private readonly router:Router,
              private readonly serviceService: ServiceService) { }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(x => {
      console.log(x['id'])
      if (!x['id']) this.router.navigate(['..', '..']);
      this.serviceService.findByIDDirect(x['id']).subscribe(x2 => {
        this.service = x2;
      })
    });
  }

}
