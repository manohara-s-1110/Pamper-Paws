import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Customer, Vet, Visit } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';
import { VisitDataService } from '../../../services/visit-data';
import { appointmentDateTimeValue, getAppointmentStatus } from '../../../utils/appointment-ui';

@Component({
  selector: 'app-vet-dashboard',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, ReactiveFormsModule],
  templateUrl: './vet-dashboard.html',
})
export class VetDashboardComponent {
  private readonly auth = inject(SessionAuthService);
  private readonly fb = inject(FormBuilder);
  private readonly vets = inject(VetDataService);
  private readonly visits = inject(VisitDataService);
  private readonly customers = inject(CustomerDataService);

  readonly vet = signal<Vet | null>(null);
  readonly appointments = signal<Visit[]>([]);
  readonly customerDirectory = signal<Record<number, Customer>>({});
  readonly leaveDates = signal<string[]>([]);
  readonly leaveMessage = signal('');
  readonly errorMessage = signal('');

  readonly today = new Date().toISOString().split('T')[0];
  readonly todaysAppointments = computed(() =>
    this.appointments()
      .filter((visit) => visit.visitDate === this.today)
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right)),
  );
  readonly upcomingAppointments = computed(() =>
    this.appointments()
      .filter((visit) => getAppointmentStatus(visit) === 'Upcoming')
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right)),
  );
  readonly nextAppointment = computed(() =>
    [...this.appointments()]
      .filter((visit) => getAppointmentStatus(visit) !== 'Completed')
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right))[0] ?? null,
  );

  readonly leaveForm = this.fb.nonNullable.group({
    date: ['', Validators.required],
  });

  constructor() {
    this.loadData();
  }

  customerName(customerId: number) {
    return this.customerDirectory()[customerId]?.name ?? `Customer #${customerId}`;
  }

  customerPhone(customerId: number) {
    return this.customerDirectory()[customerId]?.phone ?? 'Not available';
  }

  appointmentSummary(visit: Visit | null) {
    if (!visit) {
      return 'No visit ready to review.';
    }

    return `${this.customerName(visit.customerId)} - ${visit.visitDate} at ${visit.timeSlot}`;
  }

  markLeave() {
    if (!this.vet()) {
      return;
    }

    if (this.leaveForm.invalid) {
      this.leaveForm.markAllAsTouched();
      this.errorMessage.set('Please fill all required fields');
      return;
    }

    this.errorMessage.set('');
    this.leaveMessage.set('');

    this.visits.markVetLeave(this.vet()!.id, this.leaveForm.controls.date.value).subscribe({
      next: (leave) => {
        this.leaveDates.update((dates) => [...new Set([...dates, leave.date])].sort());
        this.leaveForm.reset({ date: '' });
        this.leaveMessage.set('Leave marked successfully.');
      },
      error: (error) => {
        this.errorMessage.set(error?.error?.message ?? 'Unable to mark leave for that date.');
      },
    });
  }

  private loadData() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.vets.getVetByUsername(username).subscribe({
      next: (vet) => {
        this.vet.set(vet);
        forkJoin({
          appointments: this.visits.getVisitsByVet(vet.id),
          customers: this.customers.getAllCustomers(),
          leaves: this.visits.getVetLeaves(vet.id),
        }).subscribe({
          next: ({ appointments, customers, leaves }) => {
            this.appointments.set(appointments);
            this.leaveDates.set(leaves.map((leave) => leave.date));
            this.customerDirectory.set(
              customers.reduce<Record<number, Customer>>((accumulator, customer) => {
                accumulator[customer.id] = customer;
                return accumulator;
              }, {}),
            );
          },
          error: () => {
            this.errorMessage.set('Unable to load your appointment summary right now.');
          },
        });
      },
      error: () => {
        this.errorMessage.set('Unable to load your veterinary profile right now.');
      },
    });
  }
}
