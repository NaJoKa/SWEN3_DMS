import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DocumentService {
    private readonly ES_URL = 'http://localhost:9200/documents/_search?query=${query}';

    constructor(private http: HttpClient) {}

    getDocuments(): Observable<any[]> {
        return this.http.post<any>(this.ES_URL, {
            query: { match_all: {} },
            size: 50
        }).pipe(map(res => res.hits.hits.map((h: any) => h._source)));
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
