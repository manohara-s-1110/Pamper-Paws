import { Component, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AppModalComponent } from '../../../shared/components/app-modal/app-modal';
import { AdminDataService } from '../../../services/admin-data';
import { CustomerDataService } from '../../../services/customer-data';
import { VetDataService } from '../../../services/vet-data';
import { Customer, Pet, Vet } from '../../../models/app.models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, AppModalComponent],
  templateUrl: './admin-users.html',
})
export class AdminUsersComponent {
  private readonly fb = inject(FormBuilder);
  private readonly admin = inject(AdminDataService);
  private readonly customersApi = inject(CustomerDataService);
  private readonly vetsApi = inject(VetDataService);

  readonly customers = signal<Customer[]>([]);
  readonly vets = signal<Vet[]>([]);
  readonly selectedCustomerPets = signal<Pet[]>([]);
  readonly petsCustomer = signal<Customer | null>(null);
  readonly editingCustomer = signal<Customer | null>(null);
  readonly editingVet = signal<Vet | null>(null);
  readonly customersExpanded = signal(false);
  readonly vetsExpanded = signal(false);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);

  readonly customerForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', Validators.required],
    address: ['', Validators.required],
  });

  readonly vetForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    name: ['', Validators.required],
    specialization: ['', Validators.required],
    experience: [0, [Validators.required, Validators.min(0)]],
    phone: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    clinicAddress: ['', Validators.required],
    availableDays: ['', Validators.required],
    availableTime: ['', Validators.required],
  });

  constructor() {
    this.loadUsers();
  }

  openCustomerPets(customer: Customer) {
    this.petsCustomer.set(customer);
    this.selectedCustomerPets.set([]);

    this.admin.getCustomerPets(customer.id).subscribe({
      next: (pets) => this.selectedCustomerPets.set(pets),
      error: () => this.errorMessage.set('Unable to load this customer’s pets right now.'),
    });
  }

  closeCustomerPets() {
    this.petsCustomer.set(null);
    this.selectedCustomerPets.set([]);
  }

  toggleCustomersSection() {
    this.customersExpanded.update((expanded) => !expanded);
  }

  toggleVetsSection() {
    this.vetsExpanded.update((expanded) => !expanded);
  }

  startCustomerEdit(customer: Customer) {
    this.editingCustomer.set(customer);
    this.customerForm.reset({
      username: customer.username,
      name: customer.name,
      email: customer.email,
      phone: customer.phone,
      address: customer.address,
    });
  }

  closeCustomerEdit() {
    this.editingCustomer.set(null);
  }

  saveCustomer() {
    const customer = this.editingCustomer();
    if (!customer || this.customerForm.invalid || this.saving()) {
      this.customerForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.customersApi.updateCustomer(customer.id, this.customerForm.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.statusMessage.set('Customer updated successfully.');
        this.closeCustomerEdit();
        this.loadUsers();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Unable to update the customer right now.');
      },
    });
  }

  deleteCustomer(customer: Customer) {
    if (!confirm(`Permanently delete ${customer.name} and associated customer details?`)) {
      return;
    }

    this.customersApi.deleteCustomer(customer.id).subscribe({
      next: () => {
        this.statusMessage.set('Customer deleted successfully.');
        this.loadUsers();
      },
      error: () => this.errorMessage.set('Unable to delete the customer right now.'),
    });
  }

  startVetEdit(vet: Vet) {
    this.editingVet.set(vet);
    this.vetForm.reset({
      username: vet.username,
      name: vet.name,
      specialization: vet.specialization,
      experience: vet.experience,
      phone: vet.phone,
      email: vet.email,
      clinicAddress: vet.clinicAddress,
      availableDays: vet.availableDays,
      availableTime: vet.availableTime,
    });
  }

  closeVetEdit() {
    this.editingVet.set(null);
  }

  saveVet() {
    const vet = this.editingVet();
    if (!vet || this.vetForm.invalid || this.saving()) {
      this.vetForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.vetsApi.updateVet(vet.id, this.vetForm.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.statusMessage.set('Vet updated successfully.');
        this.closeVetEdit();
        this.loadUsers();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Unable to update the vet right now.');
      },
    });
  }

  deleteVet(vet: Vet) {
    if (!confirm(`Permanently delete ${vet.name} and their vet account?`)) {
      return;
    }

    this.vetsApi.deleteVet(vet.id).subscribe({
      next: () => {
        this.statusMessage.set('Vet deleted successfully.');
        this.loadUsers();
      },
      error: () => this.errorMessage.set('Unable to delete the vet right now.'),
    });
  }

  private loadUsers() {
    forkJoin({
      customers: this.admin.getCustomers(),
      vets: this.admin.getVets(),
    }).subscribe({
      next: ({ customers, vets }) => {
        this.customers.set(customers);
        this.vets.set(vets);
      },
      error: () => {
        this.errorMessage.set('Unable to load user directories right now.');
      },
    });
  }
}
