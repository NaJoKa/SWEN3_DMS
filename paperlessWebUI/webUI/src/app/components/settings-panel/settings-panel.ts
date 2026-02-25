import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SettingsService } from '../../settings.service';

@Component({
  selector: 'app-settings-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings-panel.html',
  styleUrls: ['./settings-panel.css']
})
export class SettingsPanel implements OnInit {
  @Output() closed = new EventEmitter<void>();
  models: string[] = [];
  selected: string | null = null;
  loading = false;
  saving = false;

  constructor(private settingsService: SettingsService) {}

  ngOnInit(): void {
    this.loading = true;
    this.settingsService.getModels().subscribe({ next: m => { this.models = m; this.loadCurrent(); }, error: () => { this.loading = false; } });
  }

  loadCurrent() {
    this.settingsService.getCurrentModel().subscribe({ next: res => { this.selected = res.model || null; this.loading = false; }, error: () => { this.loading = false; } });
  }

  save() {
    if (!this.selected) return;
    this.saving = true;
    this.settingsService.setModel(this.selected).subscribe({ next: () => { this.saving = false; this.closed.emit(); }, error: () => { this.saving = false; } });
  }

  close() { this.closed.emit(); }
}
