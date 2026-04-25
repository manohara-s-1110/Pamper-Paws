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

  getVisitsByPet(petId: number) {
    return this.http.get<Visit[]>(`${this.baseUrl}/pet/${petId}`);
  }

  deleteVisit(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
