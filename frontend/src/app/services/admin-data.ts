import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Customer, Pet, Vet, Visit } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class AdminDataService {
  private readonly baseUrl = 'http://localhost:8087/admin';

  constructor(private http: HttpClient) {}

  getCustomers() {
    return this.http.get<Customer[]>(`${this.baseUrl}/customers`);
  }

  getCustomerPets(customerId: number) {
    return this.http.get<Pet[]>(`${this.baseUrl}/customers/${customerId}/pets`);
  }

  getVets() {
    return this.http.get<Vet[]>(`${this.baseUrl}/vets`);
  }

  deleteVisit(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/visits/${id}`);
  }

  getVisits() {
    return this.http.get<Visit[]>(`${this.baseUrl}/visits`);
  }
}
