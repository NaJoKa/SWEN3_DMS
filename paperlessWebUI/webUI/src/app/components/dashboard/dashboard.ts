import { Component, AfterViewInit, Output, EventEmitter } from '@angular/core';
@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  @Output() settingsEvent = new EventEmitter<void>();

  ngAfterViewInit(): void {
    this.initializeCharts();
  }

  initializeCharts() {
    this.createSalesChart();
    this.createPerformanceChart();
  }

  createSalesChart() {
    const ctx = (document.getElementById('salesChart') as HTMLCanvasElement).getContext('2d');
    if (ctx) {
    }
  }

  createPerformanceChart() {
    const ctx = (document.getElementById('performanceChart') as HTMLCanvasElement).getContext('2d');
    if (ctx) {
    }
  }

  emitSettingsEvent() {
    this.settingsEvent.emit();
  }
}
