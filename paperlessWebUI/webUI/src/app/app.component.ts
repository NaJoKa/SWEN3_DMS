import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FormsModule, HttpClientModule, FormsModule, CommonModule],

  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'webUI';
  title2 = 'searchDocumentApp';
  query = '';
  results: any[] = [];

  private URL = 'http://localhost:4200/documents/search'
  constructor(private http: HttpClient) {}

  search(): void {
    if (!this.query.trim()) {
      this.results = [];
      return;
    }

    const body = {
      query: {
        bool: {
          should: [
            { match: { title: { query: this.query, fuzziness: 'AUTO' } } },
            { match: { content: { query: this.query, fuzziness: 'AUTO' } } }
          ]
        }
      }
    };

    this.http.post(this.URL, body).subscribe((res: any) => {
      this.results = res.hits.hits;
    });
  }
}
