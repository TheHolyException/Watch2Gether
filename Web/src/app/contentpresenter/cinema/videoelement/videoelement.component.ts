import { IVideoElement } from './../../../utils/ivideoelement';
import { VideoelementService } from './../../../utils/services/videoelement.service';
import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'bug-videoelement',
  templateUrl: './videoelement.component.html',
  styleUrls: ['./videoelement.component.scss']
})
export class VideoelementComponent implements OnInit {

  constructor() { }
  
  protected api:string = environment.api;

  @Input()
  videoElement?:IVideoElement
  
  videoID:string = "45202a66-d2f1-4f79-94a9-44f88e03b7b2"

  @Output()
  selectedVideo: EventEmitter<IVideoElement> = new EventEmitter();

  

  ngOnInit() {
  }

  @HostListener('click')
  onClick() {
    this.selectedVideo.emit(this.videoElement);
  }

}
