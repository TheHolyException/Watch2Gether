import {IUser} from "./iuser";

export interface AuthUser extends IUser {

  authenticationToken?:string;

}
