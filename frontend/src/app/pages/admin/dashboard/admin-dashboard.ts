import { Component, computed, inject, signal } from '@angular/core';
import { NgFor } from '@angular/common';
import { forkJoin } from 'rxjs';

import { AdminDataService } from '../../../services/admin-data';
import { Customer, Vet, Visit } from '../../../models/app.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [NgFor],
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboardComponent {
  private readonly admin = inject(AdminDataService);

  readonly customers = signal<Customer[]>([]);
  readonly vets = signal<Vet[]>([]);
  readonly visits = signal<Visit[]>([]);

  readonly vetStats = computed(() =>
    this.vets().map((vet) => ({
      name: vet.name,
      visits: this.visits().filter((visit) => visit.vetId === vet.id).length,
    })),
  );

  constructor() {
    forkJoin({
      customers: this.admin.getCustomers(),
      vets: this.admin.getVets(),
      visits: this.admin.getVisits(),
    }).subscribe({
      next: ({ customers, vets, visits }) => {
        this.customers.set(customers);
        this.vets.set(vets);
        this.visits.set(visits);
      },
    });
  }
}
