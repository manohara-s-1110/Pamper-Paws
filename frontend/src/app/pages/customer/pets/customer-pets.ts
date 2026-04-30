import { Component, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AppModalComponent } from '../../../shared/components/app-modal/app-modal';
import { Customer, Pet } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';

@Component({
  selector: 'app-customer-pets',
  standalone: true,
  imports: [ReactiveFormsModule, NgFor, NgIf, AppModalComponent],
  templateUrl: './customer-pets.html',
})
export class CustomerPetsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);

  readonly customer = signal<Customer | null>(null);
  readonly pets = signal<Pet[]>([]);
  readonly currentPetId = signal<number | null>(null);
  readonly selectedPet = signal<Pet | null>(null);
  readonly previewUrl = signal('');
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    type: ['', Validators.required],
    age: [0, [Validators.required, Validators.min(0)]],
    imageUrl: [''],
  });

  constructor() {
    this.loadData();
  }

  async onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const imageUrl = await this.readFileAsDataUrl(file);
    this.form.patchValue({ imageUrl });
    this.previewUrl.set(imageUrl);
  }

  editPet(pet: Pet) {
    this.currentPetId.set(pet.id);
    this.previewUrl.set(pet.imageUrl ?? '');
    this.form.patchValue({
      name: pet.name,
      type: pet.type,
      age: pet.age,
      imageUrl: pet.imageUrl ?? '',
    });
    this.statusMessage.set('');
    this.errorMessage.set('');
  }

  cancelEdit() {
    this.currentPetId.set(null);
    this.previewUrl.set('');
    this.form.reset({ name: '', type: '', age: 0, imageUrl: '' });
  }

  savePet() {
    if (this.form.invalid || !this.customer() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    const payload = this.form.getRawValue();
    const request = this.currentPetId()
      ? this.customers.updatePet(this.currentPetId()!, payload)
      : this.customers.createPet(this.customer()!.id, payload);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.statusMessage.set(this.currentPetId() ? 'Pet profile updated.' : 'Pet profile created.');
        this.cancelEdit();
        this.loadPets();
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(this.resolveErrorMessage(error, 'Unable to save the pet right now.'));
      },
    });
  }

  deletePet(pet: Pet) {
    if (!confirm(`Delete ${pet.name}'s profile?`)) {
      return;
    }

    this.customers.deletePet(pet.id).subscribe({
      next: () => this.loadPets(),
      error: () => this.errorMessage.set('Unable to delete that pet right now.'),
    });
  }

  openPetDetails(pet: Pet) {
    this.selectedPet.set(pet);
  }

  closePetDetails() {
    this.selectedPet.set(null);
  }

  private loadData() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.customers.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.loadPets();
      },
      error: (error) => {
        this.errorMessage.set(this.resolveErrorMessage(error, 'Unable to load your customer profile right now.'));
      },
    });
  }

  private loadPets() {
    if (!this.customer()) {
      return;
    }

    this.customers.getPets(this.customer()!.id).subscribe({
      next: (pets) => this.pets.set(pets),
      error: (error) => {
        this.errorMessage.set(this.resolveErrorMessage(error, 'Unable to load your pets right now.'));
      },
    });
  }

  private resolveErrorMessage(error: { status?: number; error?: { message?: string } | string }, fallback: string) {
    if (error?.status === 401) {
      return 'Your session is not being accepted right now. Please sign in again after the services restart.';
    }

    const responseError = error?.error;

    if (typeof responseError === 'string' && responseError.trim()) {
      return responseError;
    }

    if (responseError && typeof responseError === 'object' && responseError.message) {
      return responseError.message;
    }

    return fallback;
  }

  private readFileAsDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result));
      reader.onerror = () => reject(reader.error);
      reader.readAsDataURL(file);
    });
  }
}
