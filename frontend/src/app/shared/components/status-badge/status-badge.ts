import { Component, computed, input } from '@angular/core';

import { AppointmentStatus } from '../../../utils/appointment-ui';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span class="status-badge" [class.completed]="tone() === 'completed'" [class.today]="tone() === 'today'" [class.upcoming]="tone() === 'upcoming'">
      {{ status() }}
    </span>
  `,
})
export class StatusBadgeComponent {
  readonly status = input.required<AppointmentStatus>();

  readonly tone = computed(() => {
    switch (this.status()) {
      case 'Completed':
        return 'completed';
      case 'Today':
        return 'today';
      default:
        return 'upcoming';
    }
  });
}
