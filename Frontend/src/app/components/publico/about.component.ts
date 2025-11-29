import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent {
  version = '1.0.0';

  mision = 'Empoderar a estudiantes y jóvenes en Colombia mediante una aplicación web segura para productos de segunda mano, fomentando confianza, sostenibilidad y economía circular (ODS 12).';

  pregunta = '¿Cómo enfrentar la fragmentación e informalidad del mercado de segunda mano en Colombia?';

  justificacion = 'Loopi busca consolidar un mercado confiable y accesible, reduciendo el desperdicio y mejorando la experiencia de compraventa. La plataforma ofrece funcionalidades clave como publicación, filtros, mensajería segura y administración, optimizando procesos y reduciendo riesgos.';

  alcance = [
    'Registro, login y gestión de perfil',
    'Publicación, edición y eliminación de productos con fotos y categorías',
    'Listados, filtros de búsqueda y categorías',
    'Carrito temporal y lista de favoritos',
    'Historial de publicaciones y compras',
    'Notificaciones por correo electrónico',
    'Panel de administración para usuarios y productos',
    'Seguridad básica: contraseñas encriptadas y HTTPS'
  ];

  objetivos = [
    'Implementar una aplicación web con herramientas libres para segunda mano en Colombia',
    'Analizar requerimientos del mercado y documentar necesidades',
    'Diseñar funcionalidades CRUD, filtros y mensajería interna',
    'Desarrollar backend y frontend con herramientas libres',
    'Validar funcionamiento mediante pruebas'
  ];

  roles = [
    {
      titulo: 'Desarrollador Frontend',
      integrantes: [
        { nombre: 'Sara Carolina Sánchez Arroyave', correo: 'sara.sanchez@elpoli.edu.co' }
      ]
    },
    {
      titulo: 'Desarrollador Backend',
      integrantes: [
        { nombre: 'Derinson Andres Valencia', correo: 'derinson.valencia@elpoli.edu.co' }
      ]
    },
    {
      titulo: 'Desarrollador Full-stack',
      integrantes: [
        { nombre: 'Daniel Felipe Venegas Sarmiento', correo: 'daniel.venegas@elpoli.edu.co' }
      ]
    },
    {
      titulo: 'Ingeniero DevOps',
      integrantes: [
        { nombre: 'Andrés Yepes Villada', correo: 'andres.yepes@elpoli.edu.co' }
      ]
    },
    {
      titulo: 'Ingeniera QA / Tester',
      integrantes: [
        { nombre: 'Yarledy Zapata Tabares', correo: 'yarledy.zapata@elpoli.edu.co' }
      ]
    }
  ];
}
