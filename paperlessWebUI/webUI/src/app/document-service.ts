import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  //private apiUrl = 'https://petstore3.swagger.io/api/v3/document'; // Adjust URL to match your API
  //private apiUrl = 'http://localhost:8080/documents';
  private apiUrl = '/rest-api/documents';
  constructor(private http: HttpClient) { }

  // Fetch all documents
  getDocuments(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Fetch a document by ID
  getDocumentById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }
}
