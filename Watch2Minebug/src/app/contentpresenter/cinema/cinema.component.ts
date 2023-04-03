import { pipe, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { CinemaService } from './../../utils/services/cinema.service';
import { map, switchMap } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { environment } from './../../../environments/environment';
import { WebSocketService } from './../../utils/services/web-socket.service';
import { BehaviorSubject, filter } from 'rxjs';
import { IVideoElement } from './../../utils/ivideoelement';
import { VideoelementService } from './../../utils/services/videoelement.service';
import { Component, ElementRef, AfterContentInit, ViewChild } from '@angular/core';
import { ICinema } from 'src/app/utils/icinema';

@Component({
  selector: 'bug-cinema',
  templateUrl: './cinema.component.html',
  styleUrls: ['./cinema.component.scss']
})
export class CinemaComponent implements AfterContentInit {

  protected clientList$:BehaviorSubject<string[] | undefined> = new BehaviorSubject<string[] | undefined>(undefined);
  protected videoList$:BehaviorSubject<IVideoElement[] | undefined> = new BehaviorSubject<IVideoElement[] | undefined>(undefined);
  protected selectedVideo$:BehaviorSubject<IVideoElement | undefined> = new BehaviorSubject<IVideoElement | undefined>(undefined);
  protected downloadProgress$:BehaviorSubject<number | undefined> = new BehaviorSubject<number | undefined>(50);

  protected cinema:ICinema | undefined = undefined;

  protected api:string = environment.api;
  private skipNextSeek:boolean = false;
  protected hasparams:boolean = false;

  @ViewChild('vid')       video:ElementRef | undefined;
  @ViewChild('searchbar') searchbar:ElementRef | undefined;


  constructor(protected readonly videoelementService:VideoelementService,
              protected readonly webSocketService:WebSocketService,
              private   readonly activatedRoute:ActivatedRoute,
              protected readonly cinemaService:CinemaService,
              private   readonly http:HttpClient) { }

  ngAfterContentInit(): void {
    this.activatedRoute.queryParams.forEach(x => {
      if (x["id"]) {
        this.hasparams = true;
        this.cinemaService.findByIDDirect(x["id"])
          .subscribe(cinema => {
            this.cinema = cinema;
            this.initCinema();
          }
        );
      }
    });
    
    
  }

  private initCinema(): void {
    if (!this.cinema) {
      console.log("Failed to obtain CinemaID");
      return;
    }

    this.refreshVideoList();

    this.http.get<string[]>(this.cinemaService.getAPI() + this.cinema.id + "/clients").subscribe(response => {
      console.log("clients:" + response.length);
      this.clientList$.next(response);
    });

    this.http.get<IVideoElement>(this.cinemaService.getAPI() + this.cinema.id + "/currentvideo").subscribe(response => {
      this.selectedVideo$.next(response);
    });
    
    this.webSocketService.socket$.next({  channel:   'videoctrl', 
                                          subscribe:   this.cinema.id});


    this.webSocketService.observer$.pipe(filter ( v => v["channel"] === "videoctrl") ).subscribe(message => {
      console.log(message);
      

      if (message["command"]) {
        switch (message["command"]) {
          case "vid":
            this.skipNextSeek = true;
            this.video!.nativeElement.currentTime = message["timestamp"];
  
            if (message["playstate"] === "true")
              this.video!.nativeElement.play();
            else 
            this.video!.nativeElement.pause();
  
            break;
  
          case "select":
            this.videoelementService.findByIDCached(message["uuid"]).subscribe(result => {
              console.log("Selecting new Video: " + result);
              //this.selectedVideo$.next(result);
              //this.selectNewVideo(result?.id!);
              if (result) {
                this.setSelectedVideo(result);
                this.video!.nativeElement.pause();
              }
            });

            break;

          case "progress":
            this.downloadProgress$.next(message["progress"]);
            console.log("Donwload progress: " + message["progress"]);
            break;

          case "refreshvideolist":
            this.refreshVideoList();
            break;
        }
      } else {
        console.log("unknown packet " + message);        
      }
    })
  }

  videoClicked():void {
    console.log("clicked !!!!!");
    this.video!.nativeElement.pause();
    
  }

  pause():void {

    this.webSocketService.socket$.next({  channel:   'videoctrl', 
                                          command:   this.video!.nativeElement.paused ? "play" : "pause", 
                                          timestamp: this.video!.nativeElement.currentTime});

    // if (this.vid.paused) this.vid.play();
    // else this.vid.pause();



    // let newVidData = this.selectedVideo$.value!;
    // newVidData.paused=this.video!.nativeElement.paused;
    //this.selectedVideo$.next(newVidData);    
  }

  seeked():void {
    if (this.skipNextSeek) {
      this.skipNextSeek = false;
      return;
    }

    this.webSocketService.socket$.next({  channel: 'videoctrl',
                                          command: 'seek',
                                          playstate: this.video!.nativeElement.paused ? "false" : "true",
                                          timestamp: this.video!.nativeElement.currentTime });
    this.video!.nativeElement.pause();
    
  }

  setSelectedVideo(video:IVideoElement) {
    if (this.selectedVideo$.value != video) {
      console.log("Selecting new video");
      this.selectedVideo$.next(video);
      this.webSocketService.socket$.next({  channel: 'videoctrl',
                                            command: 'select',
                                            uuid: video.id
      })
    }
  }



  // No Cinema Content

  onCreate() {
    console.log("create");
    this.http.get<ICinema>(environment.api+'cinema/create').pipe(tap({
      next: x => {
        this.cinemaService.reload().subscribe();
      }
    })).subscribe();
  }

  submitSearch(text:Event) {
    console.log(this.searchbar?.nativeElement!.value);
    this.webSocketService.socket$.next({  channel: 'videoctrl',
                                          command: 'search',
                                          videolink: this.searchbar?.nativeElement!.value})
    //this.http.post(this.cinemaService.getAPI() + this.cinema?.id + "/search", {url:this.searchbar?.nativeElement!.value}).subscribe(response => {
      
    //});
  }

  refreshVideoList() {
    this.http.get<IVideoElement[]>(this.cinemaService.getAPI() + this.cinema!.id + "/videos").subscribe(response => {
      this.videoList$.next(response);
      this.downloadProgress$.next(undefined);
    });
  }

  // private selectNewVideo(link:string) : void {
  //   this.selectedVideoURL$.next(this.api+"videos/stream/"+link);
  // }

}
