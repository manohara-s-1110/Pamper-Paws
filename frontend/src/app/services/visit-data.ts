import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Visit, VisitPayload } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class VisitDataService {
  private readonly baseUrl = 'http://localhost:8087/visit';

  constructor(private http: HttpClient) {}

  createVisit(payload: VisitPayload) {
    return this.http.post<Visit>(this.baseUrl, payload);
  }

  getVisitsByCustomer(customerId: number) {
    return this.http.get<Visit[]>(`${this.baseUrl}/customer/${customerId}`);
  }

  getVisitsByVet(vetId: number) {
    return this.http.get<Visit[]>(`${this.baseUrl}/vet/${vetId}`);
  }

  getUnavailableSlots(vetId: number, date: string) {
    return this.http.get<string[]>(`${this.baseUrl}/vet/${vetId}/unavailable-slots?date=${date}`);
  }

  getVisitsByPet(petId: number) {
    return this.http.get<Visit[]>(`${this.baseUrl}/pet/${petId}`);
  }

  deleteVisit(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  updateVisitStatus(id: number, status: 'COMPLETED' | 'MISSED') {
    return this.http.patch<Visit>(`${this.baseUrl}/${id}/status`, { status });
  }
}
