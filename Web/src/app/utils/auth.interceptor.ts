import {forwardRef, Inject, Injectable, Injector} from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import {mergeMap, Observable} from 'rxjs';
import {environment} from "../../environments/environment";
import {AuthUser} from "./auth-user";
import { UserService } from './services/user.service';
import {AuthService} from "./auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // constructor(@Inject(forwardRef(() => UserService)) private userService: UserService) {
  constructor() {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (request.url.toString() === environment.api + 'auth/login') return next.handle(request);

    let token = localStorage.getItem('token');
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      })
    }
    return next.handle(request);
  }


}
