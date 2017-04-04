import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import { Http, Response, Headers, RequestOptions } from '@angular/http';

@Injectable()
export class OverviewService {
  private url = 'http://localhost:3000/api/comments';
  constructor(private http:Http) {
  }

  getInfo():Observable<any> {
    return this.http.get(this.url)
      .map((res:Response) => res as any)
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'))
  }
}
