import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { Customer, Pet, Vet, Visit } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';
import { VisitDataService } from '../../../services/visit-data';
import { appointmentDateTimeValue, getAppointmentStatus } from '../../../utils/appointment-ui';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, DatePipe],
  templateUrl: './customer-dashboard.html',
})
export class CustomerDashboardComponent {
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);
  private readonly visits = inject(VisitDataService);
  private readonly vets = inject(VetDataService);

  readonly loading = signal(true);
  readonly customer = signal<Customer | null>(null);
  readonly pets = signal<Pet[]>([]);
  readonly appointments = signal<Visit[]>([]);
  readonly vetDirectory = signal<Record<number, Vet>>({});
  readonly petDirectory = signal<Record<number, Pet>>({});

  readonly upcomingAppointments = computed(() =>
    [...this.appointments()]
      .filter((visit) => getAppointmentStatus(visit) !== 'Completed')
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right))
      .slice(0, 4),
  );

  readonly nextAppointment = computed(() => this.upcomingAppointments()[0] ?? null);

  constructor() {
    this.loadDashboard();
  }

  private loadDashboard() {
    const username = this.auth.session()?.username;
    if (!username) {
      this.loading.set(false);
      return;
    }

    this.customers.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        forkJoin({
          pets: this.customers.getPets(customer.id),
          appointments: this.visits.getVisitsByCustomer(customer.id),
          vets: this.vets.getAllVets(),
        }).subscribe({
          next: ({ pets, appointments, vets }) => {
            this.pets.set(pets);
            this.appointments.set(appointments);
            this.petDirectory.set(
              pets.reduce<Record<number, Pet>>((accumulator, pet) => {
                accumulator[pet.id] = pet;
                return accumulator;
              }, {}),
            );
            this.vetDirectory.set(
              vets.reduce<Record<number, Vet>>((accumulator, vet) => {
                accumulator[vet.id] = vet;
                return accumulator;
              }, {}),
            );
            this.loading.set(false);
          },
          error: () => this.loading.set(false),
        });
      },
      error: () => this.loading.set(false),
    });
  }

  petName(petId?: number) {
    if (!petId) {
      return 'Pet details unavailable';
    }

    const normalizedPetId = Number(petId);
    return this.petDirectory()[normalizedPetId]?.name ?? `Pet #${normalizedPetId}`;
  }
}
