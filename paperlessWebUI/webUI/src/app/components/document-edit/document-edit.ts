import {Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, ViewChild, ElementRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {DocumentService} from '../../document-service';

@Component({
  selector: 'app-document-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './document-edit.html',
  styleUrls: ['./document-edit.css']
})
export class DocumentEdit implements OnChanges {
  @Input() documentId: string | number | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  loading = false;
  saving = false;
  doc: any = null;
  @ViewChild('titleInput') titleInput?: ElementRef<HTMLInputElement>;

  constructor(private documentService: DocumentService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (this.documentId != null) this.load();
  }

  load() {
    if (this.documentId == null) return;
    this.loading = true;
    this.documentService.getDocumentById(String(this.documentId)).subscribe({
      next: (d: any) => {
        this.doc = {...d};
        this.loading = false;
        // give Angular a tick to render the input, then focus
        setTimeout(() => { try { this.titleInput?.nativeElement.focus(); } catch (e) { /* ignore */ } }, 0);
      },
      error: (e: any) => { console.error('Failed to load document', e); this.loading = false; }
    });
  }

  save() {
    if (!this.doc || this.documentId == null) return;
    this.saving = true;
    const payload = {
      title: this.doc.title,
      summary: this.doc.summary,
      correspondent: this.doc.correspondent,
      documentType: this.doc.documentType,
      storagePath: this.doc.storagePath,
      archiveSerialNumber: this.doc.archiveSerialNumber
    };
    this.documentService.updateDocument(this.documentId, payload).subscribe({
      next: () => { this.saving = false; this.saved.emit(); },
      error: (e: any) => { console.error('Failed to save', e); this.saving = false; }
    });
  }

  close() { this.closed.emit(); }
}
