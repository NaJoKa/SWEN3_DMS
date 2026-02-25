import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class Navbar implements OnInit {
  isLoggedIn = false;
  username: string | null = null;

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => {
      this.isLoggedIn = !!u;
      this.username = u ? u.username : null;
    });
  }

  logout() {
    this.auth.logout();
    // optional: navigate to home
    window.location.href = '/';
  }
}
