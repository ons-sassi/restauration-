import { Component, Input } from '@angular/core';
import { SpinnerComponent } from '../spinner/spinner.component';
export interface TableColumn {
  key: string;
  label: string;
  width?: string;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.css',
  imports: [SpinnerComponent],
})
export class DataTableComponent {
  @Input() columns: TableColumn[] = [];

  @Input() data: Record<string, unknown>[] = [];

  @Input() loading = false;

  protected getValue(row: Record<string, unknown>, key: string): unknown {
    return row[key];
  }
}
