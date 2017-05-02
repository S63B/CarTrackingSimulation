import { NgModule }      from '@angular/core';
import {
  ButtonModule, SliderModule, PanelModule, InputTextModule, DataTableModule, MessagesModule, ConfirmDialogModule,
  ConfirmationService, PaginatorModule
}  from 'primeng/primeng';

import {BrowserAnimationsModule} from "@angular/platform-browser/animations";


@NgModule({
  imports:      [
    ButtonModule,
    SliderModule,
    PanelModule,
    BrowserAnimationsModule,
    InputTextModule,
    DataTableModule,
    MessagesModule,
    ConfirmDialogModule,
    PaginatorModule
  ],
  providers:    [ ConfirmationService ],
  exports:[
    ButtonModule,
    SliderModule,
    PanelModule,
    BrowserAnimationsModule,
    InputTextModule,
    DataTableModule,
    MessagesModule,
    ConfirmDialogModule,
    PaginatorModule
  ]
})
export class PrimengModule { }
