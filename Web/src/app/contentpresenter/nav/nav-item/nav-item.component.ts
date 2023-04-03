import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'bug-nav-item',
  templateUrl: 'nav-item.component.html',
  styleUrls: ['./nav-item.component.scss']
})
export class NavItemComponent implements OnInit {

  @Input()
  faIcon: string = '';

  @Input()
  title: string = '';

  constructor() { }

  ngOnInit(): void {
  }

}
