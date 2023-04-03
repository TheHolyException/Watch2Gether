export class Status {

  public static readonly OFFLINE = new Status('Offline', 0);
  public static readonly STARTING = new Status('Starting', 1);
  public static readonly ONLINE = new Status('Online', 2);

  private constructor(public text:string, public id:number) {

  }

}
