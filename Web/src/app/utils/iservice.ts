export interface IService {

  id?:number;
  name:string;
  description?:string;
  serviceType:number;
  ownerID:number;
  parentID?:number;
  iconPath?:string;

}
