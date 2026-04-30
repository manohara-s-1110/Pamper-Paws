import { Component, computed, inject, signal } from '@angular/core';
import { NgClass, NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Vet } from '../../../models/app.models';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';

@Component({
  selector: 'app-vet-availability',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf, NgClass],
  templateUrl: './vet-availability.html',
})
export class VetAvailabilityComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly vets = inject(VetDataService);

  readonly dayOptions = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  readonly timeOptions = [
    '6 AM',
    '7 AM',
    '8 AM',
    '9 AM',
    '10 AM',
    '11 AM',
    '12 PM',
    '1 PM',
    '2 PM',
    '3 PM',
    '4 PM',
    '5 PM',
    '6 PM',
    '7 PM',
    '8 PM',
  ];

  readonly vet = signal<Vet | null>(null);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);
  readonly selectedDays = signal<string[]>([]);

  readonly form = this.fb.nonNullable.group({
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
  });

  readonly availabilitySummary = computed(() => {
    const days = this.selectedDays().join(', ');
    const startTime = this.form.controls.startTime.value;
    const endTime = this.form.controls.endTime.value;

    if (!days || !startTime || !endTime) {
      return 'Select at least one day and a working-hours range.';
    }

    return `${days} · ${startTime} - ${endTime}`;
  });

  constructor() {
    this.loadVet();
  }

  saveAvailability() {
    if (this.form.invalid || !this.vet() || this.saving() || !this.selectedDays().length) {
      this.form.markAllAsTouched();
      if (!this.selectedDays().length) {
        this.errorMessage.set('Choose at least one available day.');
      }
      return;
    }

    const startTime = this.form.controls.startTime.value;
    const endTime = this.form.controls.endTime.value;
    const startIndex = this.timeOptions.indexOf(startTime);
    const endIndex = this.timeOptions.indexOf(endTime);

    if (startIndex === -1 || endIndex === -1 || endIndex <= startIndex) {
      this.errorMessage.set('Select a valid working-hours range.');
      return;
    }

    const vet = this.vet()!;
    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.vets
      .updateVet(vet.id, {
        username: vet.username,
        name: vet.name,
        specialization: vet.specialization,
        experience: vet.experience,
        phone: vet.phone,
        email: vet.email,
        clinicAddress: vet.clinicAddress,
        availableDays: this.selectedDays().join(', '),
        availableTime: `${startTime} - ${endTime}`,
      })
      .subscribe({
        next: (updatedVet) => {
          this.vet.set(updatedVet);
          this.saving.set(false);
          this.statusMessage.set('Availability updated.');
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Unable to update availability right now.');
        },
      });
  }

  toggleDay(day: string) {
    this.selectedDays.update((currentDays) =>
      currentDays.includes(day)
        ? currentDays.filter((currentDay) => currentDay !== day)
        : [...currentDays, day],
    );
  }

  isDaySelected(day: string) {
    return this.selectedDays().includes(day);
  }

  private loadVet() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.vets.getVetByUsername(username).subscribe({
      next: (vet) => {
        this.vet.set(vet);
        this.selectedDays.set(
          vet.availableDays
            .split(',')
            .map((day) => day.trim())
            .filter(Boolean),
        );

        const [startTime = '', endTime = ''] = vet.availableTime.split('-').map((value) => value.trim());
        this.form.patchValue({
          startTime,
          endTime,
        });
      },
      error: () => {
        this.errorMessage.set('Unable to load availability details right now.');
      },
    });
  }
}
