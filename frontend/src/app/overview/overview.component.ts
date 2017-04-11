import {Component, OnInit} from '@angular/core';
import {OverviewService} from './overview.service'
import {ViewEncapsulation} from '@angular/core'
import {min} from "rxjs/operator/min";
import {Car} from "./Car";
import {ConfirmationService, Message} from "primeng/primeng";

@Component({
  selector: 'my-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
  providers: [OverviewService, ConfirmationService],
  encapsulation: ViewEncapsulation.None
})
export class OverviewComponent implements OnInit {
  name = 'Angular';
  slider: number = 10;
  licensePlate: string;
  cars: Car[] = [];
  msgs: Message[] = [];


  constructor(private service: OverviewService, private confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
    this.getCars();
  }

  getCars() {
    this.service.getCars().subscribe(
      res => {
        debugger;
        // alert(res);
        this.showInfo(" Retrieved: " + res.length + " cars");
        this.cars = res;
      },
      err => {
        console.log(err);
        this.showError(err);
      });
  }

  addCars() {
    this.service.addCars(this.slider).subscribe(
      res => {
        //alert(res);
        this.showInfo("Added: " + this.slider + " cars");
      },
      err => {
        console.log(err);
        this.showError(err);
      });
  }

  addCar() {
    this.service.addCar(this.licensePlate).subscribe(
      res => {
        debugger;
        this.showInfo(" Added: " + res.licensePlate);
      },
      err => {
        console.log(err);
        this.showError(err);
      });
  }

  restart() {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to restart?',
      accept: () => {
        this.service.restart().subscribe(
          res => {
            this.showInfo("Restarting...")
          },
          err => {
            console.log(err);
            this.showError(err);
          });
      },
      reject: () => {
        this.showInfo("Declined confirmation.")
      }
    });
  }


  showError(message: string) {
    this.msgs = [];
    this.msgs.push({severity: 'error', summary: 'Error Message', detail: message});
  }

  showInfo(message: string) {
    this.msgs = [];
    this.msgs.push({severity: 'info', summary: 'Info Message', detail: message});
  }

  hideMessage() {
    this.msgs = [];
  }


}
