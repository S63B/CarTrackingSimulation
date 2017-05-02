import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import {Car} from "./Car";

@Injectable()
export class OverviewService {
  private baseUrl = 'http://192.168.24.125:8080';

  constructor(private http: Http) {
  }

  /**
   * Retrieves all the cars that are running in the simulation application
   * @returns {Observable<R|I>|Observable<R>}   All the cars that are running in the simulation application.
   */
  getCars(): Observable<Car[]> {
    return Observable.interval(10000)
      .switchMap(() => this.http.get(this.baseUrl + "/vehicles")
        .map(this.extractData)
        .catch((error: any) => Observable.throw(error.json().error || 'Server error')));
  }


  /**
   * Adds a car to the simulation application with a specific license plate.
   * @param licensePlate        The license plate of the car you want to add.
   * @returns {Observable<R>}   The car you want to add.
   */
  addCar(licensePlate: string): Observable<Car> {
    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});
    return this.http.post(this.baseUrl + "/vehicle?licensePlate=" + licensePlate, {}, options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  /**
   * Adds random cars to the simulation application.
   * @param numberOfCars        The number of cars you want to add.
   * @returns {Observable<R>}   The cars you want to add.
   */
  addCars(numberOfCars: number): Observable<Car[]> {
    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});
    return this.http.post(this.baseUrl + "/vehicles?amount=" + numberOfCars, {}, options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  /**
   * Restarts the simulation application on the VM.
   * @returns {Observable<R>}
   */
  restart (): Observable<string> {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    let options = new RequestOptions({ headers: headers });
    return this.http.post(this.baseUrl + "/restart", {} , options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  /**
   * Extracts the object from the response
   * @param res         The response from the backend
   * @returns {any|{}}  The extracted object from the response.
   */
  private extractData(res: Response) {
    let body = res.text();
    return JSON.parse(body).entity || {};
  }

  /**
   * Handles error for the API methods
   * @param error     The error returned from the backend
   * @returns {any}   A observable with the error message
   */
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
