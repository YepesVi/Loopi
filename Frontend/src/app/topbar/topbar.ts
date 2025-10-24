import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-topbar',
  standalone: true, // ⚡ importante si estás usando imports en lugar de módulos
  imports: [CommonModule, FormsModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.css']
})
export class Topbar {
  userName = 'Sara';
  searchQuery = '';
  cartCount = 2; // número de productos en el carrito

  constructor(private router: Router) {}

  searchProduct() {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
    }
  }

  goToCart() {
    console.log('Ir al carrito');
  }

  goToProfile() {
    console.log('Ir al perfil');
  }

  goToOrders() {
    console.log('Ir a pedidos');
  }

  logout() {
    console.log('Cerrando sesión...');
  }
}
