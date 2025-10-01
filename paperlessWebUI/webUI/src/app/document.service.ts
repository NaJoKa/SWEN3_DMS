import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface DocumentDto {
  id: number;          
  title?: string;
  summary?: string;
  ocrText?: string;    
}

export interface MetadataDto {
  [k: string]: any;
}

@Injectable({ providedIn: "root" })
export class DocumentService {
  constructor(private http: HttpClient) {}

  // Backend ist plural:
  private base = "/api/documents";

  get(id: number): Observable<DocumentDto> {
    return this.http.get<DocumentDto>(`${this.base}/${id}`);
  }

  update(id: number, payload: DocumentDto): Observable<DocumentDto> {
    return this.http.put<DocumentDto>(`${this.base}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // Es gibt keinen /metadata-Endpoint.
  // Wenn du trotzdem einen Button hast, zeigen wir zur Not das Dokument als "Metadata".
  metadata(id: number): Observable<MetadataDto> {
    return this.get(id) as unknown as Observable<MetadataDto>;
  }
}
