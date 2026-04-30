import { Component, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Vet } from '../../../models/app.models';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';

@Component({
  selector: 'app-vet-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './vet-profile.html',
})
export class VetProfileComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly vets = inject(VetDataService);

  readonly vet = signal<Vet | null>(null);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);
  readonly passwordStatusMessage = signal('');
  readonly passwordErrorMessage = signal('');
  readonly changingPassword = signal(false);

  readonly form = this.fb.nonNullable.group({
    username: [{ value: '', disabled: true }],
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    specialization: ['', Validators.required],
    experience: [0, [Validators.required, Validators.min(0)]],
    clinicAddress: ['', Validators.required],
  });

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
  });

  constructor() {
    this.loadVet();
  }

  saveProfile() {
    if (this.form.invalid || !this.vet() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const vet = this.vet()!;
    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.vets
      .updateVet(vet.id, {
        username: vet.username,
        name: this.form.controls.name.value,
        email: this.form.controls.email.value,
        phone: this.form.controls.phone.value,
        specialization: this.form.controls.specialization.value,
        experience: this.form.controls.experience.value,
        clinicAddress: this.form.controls.clinicAddress.value,
        availableDays: vet.availableDays,
        availableTime: vet.availableTime,
      })
      .subscribe({
        next: (updatedVet) => {
          this.vet.set(updatedVet);
          this.saving.set(false);
          this.statusMessage.set('Veterinarian profile updated.');
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Unable to update the profile right now.');
        },
      });
  }

  changePassword() {
    if (this.passwordForm.invalid || this.changingPassword()) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.passwordStatusMessage.set('');
    this.passwordErrorMessage.set('');

    const values = this.passwordForm.getRawValue();
    if (values.newPassword !== values.confirmPassword) {
      this.passwordErrorMessage.set('New password and confirmation do not match.');
      return;
    }

    this.changingPassword.set(true);

    this.auth
      .changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      })
      .subscribe({
        next: (message) => {
          this.changingPassword.set(false);
          this.passwordStatusMessage.set(message);
          this.passwordForm.reset({
            currentPassword: '',
            newPassword: '',
            confirmPassword: '',
          });
        },
        error: (error) => {
          this.changingPassword.set(false);
          this.passwordErrorMessage.set(error?.error?.message ?? 'Unable to update password right now.');
        },
      });
  }

  private loadVet() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.vets.getVetByUsername(username).subscribe({
      next: (vet) => {
        this.vet.set(vet);
        this.form.patchValue({
          username: vet.username,
          name: vet.name,
          email: vet.email,
          phone: vet.phone,
          specialization: vet.specialization,
          experience: vet.experience,
          clinicAddress: vet.clinicAddress,
        });
      },
      error: () => {
        this.errorMessage.set('Unable to load your profile right now.');
      },
    });
  }
}
