import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Vet, VetLeave, VetPayload } from '../models/app.models';

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

  filterVets(filters: { location?: string; experience?: number | null; specialization?: string }) {
    let params = new HttpParams();
    if (filters.location?.trim()) {
      params = params.set('location', filters.location.trim());
    }
    if (filters.experience !== null && filters.experience !== undefined) {
      params = params.set('experience', String(filters.experience));
    }
    if (filters.specialization?.trim()) {
      params = params.set('specialization', filters.specialization.trim());
    }
    return this.http.get<Vet[]>(`${this.baseUrl}/vets/filter`, { params });
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

  addLeave(vetId: number, date: string) {
    return this.http.post<VetLeave>(`${this.baseUrl}/vets/${vetId}/leaves`, { date });
  }

  getLeaves(vetId: number) {
    return this.http.get<VetLeave[]>(`${this.baseUrl}/vets/${vetId}/leave-records`);
  }
}
