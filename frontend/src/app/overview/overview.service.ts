import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import {Car} from "./Car";

@Injectable()
export class OverviewService {
  private baseUrl = 'http://192.168.24.120:8080';

  constructor(private http: Http) {
  }

  getCars(): Observable<Car[]> {
    return Observable.interval(10000)
      .switchMap(() => this.http.get(this.baseUrl + "/vehicles")
        .map(this.extractData)
        .catch((error: any) => Observable.throw(error.json().error || 'Server error')));
  }

  addCar(licensePlate: string): Observable<Car> {
    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});
    return this.http.post(this.baseUrl + "/vehicle?licensePlate=" + licensePlate, {}, options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  addCars(numberOfCars: number): Observable<Car[]> {
    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});
    return this.http.post(this.baseUrl + "/vehicles?amount=" + numberOfCars, {}, options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  restart (): Observable<string> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    return this.http.post(this.baseUrl + "/restart", {} , options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  private extractData(res: Response) {
    let body = res.text();
    return JSON.parse(body).entity || {};
  }

  private handleError (error: Response | any) {
    // In a real world app, you might use a remote logging infrastructure
    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Observable.throw(errMsg);
  }
}
