import { WebSocketService } from './web-socket.service';
import { webSocket } from 'rxjs/webSocket';
import {Injectable, OnInit} from '@angular/core';
import {BehaviorSubject, map, Observable, tap} from "rxjs";
import {IUser} from "../iuser";
import {SimpleService} from "./service.service";
import {AuthUser} from "../auth-user";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class UserService extends SimpleService<IUser, string> {

  public currentUser$!:BehaviorSubject<AuthUser | undefined>;

  public static readonly dummyUser:IUser = {
    username: '$DUMMY$'
  }

  protected override init() {
    super.init('user');
    this.currentUser$ = new BehaviorSubject<AuthUser | undefined>(undefined);
/*    this.currentUser$.subscribe(n => {
      console.log('NEW CU: ' + n);
    })*/
    this.checkStorageToken();
    this.loadUser().subscribe();
  }

  private checkStorageToken() {
    let token!:string | null;
    if ((token = localStorage.getItem('token')) != null) {
      this.selectCurrentUser([token, UserService.dummyUser]);
    }
  }

  public selectCurrentUser(userData: Object[]) {
    const val = {...userData[1], authenticationToken:userData[0]};
    this.currentUser$.next(val as AuthUser);
  }

  public authUser(username: string, password: string) : Observable<String[]> {
    return this.http.get<String[]>(environment.api + 'auth/login', {params:{username, password}})
      //.pipe(map(users => users.length === 1 ? users[0] : undefined));
  }

  public loadUser() : Observable<String[]> { 
    return this.http.get<String[]>(environment.api + "user/self").pipe(tap(userData => {
      let token!:string;
      token = this.currentUser$.value?.authenticationToken!;
      this.selectCurrentUser([token, userData]);
    }));
  }


}
