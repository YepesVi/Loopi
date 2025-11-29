import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './footer.html',
  styleUrls: ['./footer.css']
})
export class Footer {

  constructor(private router: Router) {}

  home(): void {
    this.router.navigateByUrl('/home');
  }

  about(): void {
    this.router.navigateByUrl('/about');
  }
  
  
  }

