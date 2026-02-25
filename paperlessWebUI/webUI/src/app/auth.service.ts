import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // use full backend URL to avoid dev-server returning index.html for API calls
  private base = 'http://localhost:8080/api/auth';

  // BehaviorSubject holds current user (null when logged out). Persist minimal user to localStorage.
  private currentUserSubject = new BehaviorSubject<any | null>(this.loadFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  private loadFromStorage(): any | null {
    try {
      const raw = localStorage.getItem('currentUser');
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  private saveToStorage(user: any | null) {
    try {
      if (user) localStorage.setItem('currentUser', JSON.stringify(user));
      else localStorage.removeItem('currentUser');
    } catch (e) { /* ignore */ }
  }

  register(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.base}/register`, { username, password }).pipe(
      tap(res => {
        if (res) {
          // backend now returns AuthResponse (id, username)
          const userObj = res && res.username ? { username: res.username } : (res.user ? { username: res.user.username } : { username });
          this.currentUserSubject.next(userObj);
          this.saveToStorage(userObj);
        }
      })
    );
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.base}/login`, { username, password }).pipe(
      tap(res => {
        if (res) {
          const userObj = res && res.username ? { username: res.username } : (res.user ? { username: res.user.username } : { username });
          this.currentUserSubject.next(userObj);
          this.saveToStorage(userObj);
        }
      })
    );
  }

  logout() {
    this.currentUserSubject.next(null);
    this.saveToStorage(null);
  }

  getCurrentUser(): any | null { return this.currentUserSubject.getValue(); }
  isLoggedIn(): boolean { return !!this.getCurrentUser(); }
}
