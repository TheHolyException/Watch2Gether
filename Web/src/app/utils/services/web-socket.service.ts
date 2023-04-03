import { environment } from 'src/environments/environment';
import { Injectable } from "@angular/core";
import {webSocket, WebSocketSubject} from 'rxjs/webSocket';
import { retry, RetryConfig } from "rxjs/operators";
import {UserService} from "./user.service";
import { Observable } from "rxjs/internal/Observable";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private static readonly URL = environment.ws;

  public socket$!:WebSocketSubject<any>;
  public observer$!:Observable<any>;

  private authInterval!:number;
  private isAuth:boolean = false;

  constructor(private readonly userService:UserService) {
    this.connect();
    window.setInterval(() => {
    }, 2000);
  }

  private connect() {
    this.socket$ = new WebSocketSubject({
      url: WebSocketService.URL,
      openObserver: {
        next: value => {
          this.auth(); // Authenticate user   
        }
      }
    });

    this.observer$ = this.socket$.pipe(retry(3000));

    this.observer$.subscribe({
      //next: (message => console.log('[WebSocket: Rx] ' + message)),
      complete: (() => {
        console.log("Completed WebSocket");
        
      }),
      error: (error => {
        console.log(error);
        
        if (error instanceof CloseEvent) {
          console.log("closing");
          //this.connect(); // Trying to reconnect
        }
      })
    })

    // let sub1 = this.socket$.subscribe({
    //   //next: (message => console.log('[WebSocket: Rx] ' + message)),
    //   error: (error => {
    //     if (error instanceof CloseEvent) {
    //       console.log("closing");
    //       sub1.unsubscribe();
    //       this.connect(); // Trying to reconnect
    //     }
    //   })
    // })    
  }

  public auth() {
    let token = this.userService.currentUser$.value?.authenticationToken;
    if (!token) {
      console.log("Token not present, not authing websocketclient");      
      return;
    }
    

    // Subsribe to the websocket to scan for an response for the Authentication
    let sub = this.socket$.subscribe(authMessage => {
      //console.log("get : " + authMessage.toString());
      if (authMessage['auth']) {
        console.log("Successfully Authenticated websocket with Session Token");        
        window.clearInterval(this.authInterval);
        sub.unsubscribe();
      }
    });
    

    // Sending an Authentication request to the server
    this.authInterval = window.setInterval(() => {
      console.log("authing");
      this.socket$.next({ auth: token });
    }, 200);
  }

  /*

  public receive$ = new Subject();

  public transmit$!: Subject<any>;
  public socket!:WebSocket;
  private authInterval!:number;
  private authProcess:boolean = false;

  constructor(private readonly userService: UserService) {
    this.connect();
    // this.messages = <Subject<any>>this.connect(WebSocketService.URL).pipe(
    //   map((response: MessageEvent): any => {
    //     return response.data;
    //   })
    // )

    window.setInterval(() => {
      console.log(this.socket.readyState)
      if (this.socket.readyState !== 1) {
        console.log("Reconnect")
        this.connect();
      }
    }, 5000);
  }
  public connect() {
  if (this.socket) this.socket.close();
    this.authProcess = true;
    this.transmit$ = <Subject<any>>this.create(WebSocketService.URL).pipe(
      map((response: MessageEvent): any => {
        return response.data;
      })
    )

    let sub = this.transmit$.subscribe(message => {
      this.receive$.subscribe(message);
    })

    this.authInterval = window.setInterval(() => {
      if (this.socket.readyState == 1) {
        this.transmit$.next({ auth: this.userService.currentUser$.value?.authenticationToken });
      }
    }, 1000);
  }

  private create(url:string) : AnonymousSubject<MessageEvent> {
    console.log('creating new socket')
    this.socket = new WebSocket(url);
    let subject = new Subject();

    let observable = new Observable((obs: Observer<MessageEvent>) => {
      this.socket.onmessage = obs.next.bind(obs);
      this.socket.onerror   = obs.error.bind(obs);
      this.socket.onclose   = obs.complete.bind(obs);
      return this.socket.close.bind(this.socket);
    });


    let observer = {
      error: (err: any) => {},
      complete: () => {},
      next: (data: Object) => {
        console.log(data);
        if (this.socket.readyState === WebSocket.OPEN) {
          this.socket.send(JSON.stringify(data));
        }
      }
    };

    return new AnonymousSubject<MessageEvent>(observer, observable);
  }

   */

}

