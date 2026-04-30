import { Component, computed, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { UserRole } from '../../../models/app.models';
import { SessionAuthService } from '../../../services/session-auth';

const STRONG_PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/;

@Component({
  selector: 'app-public-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf],
  templateUrl: './register.html',
})
export class PublicRegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
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
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(STRONG_PASSWORD_PATTERN)]],
    confirmPassword: ['', Validators.required],
    address: [''],
    specialization: [''],
    experience: [0],
    clinicAddress: [''],
    availableDays: [''],
    availableTime: [''],
  });

  constructor() {
    this.applyRoleValidators(this.activeRole());
  }

  selectRole(role: UserRole) {
    this.activeRole.set(role);
    this.statusMessage.set('');
    this.errorMessage.set('');
    this.applyRoleValidators(role);
  }

  register() {
    if (this.form.invalid || this.submitting()) {
      if (!this.submitting()) {
        this.errorMessage.set('Please fill all required fields');
      }
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
          email: values.email,
          phone: values.phone,
          address: values.address,
        })
        .subscribe({
          next: () => this.finishRegistration(),
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

  private applyRoleValidators(role: UserRole) {
    const address = this.form.controls.address;
    const specialization = this.form.controls.specialization;
    const experience = this.form.controls.experience;
    const clinicAddress = this.form.controls.clinicAddress;
    const availableDays = this.form.controls.availableDays;
    const availableTime = this.form.controls.availableTime;

    if (role === 'CUSTOMER') {
      address.setValidators([Validators.required]);
      specialization.clearValidators();
      experience.clearValidators();
      clinicAddress.clearValidators();
      availableDays.clearValidators();
      availableTime.clearValidators();
    } else {
      address.clearValidators();
      specialization.setValidators([Validators.required]);
      experience.setValidators([Validators.required, Validators.min(0)]);
      clinicAddress.setValidators([Validators.required]);
      availableDays.setValidators([Validators.required]);
      availableTime.setValidators([Validators.required]);
    }

    address.updateValueAndValidity({ emitEvent: false });
    specialization.updateValueAndValidity({ emitEvent: false });
    experience.updateValueAndValidity({ emitEvent: false });
    clinicAddress.updateValueAndValidity({ emitEvent: false });
    availableDays.updateValueAndValidity({ emitEvent: false });
    availableTime.updateValueAndValidity({ emitEvent: false });
  }
}
