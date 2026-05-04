import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Customer, Vet, VetLeave, Visit } from '../../../models/app.models';
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
  readonly leaves = signal<VetLeave[]>([]);
  readonly errorMessage = signal('');
  readonly statusMessage = signal('');
  readonly leaveSaving = signal(false);

  readonly leaveForm = this.fb.nonNullable.group({
    date: ['', Validators.required],
  });

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
    if (!this.vet() || this.leaveForm.invalid || this.leaveSaving()) {
      this.leaveForm.markAllAsTouched();
      this.errorMessage.set('Choose a leave date first.');
      return;
    }

    this.leaveSaving.set(true);
    this.errorMessage.set('');
    this.statusMessage.set('');

    this.vets.addLeave(this.vet()!.id, this.leaveForm.controls.date.value).subscribe({
      next: () => {
        this.leaveSaving.set(false);
        this.statusMessage.set('Leave marked successfully.');
        this.leaveForm.reset({ date: '' });
        this.loadLeaves(this.vet()!.id);
      },
      error: (error) => {
        this.leaveSaving.set(false);
        this.errorMessage.set(error?.error?.message ?? 'Unable to mark leave right now.');
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
        this.loadLeaves(vet.id);
        forkJoin({
          appointments: this.visits.getVisitsByVet(vet.id),
          customers: this.customers.getAllCustomers(),
        }).subscribe({
          next: ({ appointments, customers }) => {
            this.appointments.set(appointments);
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

  private loadLeaves(vetId: number) {
    this.vets.getLeaves(vetId).subscribe({
      next: (leaves) => this.leaves.set(leaves),
      error: () => this.leaves.set([]),
    });
  }
}
