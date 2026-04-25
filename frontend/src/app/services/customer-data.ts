import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Customer, CustomerPayload, Pet, PetPayload } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class CustomerDataService {
  private readonly baseUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  createCustomer(payload: CustomerPayload) {
    return this.http.post<Customer>(`${this.baseUrl}/customers`, payload);
  }

  getAllCustomers() {
    return this.http.get<Customer[]>(`${this.baseUrl}/customers`);
  }

  getCustomerByUsername(username: string) {
    return this.http.get<Customer>(`${this.baseUrl}/customers/username/${username}`);
  }

  updateCustomer(id: number, payload: CustomerPayload) {
    return this.http.put<Customer>(`${this.baseUrl}/customers/${id}`, payload);
  }

  deleteCustomer(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/customers/${id}`);
  }

  getPets(customerId: number) {
    return this.http.get<Pet[]>(`${this.baseUrl}/pets/customer/${customerId}`);
  }

  createPet(customerId: number, payload: PetPayload) {
    return this.http.post<Pet>(`${this.baseUrl}/pets/customer/${customerId}`, payload);
  }

  updatePet(id: number, payload: PetPayload) {
    return this.http.put<Pet>(`${this.baseUrl}/pets/${id}`, payload);
  }

  deletePet(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/pets/${id}`);
  }
}
