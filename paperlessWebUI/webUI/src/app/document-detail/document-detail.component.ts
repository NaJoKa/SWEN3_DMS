import { Component, OnInit, inject } from "@angular/core";
import { ActivatedRoute, Router} from "@angular/router";
import { CommonModule, Location } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { DocumentService, DocumentDto, MetadataDto } from "../document.service";

@Component({
  selector: "app-document-detail",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./document-detail.component.html",
  styleUrl: "./document-detail.component.css",
})
export class DocumentDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);
  private ds = inject(DocumentService);

  doc?: DocumentDto;
  editing?: DocumentDto;
  meta?: MetadataDto;

  loading = true;
  saving = false;
  deleting = false;
  msg?: string;
  error?: string;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get("id")!); // als Zahl lesen
    this.ds.get(id).subscribe({
      next: (d) => {
        this.doc = d;
        this.editing = { ...d };
        this.loading = false;
      },
      error: (_) => {
        this.error = "Dokument nicht gefunden.";
        this.loading = false;
      },
    });
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if (!this.doc || !this.editing) return;
    this.saving = true;
    this.msg = this.error = undefined;
    console.log("save() called with", this.editing); // <— sichtbar im Console-Tab

    this.ds.update(this.doc.id, this.editing).subscribe({
      next: (d) => {
        console.log("PUT response", d); // <—
        this.doc = d;
        this.editing = { ...d };
        this.msg = "Gespeichert.";
        this.saving = false;
      },
      error: (err) => {
        console.error("PUT failed", err); // <—
        this.error = "Update fehlgeschlagen.";
        this.saving = false;
      },
    });
  }

  del(): void {
    if (!this.doc) return;
    if (!confirm("Wirklich löschen?")) return;
    this.deleting = true;
    this.error = undefined;
    this.ds.delete(this.doc.id).subscribe({
      next: (_) => this.location.back(),
      error: (_) => {
        this.error = "Löschen fehlgeschlagen.";
        this.deleting = false;
      },
    });
  }

  loadMetadata(): void {
    if (!this.doc) return;
    this.meta = undefined;
    this.error = undefined;
    this.ds.metadata(this.doc.id).subscribe({
      next: (m) => (this.meta = m),
      error: (_) => (this.error = "Metadaten konnten nicht geladen werden."),
    });
  }
}
