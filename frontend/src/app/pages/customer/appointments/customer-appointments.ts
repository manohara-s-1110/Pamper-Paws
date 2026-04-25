import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AppModalComponent } from '../../../shared/components/app-modal/app-modal';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { Customer, Pet, Vet, Visit } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';
import { VisitDataService } from '../../../services/visit-data';
import {
  AppointmentFilter,
  appointmentDateTimeValue,
  getAppointmentStatus,
  isVisitInFilter,
} from '../../../utils/appointment-ui';

@Component({
  selector: 'app-customer-appointments',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf, DatePipe, AppModalComponent, StatusBadgeComponent],
  templateUrl: './customer-appointments.html',
})
export class CustomerAppointmentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);
  private readonly vets = inject(VetDataService);
  private readonly visits = inject(VisitDataService);

  readonly customer = signal<Customer | null>(null);
  readonly pets = signal<Pet[]>([]);
  readonly vetsList = signal<Vet[]>([]);
  readonly appointments = signal<Visit[]>([]);
  readonly timeSlots = signal<string[]>([]);
  readonly activeFilter = signal<AppointmentFilter>('all');
  readonly selectedAppointment = signal<Visit | null>(null);
  readonly selectedPetId = signal<number | null>(null);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);
  readonly cancellingAppointmentId = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    petId: ['', Validators.required],
    vetId: ['', Validators.required],
    visitDate: ['', Validators.required],
    timeSlot: ['', Validators.required],
    reason: ['', Validators.required],
  });

  readonly filteredAppointments = computed(() =>
    [...this.appointments()].sort((left, right) =>
      appointmentDateTimeValue(left) - appointmentDateTimeValue(right),
    ),
  );

  readonly visibleAppointments = computed(() =>
    this.filteredAppointments().filter((visit) => isVisitInFilter(visit, this.activeFilter())),
  );

  readonly nextAppointment = computed(() =>
    [...this.appointments()]
      .filter((visit) => appointmentDateTimeValue(visit) >= Date.now() && getAppointmentStatus(visit) !== 'Completed')
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right))[0] ?? null,
  );

  readonly todayCount = computed(() =>
    this.filteredAppointments().filter((visit) => getAppointmentStatus(visit) === 'Today').length,
  );

  readonly upcomingCount = computed(() =>
    this.filteredAppointments().filter((visit) => getAppointmentStatus(visit) === 'Upcoming').length,
  );

  readonly filters: { label: string; value: AppointmentFilter }[] = [
    { label: 'All', value: 'all' },
    { label: 'Today', value: 'today' },
    { label: 'Upcoming', value: 'upcoming' },
    { label: 'Past', value: 'past' },
  ];

  readonly selectedPet = computed(() =>
    this.pets().find((pet) => pet.id === this.selectedPetId()) ?? null,
  );

  readonly availableSlotsLabel = computed(() =>
    this.timeSlots().length ? `${this.timeSlots().length} open slots ready to book` : 'Choose a vet and date to load slots',
  );

  constructor() {
    this.loadData();
  }

  todayDate() {
    return new Date().toISOString().split('T')[0];
  }

  setFilter(filter: AppointmentFilter) {
    this.activeFilter.set(filter);
  }

  openDetails(visit: Visit) {
    this.selectedAppointment.set(visit);
  }

  closeDetails() {
    this.selectedAppointment.set(null);
  }

  cancelAppointment(visit: Visit) {
    if (this.cancellingAppointmentId() || !confirm('Cancel this appointment?')) {
      return;
    }

    this.cancellingAppointmentId.set(visit.id);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.visits.deleteVisit(visit.id).subscribe({
      next: () => {
        this.cancellingAppointmentId.set(null);
        this.closeDetails();
        this.appointments.update((appointments) => appointments.filter((appointment) => appointment.id !== visit.id));
        this.statusMessage.set('Appointment cancelled successfully.');
      },
      error: () => {
        this.cancellingAppointmentId.set(null);
        this.errorMessage.set('Unable to cancel the appointment right now.');
      },
    });
  }

  appointmentStatus(visit: Visit) {
    return getAppointmentStatus(visit);
  }

  petName(petId?: number) {
    if (!petId) {
      return 'Pet details unavailable';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.name ?? `Pet #${normalizedPetId}`;
  }

  petType(petId?: number) {
    if (!petId) {
      return 'Pet profile not linked to this booking';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.type ?? 'Pet record';
  }

  petImage(petId?: number) {
    if (!petId) {
      return '';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.imageUrl ?? '';
  }

  loadSlots() {
    const vetId = Number(this.form.controls.vetId.value);
    const visitDate = this.form.controls.visitDate.value;
    if (!vetId || !visitDate) {
      this.timeSlots.set([]);
      return;
    }

    this.vets.getSlots(vetId, visitDate).subscribe({
      next: (slots) => {
        this.timeSlots.set(slots);
        this.form.patchValue({ timeSlot: '' });
      },
      error: () => this.errorMessage.set('Unable to load time slots right now.'),
    });
  }

  selectPet(pet: Pet) {
    this.selectedPetId.set(pet.id);
    this.form.patchValue({ petId: String(pet.id) });
  }

  bookAppointment() {
    if (this.form.invalid || !this.customer() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.visits
      .createVisit({
        customerId: this.customer()!.id,
        vetId: Number(this.form.controls.vetId.value),
        petId: Number(this.form.controls.petId.value),
        visitDate: this.form.controls.visitDate.value,
        timeSlot: this.form.controls.timeSlot.value,
        reason: this.form.controls.reason.value,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.statusMessage.set('Appointment booked successfully.');
          this.selectedPetId.set(null);
          this.form.reset({ petId: '', vetId: '', visitDate: '', timeSlot: '', reason: '' });
          this.timeSlots.set([]);
          this.loadAppointments();
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Booking failed. Please try another slot.');
        },
      });
  }

  vetName(vetId: number) {
    return this.vetsList().find((vet) => vet.id === vetId)?.name ?? `Vet #${vetId}`;
  }

  vetSpecialization(vetId: number) {
    return this.vetsList().find((vet) => vet.id === vetId)?.specialization ?? 'Veterinary care';
  }

  emptyStateTitle() {
    switch (this.activeFilter()) {
      case 'today':
        return 'No appointments today';
      case 'upcoming':
        return 'You’re all set!';
      case 'past':
        return 'No past visits yet';
      default:
        return 'No bookings yet 📅';
    }
  }

  private loadData() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.customers.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        forkJoin({
          pets: this.customers.getPets(customer.id),
          vets: this.vets.getAllVets(),
          appointments: this.visits.getVisitsByCustomer(customer.id),
        }).subscribe({
          next: ({ pets, vets, appointments }) => {
            this.pets.set(pets);
            this.vetsList.set(vets);
            this.appointments.set(appointments);
          },
        });
      },
    });
  }

  private loadAppointments() {
    if (!this.customer()) {
      return;
    }

    this.visits.getVisitsByCustomer(this.customer()!.id).subscribe({
      next: (appointments) => this.appointments.set(appointments),
    });
  }
}
