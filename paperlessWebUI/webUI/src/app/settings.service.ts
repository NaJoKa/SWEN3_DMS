import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private base = 'http://localhost:8080/api/settings';
  constructor(private http: HttpClient) {}

  getModels(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/models`);
  }

  getCurrentModel(): Observable<{ model: string }> {
    return this.http.get<{ model: string }>(`${this.base}/model`);
  }

  setModel(model: string): Observable<any> {
    return this.http.post<any>(`${this.base}/model`, { model });
  }
}
