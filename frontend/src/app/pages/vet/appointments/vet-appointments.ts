import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { forkJoin } from 'rxjs';

import { AppModalComponent } from '../../../shared/components/app-modal/app-modal';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { Customer, Pet, Vet, Visit } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';
import { VisitDataService } from '../../../services/visit-data';
import { AppointmentFilter, appointmentDateTimeValue, getAppointmentStatus, isVisitInFilter } from '../../../utils/appointment-ui';

@Component({
  selector: 'app-vet-appointments',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, AppModalComponent, StatusBadgeComponent],
  templateUrl: './vet-appointments.html',
})
export class VetAppointmentsComponent {
  private readonly auth = inject(SessionAuthService);
  private readonly vets = inject(VetDataService);
  private readonly visits = inject(VisitDataService);
  private readonly customers = inject(CustomerDataService);

  readonly vet = signal<Vet | null>(null);
  readonly appointments = signal<Visit[]>([]);
  readonly customerDirectory = signal<Record<number, Customer>>({});
  readonly petDirectory = signal<Record<number, Pet>>({});
  readonly selectedAppointment = signal<Visit | null>(null);
  readonly patientHistory = signal<Visit[]>([]);
  readonly historyLoading = signal(false);
  readonly historyError = signal('');
  readonly activeFilter = signal<AppointmentFilter>('today');

  readonly filters: { label: string; value: AppointmentFilter }[] = [
    { label: 'Today', value: 'today' },
    { label: 'Upcoming', value: 'upcoming' },
    { label: 'Past', value: 'past' },
  ];

  readonly visibleAppointments = computed(() =>
    [...this.appointments()]
      .filter((visit) => isVisitInFilter(visit, this.activeFilter()))
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right)),
  );

  constructor() {
    this.loadData();
  }

  setFilter(filter: AppointmentFilter) {
    this.activeFilter.set(filter);
  }

  appointmentStatus(visit: Visit) {
    return getAppointmentStatus(visit);
  }

  customerName(customerId: number) {
    return this.customerDirectory()[customerId]?.name ?? `Customer #${customerId}`;
  }

  petName(petId?: number) {
    if (!petId) {
      return 'Pet details unavailable';
    }

    const normalizedPetId = Number(petId);
    return this.petDirectory()[normalizedPetId]?.name ?? `Pet #${normalizedPetId}`;
  }

  petDetails(petId?: number) {
    if (!petId) {
      return 'No linked pet profile';
    }

    const normalizedPetId = Number(petId);
    const pet = this.petDirectory()[normalizedPetId];
    return pet ? `${pet.type} · ${pet.age} years` : 'Pet record';
  }

  openDetails(visit: Visit) {
    this.selectedAppointment.set(visit);
    this.patientHistory.set([]);
    this.historyError.set('');

    if (!visit.petId) {
      this.historyError.set('Patient history is unavailable because this visit is not linked to a pet record.');
      return;
    }

    this.historyLoading.set(true);
    this.visits.getVisitsByPet(visit.petId).subscribe({
      next: (history) => {
        this.historyLoading.set(false);
        this.patientHistory.set(
          history
            .filter((item) => item.id !== visit.id)
            .sort((left, right) => appointmentDateTimeValue(right) - appointmentDateTimeValue(left)),
        );
      },
      error: () => {
        this.historyLoading.set(false);
        this.historyError.set('Unable to load patient history right now.');
      },
    });
  }

  closeDetails() {
    this.selectedAppointment.set(null);
    this.patientHistory.set([]);
    this.historyLoading.set(false);
    this.historyError.set('');
  }

  emptyStateTitle() {
    switch (this.activeFilter()) {
      case 'today':
        return 'No appointments today';
      case 'upcoming':
        return 'No upcoming visits';
      default:
        return 'No past visits';
    }
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
        }).subscribe({
          next: ({ appointments, customers }) => {
            this.appointments.set(appointments);
            this.customerDirectory.set(
              customers.reduce<Record<number, Customer>>((accumulator, customer) => {
                accumulator[customer.id] = customer;
                return accumulator;
              }, {}),
            );

            if (!customers.length) {
              this.petDirectory.set({});
              return;
            }

            forkJoin(customers.map((customer) => this.customers.getPets(customer.id))).subscribe({
              next: (petGroups) => {
                const pets = petGroups.flat();
                this.petDirectory.set(
                  pets.reduce<Record<number, Pet>>((accumulator, pet) => {
                    accumulator[pet.id] = pet;
                    return accumulator;
                  }, {}),
                );
              },
              error: () => {
                this.petDirectory.set({});
              },
            });
          },
        });
      },
    });
  }
}
