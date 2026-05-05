import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import { AppModalComponent } from '../../../shared/components/app-modal/app-modal';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { Customer, Pet, Vet, Visit } from '../../../models/app.models';
import { CustomerDataService } from '../../../services/customer-data';
import { SessionAuthService } from '../../../services/session-auth';
import { VetDataService } from '../../../services/vet-data';
import { VisitDataService } from '../../../services/visit-data';
import { PaymentDataService } from '../../../services/payment-data';
import {
  AppointmentFilter,
  appointmentDateTimeValue,
  getAppointmentStatus,
  isVisitInFilter,
} from '../../../utils/appointment-ui';

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => { open: () => void };
  }
}

@Component({
  selector: 'app-customer-appointments',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, NgFor, NgIf, DatePipe, AppModalComponent, StatusBadgeComponent],
  templateUrl: './customer-appointments.html',
})
export class CustomerAppointmentsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(SessionAuthService);
  private readonly customers = inject(CustomerDataService);
  private readonly vets = inject(VetDataService);
  private readonly visits = inject(VisitDataService);
  private readonly payments = inject(PaymentDataService);

  readonly customer = signal<Customer | null>(null);
  readonly pets = signal<Pet[]>([]);
  readonly vetsList = signal<Vet[]>([]);
  readonly vetLocationFilter = signal('');
  readonly vetExperienceFilter = signal<number | null>(null);
  readonly vetSpecializationFilter = signal('');
  readonly appointments = signal<Visit[]>([]);
  readonly timeSlots = signal<string[]>([]);
  readonly unavailableSlots = signal<string[]>([]);
  readonly activeFilter = signal<AppointmentFilter>('all');
  readonly selectedAppointment = signal<Visit | null>(null);
  readonly selectedPetId = signal<number | null>(null);
  readonly statusMessage = signal('');
  readonly errorMessage = signal('');
  readonly saving = signal(false);
  readonly cancellingAppointmentId = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    petId: ['', Validators.required],
    vetId: ['', Validators.required],
    visitDate: ['', Validators.required],
    timeSlot: ['', Validators.required],
    reason: ['', Validators.required],
    paymentMethod: ['ONLINE' as 'ONLINE' | 'CASH', Validators.required],
  });

  readonly filteredAppointments = computed(() =>
    [...this.appointments()].sort((left, right) =>
      appointmentDateTimeValue(left) - appointmentDateTimeValue(right),
    ),
  );

  readonly visibleAppointments = computed(() =>
    this.filteredAppointments().filter((visit) => isVisitInFilter(visit, this.activeFilter())),
  );

  readonly nextAppointment = computed(() =>
    [...this.appointments()]
      .filter((visit) => appointmentDateTimeValue(visit) >= Date.now() && getAppointmentStatus(visit) !== 'Completed')
      .sort((left, right) => appointmentDateTimeValue(left) - appointmentDateTimeValue(right))[0] ?? null,
  );

  readonly todayCount = computed(() =>
    this.filteredAppointments().filter((visit) => getAppointmentStatus(visit) === 'Today').length,
  );

  readonly upcomingCount = computed(() =>
    this.filteredAppointments().filter((visit) => getAppointmentStatus(visit) === 'Upcoming').length,
  );

  readonly filters: { label: string; value: AppointmentFilter }[] = [
    { label: 'All', value: 'all' },
    { label: 'Today', value: 'today' },
    { label: 'Upcoming', value: 'upcoming' },
    { label: 'Past', value: 'past' },
  ];

  readonly selectedPet = computed(() =>
    this.pets().find((pet) => pet.id === this.selectedPetId()) ?? null,
  );

  readonly vetLocations = computed(() =>
    [...new Set(this.vetsList().map((vet) => vet.clinicAddress).filter(Boolean))].sort(),
  );

  readonly vetSpecializations = computed(() =>
    [...new Set(this.vetsList().map((vet) => vet.specialization).filter(Boolean))].sort(),
  );

  readonly availableSlotsLabel = computed(() =>
    this.availableSlots().length ? `${this.availableSlots().length} open slots ready to book` : 'Choose a vet and date to load slots',
  );

  readonly availableSlots = computed(() =>
    this.timeSlots().filter((slot) => !this.unavailableSlots().includes(slot)),
  );

  constructor() {
    this.loadData();
  }

  todayDate() {
    return new Date().toISOString().split('T')[0];
  }

  setFilter(filter: AppointmentFilter) {
    this.activeFilter.set(filter);
  }

  openDetails(visit: Visit) {
    this.selectedAppointment.set(visit);
  }

  closeDetails() {
    this.selectedAppointment.set(null);
  }

  cancelAppointment(visit: Visit) {
    if (this.cancellingAppointmentId() || !confirm('Cancel this appointment?')) {
      return;
    }

    this.cancellingAppointmentId.set(visit.id);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.visits.updateVisitStatus(visit.id, 'CANCELLED').subscribe({
      next: (updatedVisit) => {
        this.cancellingAppointmentId.set(null);
        this.closeDetails();
        this.appointments.update((appointments) =>
          appointments.map((appointment) => appointment.id === updatedVisit.id ? updatedVisit : appointment),
        );
        this.statusMessage.set('Appointment cancelled successfully.');
      },
      error: () => {
        this.cancellingAppointmentId.set(null);
        this.errorMessage.set('Unable to cancel the appointment right now.');
      },
    });
  }

  appointmentStatus(visit: Visit) {
    return getAppointmentStatus(visit);
  }

  petName(petId?: number) {
    if (!petId) {
      return 'Pet details unavailable';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.name ?? `Pet #${normalizedPetId}`;
  }

  petType(petId?: number) {
    if (!petId) {
      return 'Pet profile not linked to this booking';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.type ?? 'Pet record';
  }

  petImage(petId?: number) {
    if (!petId) {
      return '';
    }

    const normalizedPetId = Number(petId);
    return this.pets().find((pet) => pet.id === normalizedPetId)?.imageUrl ?? '';
  }

  loadSlots() {
    const vetId = Number(this.form.controls.vetId.value);
    const visitDate = this.form.controls.visitDate.value;
    if (!vetId || !visitDate) {
      this.timeSlots.set([]);
      this.unavailableSlots.set([]);
      return;
    }

    forkJoin({
      slots: this.vets.getSlots(vetId, visitDate),
      unavailableSlots: this.visits.getUnavailableSlots(vetId, visitDate).pipe(catchError(() => of([] as string[]))),
    }).subscribe({
      next: ({ slots, unavailableSlots }) => {
        this.timeSlots.set(slots);
        this.unavailableSlots.set(unavailableSlots);
        this.form.patchValue({ timeSlot: '' });
      },
      error: () => this.errorMessage.set('Unable to load time slots right now.'),
    });
  }

  selectPet(pet: Pet) {
    this.selectedPetId.set(pet.id);
    this.form.patchValue({ petId: String(pet.id) });
  }

  updateVetLocationFilter(value: string) {
    this.vetLocationFilter.set(value);
    this.loadFilteredVets();
  }

  updateVetExperienceFilter(value: string) {
    this.vetExperienceFilter.set(value ? Number(value) : null);
    this.loadFilteredVets();
  }

  updateVetSpecializationFilter(value: string) {
    this.vetSpecializationFilter.set(value);
    this.loadFilteredVets();
  }

  clearVetFilters() {
    this.vetLocationFilter.set('');
    this.vetExperienceFilter.set(null);
    this.vetSpecializationFilter.set('');
    this.loadFilteredVets();
  }

  bookAppointment() {
    if (!this.form.controls.petId.value) {
      alert('Please select a pet before booking');
      this.form.controls.petId.markAsTouched();
      return;
    }

    if (this.form.invalid || !this.customer() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');

    this.visits
      .createVisit({
        customerId: this.customer()!.id,
        vetId: Number(this.form.controls.vetId.value),
        petId: Number(this.form.controls.petId.value),
        visitDate: this.form.controls.visitDate.value,
        timeSlot: this.form.controls.timeSlot.value,
        reason: this.form.controls.reason.value,
        paymentMethod: this.form.controls.paymentMethod.value,
      })
      .subscribe({
        next: (visit) => {
          if (visit.paymentMethod === 'ONLINE' && visit.payment?.razorpayOrderId) {
            this.openRazorpayCheckout(visit);
            return;
          }
          this.finishBooking('Appointment booked successfully. Cash payment can be completed at the clinic.');
        },
        error: (error) => {
          this.saving.set(false);
          this.errorMessage.set(error?.error?.message ?? 'Booking failed. Please try another slot.');
        },
      });
  }

  vetName(vetId: number) {
    return this.vetsList().find((vet) => vet.id === vetId)?.name ?? `Vet #${vetId}`;
  }

  vetSpecialization(vetId: number) {
    return this.vetsList().find((vet) => vet.id === vetId)?.specialization ?? 'Veterinary care';
  }

  selectedVet() {
    const vetId = Number(this.form.controls.vetId.value);
    return this.vetsList().find((vet) => vet.id === vetId) ?? null;
  }

  selectedFee() {
    return this.selectedVet()?.consultationFee || 500;
  }

  emptyStateTitle() {
    switch (this.activeFilter()) {
      case 'today':
        return 'No appointments today';
      case 'upcoming':
        return 'You’re all set!';
      case 'past':
        return 'No past visits yet';
      default:
        return 'No bookings yet 📅';
    }
  }

  paymentLabel(visit: Visit) {
    const method = visit.paymentMethod ?? 'ONLINE';
    const status = visit.paymentStatus ?? 'PENDING';
    return `${method} · ${status}`;
  }

  retryPayment(visit: Visit) {
    if (this.saving()) {
      return;
    }

    const amount = this.paymentAmountForVisit(visit);
    if (!amount || amount <= 0) {
      this.errorMessage.set('Payment amount is not available for this booking. Please contact support.');
      return;
    }
    this.saving.set(true);
    this.statusMessage.set('');
    this.errorMessage.set('');
    this.payments
      .initiatePayment({
        appointmentId: visit.id,
        userId: visit.customerId,
        amount,
        paymentMethod: 'ONLINE',
      })
      .subscribe({
        next: (payment) => {
          if (payment.paymentStatus === 'SUCCESS') {
            this.markAppointmentPaymentSuccess(visit.id);
            this.finishBooking('Payment already succeeded. Appointment confirmed.');
            return;
          }
          this.openRazorpayCheckout({ ...visit, payment });
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Unable to restart payment right now.');
        },
      });
  }

  private finishBooking(message: string) {
    this.saving.set(false);
    this.statusMessage.set(message);
    this.selectedPetId.set(null);
    this.form.reset({ petId: '', vetId: '', visitDate: '', timeSlot: '', reason: '', paymentMethod: 'ONLINE' });
    this.timeSlots.set([]);
    this.unavailableSlots.set([]);
    this.loadAppointments();
  }

  private openRazorpayCheckout(visit: Visit) {
    const payment = visit.payment;
    if (!payment?.razorpayOrderId || !payment.razorpayKeyId) {
      this.saving.set(false);
      this.errorMessage.set('Payment could not be started. Razorpay is not configured yet.');
      this.loadAppointments();
      return;
    }

    this.loadRazorpayScript()
      .then(() => {
        const checkout = new window.Razorpay!({
          key: payment.razorpayKeyId,
          amount: Math.round(Number(payment.amount) * 100),
          currency: payment.currency ?? 'INR',
          name: 'PamperPaws',
          description: `Appointment #${visit.id}`,
          order_id: payment.razorpayOrderId,
          handler: (response: any) => {
            this.payments
              .verifyPayment({
                appointmentId: visit.id,
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              })
              .subscribe({
                next: () => this.finishBooking('Payment successful. Appointment confirmed.'),
                error: () => this.reconcileAfterVerifyFailure(visit),
              });
          },
          modal: {
            ondismiss: () => {
              this.saving.set(false);
              this.statusMessage.set('Appointment is pending until online payment is completed.');
              this.loadAppointments();
            },
          },
          prefill: {
            name: this.customer()?.name,
            email: this.customer()?.email,
            contact: this.customer()?.phone,
          },
          theme: { color: '#2563eb' },
        });
        checkout.open();
      })
      .catch(() => {
        this.saving.set(false);
        this.errorMessage.set('Unable to load Razorpay checkout. Please try again.');
        this.loadAppointments();
      });
  }

  private reconcileAfterVerifyFailure(visit: Visit) {
    const amount = this.paymentAmountForVisit(visit);
    if (!amount) {
      this.saving.set(false);
      this.errorMessage.set('Payment verification failed. You can retry from your booking.');
      this.loadAppointments();
      return;
    }

    this.payments.initiatePayment({
      appointmentId: visit.id,
      userId: visit.customerId,
      amount,
      paymentMethod: 'ONLINE',
    }).subscribe({
      next: (payment) => {
        if (payment.paymentStatus === 'SUCCESS') {
          this.markAppointmentPaymentSuccess(visit.id);
          this.finishBooking('Payment successful. Appointment confirmed.');
          return;
        }
        this.saving.set(false);
        this.errorMessage.set('Payment verification failed. You can retry from your booking.');
        this.loadAppointments();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Payment verification failed. You can retry from your booking.');
        this.loadAppointments();
      },
    });
  }

  paymentAmountForVisit(visit: Visit) {
    return visit.consultationFee || this.vetsList().find((vet) => vet.id === visit.vetId)?.consultationFee || 500;
  }

  shouldShowRetry(visit: Visit) {
    const status = this.appointmentStatus(visit);
    return visit.paymentMethod === 'ONLINE'
      && visit.paymentStatus !== 'SUCCESS'
      && status !== 'Completed'
      && status !== 'Missed'
      && status !== 'Cancelled';
  }

  private markAppointmentPaymentSuccess(appointmentId: number) {
    this.appointments.update((appointments) =>
      appointments.map((appointment) =>
        appointment.id === appointmentId
          ? { ...appointment, paymentStatus: 'SUCCESS', status: appointment.status === 'PENDING' ? 'CONFIRMED' : appointment.status }
          : appointment,
      ),
    );
  }

  private loadRazorpayScript() {
    if (window.Razorpay) {
      return Promise.resolve();
    }

    return new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve();
      script.onerror = () => reject();
      document.body.appendChild(script);
    });
  }

  private loadData() {
    const username = this.auth.session()?.username;
    if (!username) {
      return;
    }

    this.customers.getCustomerByUsername(username).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        forkJoin({
          pets: this.customers.getPets(customer.id),
          vets: this.vets.getAllVets(),
          appointments: this.visits.getVisitsByCustomer(customer.id),
        }).subscribe({
          next: ({ pets, vets, appointments }) => {
            this.pets.set(pets);
            this.vetsList.set(vets);
            this.appointments.set(appointments);
          },
        });
      },
    });
  }

  private loadAppointments() {
    if (!this.customer()) {
      return;
    }

    this.visits.getVisitsByCustomer(this.customer()!.id).subscribe({
      next: (appointments) => this.appointments.set(appointments),
    });
  }

  private loadFilteredVets() {
    this.vets.filterVets({
      location: this.vetLocationFilter(),
      experience: this.vetExperienceFilter(),
      specialization: this.vetSpecializationFilter(),
    }).subscribe({
      next: (vets) => {
        this.vetsList.set(vets);
        const selectedVetId = Number(this.form.controls.vetId.value);
        if (selectedVetId && !vets.some((vet) => vet.id === selectedVetId)) {
          this.form.patchValue({ vetId: '', timeSlot: '' });
          this.timeSlots.set([]);
          this.unavailableSlots.set([]);
        }
      },
      error: () => this.errorMessage.set('Unable to filter veterinarians right now.'),
    });
  }
}
