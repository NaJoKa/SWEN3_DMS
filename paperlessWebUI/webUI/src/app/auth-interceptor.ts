import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    try {
      const raw = localStorage.getItem('currentUser');
      if (raw) {
        const parsed = JSON.parse(raw);
        const token = parsed && parsed.token ? parsed.token : null;
        console.debug('AuthInterceptor: parsed currentUser from storage', parsed);
        if (token) {
          console.debug('AuthInterceptor: attaching token to request', token);
          const cloned = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
          return next.handle(cloned);
        } else {
          console.debug('AuthInterceptor: no token found in currentUser');
        }
      } else {
        console.debug('AuthInterceptor: no currentUser in localStorage');
      }
    } catch (e) {
      console.error('AuthInterceptor: error parsing currentUser', e);
    }
    return next.handle(req);
  }
}
