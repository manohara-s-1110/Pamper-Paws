import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Vet, VetPayload } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class VetDataService {
  private readonly baseUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  createVet(payload: VetPayload) {
    return this.http.post<Vet>(`${this.baseUrl}/vets`, payload);
  }

  getAllVets() {
    return this.http.get<Vet[]>(`${this.baseUrl}/vets`);
  }

  getVetByUsername(username: string) {
    return this.http.get<Vet>(`${this.baseUrl}/vets/username/${username}`);
  }

  updateVet(id: number, payload: VetPayload) {
    return this.http.put<Vet>(`${this.baseUrl}/vets/${id}`, payload);
  }

  deleteVet(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/vets/${id}`);
  }

  getSlots(vetId: number, date: string) {
    return this.http.get<string[]>(`${this.baseUrl}/vets/${vetId}/slots?date=${date}`);
  }
}
