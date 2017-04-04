import {Component, OnInit} from '@angular/core';
import {OverviewService} from './overview.service'

@Component({
  selector: 'my-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
  providers: [ OverviewService ]
})
export class OverviewComponent implements OnInit {
  name = 'Angular';

  constructor(private service:OverviewService) {
  }

  ngOnInit():void {
    this.getInfo();
  }

  getInfo() {
    this.service.getInfo().subscribe(
      res => {
        alert(res);
      },
      err => {
        console.log(err);
        alert(err);
      });
  }
}
