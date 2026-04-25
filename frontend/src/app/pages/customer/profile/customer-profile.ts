import { Component, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Customer } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';

@Component({
  selector: 'app-customer-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './customer-profile.html',
})
export class CustomerProfileComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);

  readonly customer = signal<Customer | null>(null);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: [{ value: '', disabled: true }],
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    address: ['', Validators.required],
  });

  constructor() {
    this.loadCustomer();
  }

  saveProfile() {
    if (this.form.invalid || !this.customer() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    const customer = this.customer()!;
    this.customers
      .updateCustomer(customer.id, {
        username: customer.username,
        name: this.form.controls.name.value,
        email: this.form.controls.email.value,
        phone: this.form.controls.phone.value,
        address: this.form.controls.address.value,
      })
      .subscribe({
        next: (updatedCustomer) => {
          this.customer.set(updatedCustomer);
          this.saving.set(false);
          this.statusMessage.set('Profile updated successfully.');
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Unable to save profile changes right now.');
        },
      });
  }

  private loadCustomer() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.customers.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.form.patchValue({
          username: customer.username,
          name: customer.name,
          email: customer.email,
          phone: customer.phone,
          address: customer.address,
        });
      },
    });
  }
}
