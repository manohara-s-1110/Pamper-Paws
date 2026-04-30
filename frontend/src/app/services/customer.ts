import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class CustomerService {

  private baseUrl = 'http://localhost:8087/customers';

  constructor(private http: HttpClient) {}

  createCustomer(data: any) {
    return this.http.post(this.baseUrl, data);
  }
}