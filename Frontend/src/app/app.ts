import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Topbar } from './topbar/topbar';
import { Footer } from './footer/footer';
import { PopupService } from './services/popup';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { CategoryList } from './components/categorias/category-list/category-list';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Topbar, Footer, CommonModule, CategoryList],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');

  // Observable que contiene el estado de visibilidad
  isCategoryPopupOpen$!: Observable<boolean>; 

  constructor(private popupService: PopupService) {}

  ngOnInit(): void {
    // Asignamos el Observable del servicio
    this.isCategoryPopupOpen$ = this.popupService.isCategoryPopupOpen$;
  }

  // Método para cerrar el popup desde dentro (ej. haciendo clic en un botón "Cerrar")
  closePopup(): void {
    this.popupService.closeCategoryPopup();
  }
}

