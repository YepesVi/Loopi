import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PopupService {

  // BehaviorSubject almacena el estado actual (cerrado por defecto)
  private isCategoryPopupOpenSubject = new BehaviorSubject<boolean>(false);
  
  // Observable para que otros componentes se suscriban a los cambios
  isCategoryPopupOpen$ = this.isCategoryPopupOpenSubject.asObservable();

  /**
   * Abre el popup de categorías.
   */
  openCategoryPopup(): void {
    this.isCategoryPopupOpenSubject.next(true);
  }

  /**
   * Cierra el popup de categorías.
   */
  closeCategoryPopup(): void {
    this.isCategoryPopupOpenSubject.next(false);
  }

  /**
   * Alterna el estado (abierto/cerrado).
   */
  toggleCategoryPopup(): void {
    const currentState = this.isCategoryPopupOpenSubject.value;
    this.isCategoryPopupOpenSubject.next(!currentState);
  }
}