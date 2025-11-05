import { Component, signal, computed } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { Topbar } from './topbar/topbar';
import { Footer } from './footer/footer';
import { PopupService } from './services/categorias/popup';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { CategoryList } from './components/categorias/category-list/category-list';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Topbar, Footer, CommonModule, CategoryList],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  isCategoryPopupOpen$!: Observable<boolean>;

  // Signal reactivo que se actualiza con cada navegación
  private readonly currentRoute = signal('');

  // Computed para ocultar layout en rutas específicas
  readonly hideLayout = computed(() => {
    const hiddenRoutes = ['/login-register', '/reset-password'];
    return hiddenRoutes.includes(this.currentRoute());
  });

  constructor(private popupService: PopupService, private router: Router) {}

  ngOnInit(): void {
    this.isCategoryPopupOpen$ = this.popupService.isCategoryPopupOpen$;

    // Actualiza la ruta actual cuando cambia
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.currentRoute.set(event.urlAfterRedirects);
      });
  }

  closePopup(): void {
    this.popupService.closeCategoryPopup();
  }
}
