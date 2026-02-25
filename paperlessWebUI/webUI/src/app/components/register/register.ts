import { Component } from '@angular/core';
import { AuthService } from '../../auth.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: true,
  templateUrl: './register.html',
  imports: [
    FormsModule
  ],
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  username = '';
  password = '';

  constructor(private auth: AuthService) {}

  register() {
    this.auth.register(this.username, this.password).subscribe({ next: res => { console.log('registered', res); window.location.href = '/'; }, error: err => { console.error('register failed', err); alert('Register failed'); } });
  }
}
