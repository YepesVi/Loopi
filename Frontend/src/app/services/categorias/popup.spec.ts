import { TestBed } from '@angular/core/testing';
import { PopupService } from './popup';
import { take } from 'rxjs/operators';

describe('PopupService', () => {
  let service: PopupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PopupService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería inicializarse con el popup cerrado (false)', (done) => {
    service.isCategoryPopupOpen$.subscribe(isOpen => {
      expect(isOpen).toBeFalse();
      done();
    });
  });

  it('debería abrir el popup', (done) => {
    service.openCategoryPopup();
    service.isCategoryPopupOpen$.subscribe(isOpen => {
      expect(isOpen).toBeTrue();
      done();
    });
  });

  it('debería cerrar el popup', (done) => {
    // Primero lo abrimos para asegurar cambio de estado
    service.openCategoryPopup();
    service.closeCategoryPopup();
    
    service.isCategoryPopupOpen$.subscribe(isOpen => {
      expect(isOpen).toBeFalse();
      done();
    });
  });

  it('debería alternar (toggle) el estado del popup', (done) => {
    // 1. Estado inicial es false. Hacemos el primer Toggle -> Debe pasar a TRUE
    service.toggleCategoryPopup();

    // Verificamos estado 1
    service.isCategoryPopupOpen$.pipe(take(1)).subscribe(isOpen => {
      expect(isOpen).toBeTrue();

      // 2. Hacemos el segundo Toggle -> Debe pasar a FALSE
      service.toggleCategoryPopup();

      // Verificamos estado 2
      service.isCategoryPopupOpen$.pipe(take(1)).subscribe(isClosed => {
        expect(isClosed).toBeFalse();
        done(); // Finaliza el test exitosamente
      });
    });
  });
});