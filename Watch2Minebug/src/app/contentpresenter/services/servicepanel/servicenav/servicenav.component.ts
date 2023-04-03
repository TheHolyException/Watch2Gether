import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'bug-servicenav',
  templateUrl: './servicenav.component.html',
  styleUrls: ['./servicenav.component.scss']
})
export class ServicenavComponent implements OnInit {

  @Input()
  faIcon: string = '';

  @Input()
  title: string = '';
  constructor() { }

  ngOnInit(): void {
  }

}
