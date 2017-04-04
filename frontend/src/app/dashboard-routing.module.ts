import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {OverviewComponent}   from './overview/overview.component';

const routes: Routes = [
  { path: '', redirectTo: '/overview', pathMatch: 'full' },
  { path: 'overview',  component: OverviewComponent },
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class DashboardRoutingModule {}
