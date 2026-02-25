import {Component, EventEmitter, OnInit, Output, ViewChild, ElementRef, AfterViewChecked} from '@angular/core';
import {catchError, Observable, of} from 'rxjs';
import {DocumentService} from '../../document-service';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {DocumentEdit} from '../document-edit/document-edit';
import {SettingsPanel} from '../settings-panel/settings-panel';
import { AuthService } from '../../auth.service';

interface DocumentPage {
  content: any[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Component({
  selector: 'app-documents',
  imports: [
    CommonModule,
    FormsModule,
    DocumentEdit,
    SettingsPanel,
  ],
  templateUrl: './documents.html',
  styleUrls: ['./documents.css']
})
export class Documents implements OnInit, AfterViewChecked{
  @Output() settingsEvent = new EventEmitter<void>();

  // documents$ is the single observable the template should use for non-paged lists
  documents$: Observable<any[]> = of([]);
  // paged result
  pageData: DocumentPage | null = null;

  searchQuery = '';
  isSearching = false;

  // paging defaults
  currentPage = 0;
  pageSize = 10;

  constructor(private documentService: DocumentService, private authService: AuthService) {}

  @ViewChild('editorContainer') editorContainer?: ElementRef<HTMLElement>;
  private lastScrolledForId: string | number | null = null;

  // currently editing document id
  editingId: string | number | null = null;

  // show/hide settings panel
  showSettings = false;

  // auth state
  isLoggedIn = false;
  username: string | null = null;

  ngOnInit(): void {
    // Fetch documents as an Observable (initial non-paged load)
    this.loadAllDocuments();
    // subscribe to auth state
    this.authService.currentUser$.subscribe(u => {
      this.isLoggedIn = !!u;
      this.username = u ? u.username : null;
    });
  }

  loadAllDocuments() {
    this.pageData = null;
    this.documents$ = this.documentService.getDocuments().pipe(
      catchError(err => {
        console.error('Failed to load documents', err);
        return of([]);
      })
    );
  }

  // Navigate to the detail page when a document is clicked
  viewDocument(id: string | number): void {
    // open inline editor for this document
    this.editingId = id;
  }

  onEditorSaved() {
    // refresh list and close editor
    this.loadAllDocuments();
    this.editingId = null;
  }

  onEditorClosed() {
    this.editingId = null;
  }

  searchDocuments(page: number = 0): void{
    if (!this.searchQuery || !this.searchQuery.trim()) {
      this.loadAllDocuments();
      return;
    }

    this.isSearching = true;
    this.currentPage = page;

    this.documentService.searchDocumentsPage(this.searchQuery, this.currentPage, this.pageSize).subscribe({
      next: (p) => {
        this.pageData = p;
        this.isSearching = false;
      },
      error: (err) => {
        console.error('Paged search failed', err);
        this.pageData = {content: [], totalElements: 0, totalPages: 0, page: 0, size: this.pageSize};
        this.isSearching = false;
      }
    });
  }

  // pagination controls
  prevPage(): void {
    if (this.pageData && this.pageData.page > 0) {
      this.searchDocuments(this.pageData.page - 1);
    }
  }

  nextPage(): void {
    if (this.pageData && this.pageData.page < (this.pageData.totalPages - 1)) {
      this.searchDocuments(this.pageData.page + 1);
    }
  }

  emitSettingsEvent() {
    // open settings panel
    this.showSettings = true;
  }

  onSettingsClosed() {
    this.showSettings = false;
  }

  logout() {
    this.authService.logout();
  }

  // scroll into view once after the editor is rendered
  ngAfterViewChecked(): void {
    if (this.editingId != null && this.editorContainer && this.lastScrolledForId !== this.editingId) {
      try {
        this.editorContainer.nativeElement.scrollIntoView({behavior: 'smooth', block: 'start'});
        this.lastScrolledForId = this.editingId;
      } catch (e) {
        // ignore
      }
    }
  }
}
