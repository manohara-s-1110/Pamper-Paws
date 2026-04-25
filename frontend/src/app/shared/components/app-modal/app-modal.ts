import { Component, HostListener, input, output } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="modal-shell" *ngIf="open()" (click)="close.emit()">
      <section class="modal-card" [class.compact]="compact()" (click)="$event.stopPropagation()">
        <header class="modal-header">
          <div>
            <span class="eyebrow" *ngIf="eyebrow()">{{ eyebrow() }}</span>
            <h2>{{ title() }}</h2>
          </div>
          <button class="modal-close" type="button" (click)="close.emit()" aria-label="Close dialog">Close</button>
        </header>

        <div class="modal-body">
          <ng-content></ng-content>
        </div>
      </section>
    </div>
  `,
})
export class AppModalComponent {
  readonly open = input(false);
  readonly title = input('');
  readonly eyebrow = input('');
  readonly compact = input(false);
  readonly close = output<void>();

  @HostListener('document:keydown.escape')
  handleEscape() {
    if (this.open()) {
      this.close.emit();
    }
  }
}
