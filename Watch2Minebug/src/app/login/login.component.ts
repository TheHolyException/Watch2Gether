import { Component, OnInit } from '@angular/core';
import {UserService} from "../utils/services/user.service";
import {Router} from "@angular/router";
import {AuthService} from "../utils/auth.service";

@Component({
  selector: 'bug-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  public username:string='';
  public password:string='';

  constructor(private readonly userService: UserService,
              private readonly authService: AuthService,
              private readonly router: Router) { }

  onLogin() {
    this.userService.authUser(this.username, this.password)
      .subscribe(response => {
        if (response) {
          console.log(response);
          this.userService.selectCurrentUser(response);
          this.router.navigate(['/home']);
          localStorage.setItem('token', response[0].toString());
        } else {
          alert('invalid credentials!')
        }
      })
  }
}
