import { NgModule }      from '@angular/core';

import { OverviewComponent }  from './overview.component';
import { OverviewService }  from './overview.service';
import { HttpModule } from '@angular/http';
import {FormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {PrimengModule} from "../primeng.module";

@NgModule({
  imports:      [
    HttpModule,
    FormsModule,
    BrowserAnimationsModule,
    PrimengModule
  ],
  declarations: [ OverviewComponent ],
  providers:    [ OverviewService ]
})
export class OverviewModule { }
