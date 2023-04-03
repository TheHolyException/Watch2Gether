import { WebSocketService } from './../../utils/services/web-socket.service';
import { tap } from 'rxjs';
import { UserService } from './../../utils/services/user.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'bug-userinfo',
  templateUrl: './userinfo.component.html',
  styleUrls: ['./userinfo.component.scss']
})
export class UserinfoComponent implements OnInit {

  constructor(public readonly userService:UserService,
              public readonly websocketService:WebSocketService) { }

  ngOnInit() {
    // let sub = this.userService.loadUser().subscribe({
    //   next: (message => {
    //     this.websocketService.auth();
    //     sub.unsubscribe();
    //   }),
    //   error: (error => {
    //     console.log("failed");        
    //   })      
    // }); // Load userdata
  }

}
