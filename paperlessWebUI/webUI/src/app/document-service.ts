import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  //private apiUrl = 'https://petstore3.swagger.io/api/v3/document'; // Adjust URL to match your API
  //private apiUrl = 'http://localhost:8080/documents';
  private apiUrl = '/rest-api/documents';
  private readonly ES_URL = 'http://localhost:9200/documents/_search?query=${query}';
  constructor(private http: HttpClient) { }

  // Fetch all documents
  getDocuments(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getDocumentsforSearch(): Observable<any[]> {
    return this.http.post<any>(this.ES_URL, {
      query: { match_all: {} },
      size: 50
    }).pipe(map(res => res.hits.hits.map((h: any) => h._source)));
  }

  // Fetch a document by ID
  getDocumentById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  searchDocuments(query: string): Observable<any[]> {
    const body = {
      query: {
        bool: {
          should: [
            {
              multi_match: {
                query,
                fields: ['title', 'summary', 'correspondent', 'documentType'],
                fuzziness: 'AUTO' //fuzzysuche
              }
            }
            ]
        }
      }
    };

    return this.http.post<any>(this.ES_URL, body).pipe(
      map(res => res.hits.hits.map((h: any) => h._source))
    );
  }
}
