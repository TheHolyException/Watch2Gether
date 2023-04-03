import {AfterContentInit, Component, HostBinding, Input, OnInit} from '@angular/core';
import {IService} from "../../../../utils/iservice";
import {IconService} from "../../../../utils/services/icon.service";
import {IIcon} from "../../../../utils/iicon";
import {ServiceService} from "../../../../utils/services/service-service.service";
import {Router} from "@angular/router";

@Component({
  selector: 'bug-serviceitem',
  templateUrl: './serviceitem.component.html',
  styleUrls: ['./serviceitem.component.scss']
})
export class ServiceitemComponent implements OnInit {

  @Input()
  public service!:IService;
  public icon?:IIcon;
  public imagePath?:string;

  @HostBinding('class.folded')
  public folded:boolean = false;

  public extendSubservices:boolean = false;

  constructor(private readonly iconService:IconService,
              public readonly serviceService:ServiceService,
              private readonly router:Router) {

  }

  ngOnInit(): void {
    // if (this.service.iconID) {
    //   this.iconService.findByID(this.service.iconID).subscribe(item => {
    //     console.log('found icon: ' + item)
    //     this.icon = item;
    //   });
    // }
  }

  onFold() {
    this.folded = !this.folded;
  }

  onFoldSubServices() {
    this.extendSubservices = !this.extendSubservices;
  }

  onOptionClick() {
    this.router.navigate(['services', 'panel', 'console'], {queryParams: { id : this.service.id}})
  }
}
