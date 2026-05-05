import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Payment, PaymentInitiatePayload, PaymentInitiateResponse, PaymentVerifyPayload } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class PaymentDataService {
  private readonly baseUrl = 'http://localhost:8087/payments';

  constructor(private http: HttpClient) {}

  initiatePayment(payload: PaymentInitiatePayload) {
    return this.http.post<PaymentInitiateResponse>(`${this.baseUrl}/initiate`, payload);
  }

  verifyPayment(payload: PaymentVerifyPayload) {
    return this.http.post<Payment>(`${this.baseUrl}/verify`, payload);
  }

  getPayment(appointmentId: number) {
    return this.http.get<Payment>(`${this.baseUrl}/${appointmentId}`);
  }
}
