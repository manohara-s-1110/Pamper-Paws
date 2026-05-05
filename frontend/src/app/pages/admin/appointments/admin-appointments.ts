import { Component, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { forkJoin } from 'rxjs';

import { AdminDataService } from '../../../services/admin-data';
import { Customer, Vet, Visit } from '../../../models/app.models';

@Component({
  selector: 'app-admin-appointments',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './admin-appointments.html',
})
export class AdminAppointmentsComponent {
  private readonly admin = inject(AdminDataService);

  readonly appointments = signal<Visit[]>([]);
  readonly customerDirectory = signal<Record<number, Customer>>({});
  readonly vetDirectory = signal<Record<number, Vet>>({});

  constructor() {
    this.loadData();
  }

  customerName(customerId: number) {
    return this.customerDirectory()[customerId]?.name ?? `Customer #${customerId}`;
  }

  vetName(vetId: number) {
    return this.vetDirectory()[vetId]?.name ?? `Vet #${vetId}`;
  }

  deleteVisit(id: number) {
    if (!confirm('Delete this appointment?')) {
      return;
    }

    this.admin.deleteVisit(id).subscribe({
      next: () => this.loadData(),
    });
  }

  private loadData() {
    forkJoin({
      appointments: this.admin.getVisits(),
      customers: this.admin.getCustomers(),
      vets: this.admin.getVets(),
    }).subscribe({
      next: ({ appointments, customers, vets }) => {
        this.appointments.set(appointments);
        this.customerDirectory.set(
          customers.reduce<Record<number, Customer>>((accumulator, customer) => {
            accumulator[customer.id] = customer;
            return accumulator;
          }, {}),
        );
        this.vetDirectory.set(
          vets.reduce<Record<number, Vet>>((accumulator, vet) => {
            accumulator[vet.id] = vet;
            return accumulator;
          }, {}),
        );
      },
    });
  }
}
