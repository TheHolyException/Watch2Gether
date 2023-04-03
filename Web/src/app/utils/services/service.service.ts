import {Injectable, Injector} from '@angular/core';
import {BehaviorSubject, catchError, EMPTY, find, firstValueFrom, lastValueFrom, map, Observable, of, shareReplay, tap, zip} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
@Injectable({
  providedIn: 'root'
})
export abstract class SimpleService<T extends {id?: ID}, ID> {

  public items$: BehaviorSubject<T[]> = new BehaviorSubject<T[]>([]);
  protected api!: string;

  protected constructor(public readonly http: HttpClient,
                        public readonly injector:Injector) {
    this.init();
  }

  protected init(apiSuffix: string = '') {
    this.api = environment.api + apiSuffix + '/';
  }

  public reload() : Observable<T[]> {
    return this.http.get<T[]>(this.api).pipe(
      tap(items => {
        this.items$.next(items);
        console.log("Fetched dataset for " + this.api);
        
      })
    );
  }

  public create(item: T) : Observable<T> {
    return this.http.post<T>(this.api, item).pipe(tap({
      complete: () => this.reload().subscribe()
    }))
  }

  public update(item: T) : Observable<T> {
    return this.http.put<T>(this.api + item.id, item).pipe(tap({
      complete: () => this.reload().subscribe()
    }))
  }

  public delete(item: T) : Observable<T> {
    return this.http.delete<T>(this.api + item.id).pipe(tap({
      complete: () => this.reload().subscribe()
    }))
  }

  public findByIDCached(itemID: ID) : Observable<T | undefined> {
    return this.items$.pipe(
      map(items => items.find(item => item.id == itemID))
    )
  }

  public findByIDDirect(itemID: ID) : Observable<T | undefined> {
    return this.http.get<T>(this.api + itemID);
  }

  cache: { [id: string]: Observable<T> } = {};

  public findByID(code: string): Observable<T> {
    if (this.cache[code]) {
      return this.cache[code];
    }
    
    let observable = this.http.get<T>(this.api+code).pipe(
      shareReplay(1),
      catchError(err => {
          delete this.cache[code];
          return EMPTY;
      })
    );

    this.cache[code] = observable;
    return observable;
  }


  private getDefaultHeader() {

  }

  public getAPI():string {
    return this.api;
  }


}
