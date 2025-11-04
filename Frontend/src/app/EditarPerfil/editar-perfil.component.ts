import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-editar-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-perfil.component.html',
  styleUrls: ['./editar-perfil.component.css']
})
export class EditarPerfilComponent {
  avatarUrl = 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';

  setAvatar(src: string) {
    this.avatarUrl = src;
    const selector = document.getElementById('avatarSelector');
    if (selector) selector.style.display = 'none';
  }

  toggleAvatars() {
    const selector = document.getElementById('avatarSelector');
    if (selector) {
      selector.style.display = selector.style.display === 'flex' ? 'none' : 'flex';
    }
  }
}
