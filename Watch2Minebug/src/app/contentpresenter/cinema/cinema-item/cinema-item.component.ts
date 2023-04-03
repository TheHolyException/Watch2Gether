import { Router } from '@angular/router';
import { VideoelementService } from './../../../utils/services/videoelement.service';
import { Component, HostListener, Input, AfterContentInit } from '@angular/core';
import { ICinema } from 'src/app/utils/icinema';
import { IUser } from 'src/app/utils/iuser';
import { UserService } from 'src/app/utils/services/user.service';

@Component({
  selector: 'bug-cinema-item',
  templateUrl: './cinema-item.component.html',
  styleUrls: ['./cinema-item.component.scss']
})
export class CinemaItemComponent implements AfterContentInit {

  constructor(private readonly userservice:UserService,
              private readonly videoelementService:VideoelementService,
              private readonly router:Router) { }

  @Input()
  cinemaElement!:ICinema;

  protected owner:IUser|undefined;

  ngAfterContentInit() {
     this.userservice.findByID(this.cinemaElement.ownerID).subscribe(result => {
      this.owner = result;      
     })
    
  }

  @HostListener("click")
  onClick() {
    console.log("Clicked on CinemaItem");
    this.router.navigate(['cinema'], {queryParams: {id: this.cinemaElement.id}});
    
  }

}
