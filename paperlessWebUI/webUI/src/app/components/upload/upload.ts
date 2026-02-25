import { Component } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {Subscription, interval, of} from 'rxjs';
import {switchMap, takeWhile, catchError, startWith, tap} from 'rxjs/operators';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './upload.html',
  styleUrl: './upload.css'
})
export class Upload {
  fileName = '';
  uploadProgress = 0;
  uploading = false;
  //uploadSub: any;
  uploadSub?: Subscription;
  fileUpload: any;
  // latest OCR summary (null until server provides it)
  summary: string | null = null;
  // Polling for OCR
  polling = false;
  pollIntervalMs = 2000; // 2 seconds
  maxPollAttempts = 90; // ~3 minutes
  private pollTimerId?: any;
  private pollSubscription?: Subscription;
  private pollAttempts = 0;

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    this.fileName = file.name;

    const formData = new FormData();
    formData.append('file', file);

    this.uploading = true;

    //Sending the Upload request
    // im container -> /rest-api/documents, sonst -> http://localhost:8080/documents
    const upload$ = this.http.post('http://localhost:8080/documents', formData, {
      reportProgress: true,
      observe: 'events'
    })
      .pipe(finalize(() => this.reset()));

    this.uploadSub = upload$.subscribe(event => {
      if (event.type === HttpEventType.UploadProgress) {
        this.uploadProgress = Math.round(
          (100 * (event.loaded / (event.total ?? 1)))
        );
      } else if (event.type === HttpEventType.Response) {
        const message = event.body;
        console.log('Upload complete!', event.body);
        // If server returned a Document object, extract id and start polling
        try {
          const doc: any = message;
          const id = doc?.id ?? (doc && doc['id']);
          if (id != null) {
            // start polling for OCR result
            console.log('Document id parsed from upload response:', id);
            // clear previous summary and start polling
            this.summary = null;
            this.startPollingForOcr(id);
            // immediate single-shot GET to verify endpoint reachability and get an immediate log
            const url = `http://localhost:8080/documents/${id}`;
            this.http.get<any>(url).pipe(
              catchError(err => {
                console.warn('Immediate GET error (ignored):', err);
                return of(null);
              })
            ).subscribe(res => {
              console.log('Immediate GET response for id', id, res);
              // if immediate response already contains summary/ocrText, set it
              const s = res?.summary ?? res?.ocrText ?? null;
              if (s) {
                this.summary = s;
                this.stopPolling();
              }
            });
          } else {
            // fallback: put raw message into textarea
            const textarea = document.getElementById('documentSummary') as HTMLTextAreaElement;
            if (textarea && message) {
              textarea.value = message.toString();
            }
          }
        } catch (e) {
          console.error('Failed to parse upload response', e);
        }
      }
    });
  }

  private startPollingForOcr(id: number | string) {
    if (this.polling) return;
    this.polling = true;
    this.pollAttempts = 0;
    const url = `http://localhost:8080/documents/${id}`;

    console.log('Starting OCR polling for id', id, 'every', this.pollIntervalMs, 'ms');

    // Use RxJS interval + switchMap to poll the HTTP endpoint. This avoids deprecated patterns
    // and keeps a single subscription we can unsubscribe later.
    this.pollSubscription = interval(this.pollIntervalMs).pipe(
      // emit immediately then every interval
      startWith(0),
      // log each tick so we know the pipeline is firing
      tap(() => console.log('poll tick for id', id)),
      // stop when attempts exceed max
      takeWhile(() => this.pollAttempts < this.maxPollAttempts),
      switchMap(() =>
        this.http.get<any>(url).pipe(
          catchError(err => {
            console.warn('Polling HTTP error (ignored):', err);
            return of(null);
          })
        )
      )
    ).subscribe({
      next: d => {
        this.pollAttempts++;
        console.log('Polling attempt', this.pollAttempts, 'for document id', id, 'response=', d);
        const s = d?.summary ?? d?.ocrText ?? null;
        if (s) {
          this.summary = s;
          this.stopPolling();
        } else if (this.pollAttempts >= this.maxPollAttempts) {
          console.warn('OCR polling timed out for id', id);
          this.stopPolling();
        }
      },
      error: err => {
        console.error('Polling stream error', err);
        this.stopPolling();
      },
      complete: () => {
        console.log('Polling completed for id', id);
      }
    });

    console.log('pollSubscription created:', !!this.pollSubscription);
  }

   private stopPolling() {
     this.polling = false;
    if (this.pollTimerId) {
      clearInterval(this.pollTimerId);
      this.pollTimerId = undefined;
    }
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = undefined;
    }
     // uploading state remains false after reset is called by finalize
   }

  /*cancelUpload() {
    this.uploadSub?.unsubscribe();
    this.reset();
  }*/

  private reset() {
    this.uploading = false;
    this.uploadProgress = 0;
    this.uploadSub = undefined;
    this.fileName = '';
    this.summary = null;
   }
}
