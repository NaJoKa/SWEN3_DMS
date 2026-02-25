import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {DocumentDto} from './document.service';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  //private apiUrl = 'https://petstore3.swagger.io/api/v3/document'; // Adjust URL to match your API
  private apiUrl = 'http://localhost:8080/documents';
  //private apiUrl = '/rest-api/documents';
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
    return this.get(Number(id));
  }

  get(id: number): Observable<DocumentDto> {
    return this.http.get<DocumentDto>(`${this.apiUrl}/${id}`);
  }

  update(id: number, payload: DocumentDto): Observable<DocumentDto> {
    return this.http.put<DocumentDto>(`${this.apiUrl}/${id}`, payload);
  }

  // compatibility alias used by components that import `document-service.ts`
  updateDocument(id: string | number, payload: any): Observable<DocumentDto> {
    return this.update(Number(id), payload as DocumentDto);
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

  searchDocumentsPage(query: string, page: number = 0, size: number = 10): Observable<{content: any[]; totalElements: number; totalPages: number; page: number; size: number}> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', String(page))
      .set('size', String(size));

    return this.http.get<any>(this.apiUrl + '/search', {params}).pipe(
      map(res => {
        // support Spring's Page JSON (number) and other possible shapes
        const content = res.content ?? (res.hits?.hits ? res.hits.hits.map((h: any) => h._source) : []);
        const totalElements = res.totalElements ?? res.total_elements ?? res.total ?? 0;
        const totalPages = res.totalPages ?? res.total_pages ?? Math.ceil((totalElements || 0) / (res.size ?? size));
        const pageNumber = (res.page ?? res.number ?? 0);
        const pageSize = res.size ?? size;
        return {content, totalElements, totalPages, page: pageNumber, size: pageSize};
      })
    );
  }
}
