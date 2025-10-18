import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Observable} from 'rxjs';
import {DocumentService} from '../../document-service';
import {AsyncPipe, DatePipe, NgForOf} from '@angular/common';

@Component({
  selector: 'app-documents',
  imports: [
    AsyncPipe,
    DatePipe
  ],
  templateUrl: './documents.html',
  styleUrl: './documents.css'
})
export class Documents implements OnInit{
  @Output() settingsEvent = new EventEmitter<void>();

  emitSettingsEvent() {
    this.settingsEvent.emit();
  }

  documents$ = new Observable<any[]>;  // Observable for documents

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    // Fetch documents as an Observable
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
