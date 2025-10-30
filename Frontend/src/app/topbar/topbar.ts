import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PopupService } from '../services/popup';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.css']
})
export class Topbar {


  constructor(private popupService: PopupService) { }

  // Llama al servicio para abrir/cerrar el popup
  toggleCategoryPopup(): void {
    this.popupService.toggleCategoryPopup();
  }
  
  searchQuery = '';

  searchProduct() {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
      // Aquí podrías navegar a una página de resultados
    }
  }
}
