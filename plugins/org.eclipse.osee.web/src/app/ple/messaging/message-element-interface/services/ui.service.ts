import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UiService {

  private _filter: BehaviorSubject<string> = new BehaviorSubject<string>("");

  private _UpdateRequired: Subject<boolean> = new Subject<boolean>();
  private _branchId: BehaviorSubject<string> = new BehaviorSubject<string>("0");
  private _messageId: BehaviorSubject<string> = new BehaviorSubject<string>("0");
  private _subMessageId: BehaviorSubject<string> = new BehaviorSubject<string>("0");
  constructor() { }

  get filter() {
    return this._filter
  }

  set filterString(filter: string) {
    if (filter !== this._filter.getValue()) {
      this._filter.next(filter); 
    }
  }

  get UpdateRequired() {
    return this._UpdateRequired;
  }

  set updateMessages(value: boolean) {
    this._UpdateRequired.next(value);
  }

  get BranchId() {
    return this._branchId;
  }

  set BranchIdString(value: string) {
    this._branchId.next(value);
  }

  get messageId() {
    return this._messageId;
  }

  set messageIdString(value: string) {
    this._messageId.next(value);
  }

  get subMessageId() {
    return this._subMessageId;
  }

  set subMessageIdString(value: string) {
    this._subMessageId.next(value);
  }
}
