import { NgModule }      from '@angular/core';

import { OverviewComponent }  from './overview.component';
import { OverviewService }  from './overview.service';
import { HttpModule } from '@angular/http';

@NgModule({
  imports:      [
    HttpModule
  ],
  declarations: [ OverviewComponent ],
  providers:    [ OverviewService ]
})
export class OverviewModule { }
