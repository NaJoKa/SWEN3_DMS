import { Component } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, MatProgressBarModule, MatButtonModule, MatIconModule],
  templateUrl: './upload.html',
  styleUrls: ['./upload.css'],
})
export class Upload {
  fileName = '';
  uploadProgress = 0;
  uploading = false;
  //uploadSub: any;
  uploadSub?: Subscription;

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
    const upload$ = this.http
      .post('/api/upload', formData, {
        reportProgress: true,
        observe: 'events',
      })
      .pipe(finalize(() => this.reset()));

    this.uploadSub = upload$.subscribe(event => {
      if (event.type === HttpEventType.UploadProgress) {
        this.uploadProgress = Math.round(
          (100 * (event.loaded / (event.total ?? 1)))
        );
      } else if (event.type === HttpEventType.Response) {
        console.log('Upload complete!', event.body);
      }
    });
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
  }
}
