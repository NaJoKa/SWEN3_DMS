import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth.service';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  username = '';
  password = '';

  constructor(private auth: AuthService) {}

  login() {
    // TEMP debug: log values to verify ngModel binding (remove in production!)
    console.log('LOGIN attempt', { username: this.username, password: this.password ? '***present***' : '***empty***' });
    this.auth.login(this.username, this.password).subscribe({ next: res => { console.log('logged in', res); window.location.href = '/'; }, error: err => { console.error('login failed', err); alert('Login failed'); } });
  }
}
