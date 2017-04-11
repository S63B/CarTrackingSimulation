import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { DashboardComponent }  from './dashboard.component';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { OverviewModule } from './overview/overview.module';


@NgModule({
  imports:      [
    BrowserModule,
    DashboardRoutingModule,
    OverviewModule,
  ],

  declarations: [ DashboardComponent ],
  bootstrap:    [ DashboardComponent ]
})
export class DashboardModule { }
