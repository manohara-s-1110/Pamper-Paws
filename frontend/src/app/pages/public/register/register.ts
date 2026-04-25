import { Component, computed, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { UserRole } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';

@Component({
  selector: 'app-public-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf],
  templateUrl: './register.html',
})
export class PublicRegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);
  private readonly router = inject(Router);

  readonly activeRole = signal<UserRole>('CUSTOMER');
  readonly submitting = signal(false);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly roleLabel = computed(() =>
    this.activeRole() === 'CUSTOMER' ? 'Customer' : 'Veterinarian',
  );

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    address: [''],
    specialization: [''],
    experience: [0],
    clinicAddress: [''],
    availableDays: [''],
    availableTime: [''],
  });

  selectRole(role: UserRole) {
    this.activeRole.set(role);
    this.statusMessage.set('');
    this.errorMessage.set('');
  }

  register() {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const values = this.form.getRawValue();
    if (values.password !== values.confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    if (this.activeRole() === 'CUSTOMER' && !values.address.trim()) {
      this.errorMessage.set('Address is required for customer registration.');
      return;
    }

    if (
      this.activeRole() === 'VET' &&
      (!values.specialization.trim() ||
        !values.clinicAddress.trim() ||
        !values.availableDays.trim() ||
        !values.availableTime.trim())
    ) {
      this.errorMessage.set('Complete the veterinarian details before continuing.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');
    this.statusMessage.set('');

    if (this.activeRole() === 'CUSTOMER') {
      this.auth
        .register({
          username: values.username,
          password: values.password,
          role: this.activeRole(),
          name: values.name,
        })
        .subscribe({
          next: () => {
            this.customers
              .createCustomer({
                username: values.username,
                name: values.name,
                email: values.email,
                phone: values.phone,
                address: values.address,
              })
              .subscribe({
                next: () => this.finishRegistration(),
                error: () => this.handleRegistrationError('Customer profile creation failed after auth registration.'),
              });
          },
          error: (error) => {
            this.handleRegistrationError(error?.error?.message ?? 'Registration failed. Please try again.');
          },
        });
      return;
    }

    this.auth
      .registerVet({
        username: values.username,
        password: values.password,
        name: values.name,
        email: values.email,
        phone: values.phone,
        specialization: values.specialization,
        experience: Number(values.experience),
        clinicAddress: values.clinicAddress,
        availableDays: values.availableDays,
        availableTime: values.availableTime,
      })
      .subscribe({
        next: () => this.finishRegistration(),
        error: (error) => {
          this.handleRegistrationError(error?.error?.message ?? 'Veterinarian registration failed. Please try again.');
        },
      });
  }

  private finishRegistration() {
    this.submitting.set(false);
    this.statusMessage.set(`${this.roleLabel()} account created. You can sign in now.`);
    this.form.reset({
      username: '',
      name: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
      address: '',
      specialization: '',
      experience: 0,
      clinicAddress: '',
      availableDays: '',
      availableTime: '',
    });

    setTimeout(() => {
      this.router.navigate(['/login']);
    }, 1200);
  }

  private handleRegistrationError(message: string) {
    this.submitting.set(false);
    this.errorMessage.set(message);
  }
}
