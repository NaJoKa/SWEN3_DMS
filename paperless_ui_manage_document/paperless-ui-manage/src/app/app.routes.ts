import { Routes } from "@angular/router";
import { DocumentDetailComponent } from "./document-detail/document-detail.component";

export const routes: Routes = [
  { path: "documents/:id", component: DocumentDetailComponent },
  { path: "", redirectTo: "documents/1", pathMatch: "full" },
  { path: "**", redirectTo: "" },
];
