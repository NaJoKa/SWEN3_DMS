import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Observable, of} from 'rxjs';
import {DocumentService} from '../../document-service';
import {AsyncPipe, DatePipe, NgForOf} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    FormsModule,
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
    this.loadAllDocuments();
  }

  loadAllDocuments() {
    this.documents$ = this.documentService.getDocuments();
  }

  // Navigate to the detail page when a document is clicked
  viewDocument(id: string): void {
    // In a real-world scenario, you might navigate to a different page with more details
    // For example:
    // this.router.navigate([`/document/${id}`]);
    console.log(`View document with ID: ${id}`);
  }
}
