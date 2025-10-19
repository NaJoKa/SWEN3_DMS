import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Observable, of} from 'rxjs';
import {DocumentService} from '../../document-service';
import {AsyncPipe, DatePipe, NgForOf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {catchError} from 'rxjs/operators';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    FormsModule,
    NgForOf
  ],
  templateUrl: './documents.html',
  styleUrl: './documents.css'
})
export class Documents implements OnInit{
  @Output() settingsEvent = new EventEmitter<void>();

  documents$: Observable<any[]> = of([]);
  searchQuery = '';
  isSearching = false;  // Observable for documents

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    // Fetch documents as an Observable
    //this.documents$ = this.documentService.getDocuments();
    this.loadAllDocuments();
  }

  loadAllDocuments() {
    this.documents$ = this.documentService.getDocuments();
  }

  searchDocuments(): void{
    if (!this.searchQuery.trim()) {
      this.loadAllDocuments();
      return;
    }
    this.isSearching = true;
    this.documents$ = this.documentService.searchDocuments(this.searchQuery).pipe(
      catchError((err) => {
        console.error('Search failed', err);
        this.isSearching = false;
        return of([]);
      })
    );

    this.documents$.subscribe(() => (this.isSearching = false));
  }

  emitSettingsEvent() {
    this.settingsEvent.emit();
  }
  // Navigate to the detail page when a document is clicked
  viewDocument(id: string): void {
    // In a real-world scenario, you might navigate to a different page with more details
    // For example:
    // this.router.navigate([`/document/${id}`]);
    console.log(`View document with ID: ${id}`);
  }
}
