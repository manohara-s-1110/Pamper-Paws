import { TestBed } from '@angular/core/testing';
import { CustomerService } from './customer';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('CustomerService', () => {

  let service: CustomerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CustomerService]
    });

    service = TestBed.inject(CustomerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should create a customer (success)', () => {
    const mockData = { name: 'Bhanu' };

    service.createCustomer(mockData).subscribe(res => {
      expect(res).toEqual(mockData);
    });

    const req = httpMock.expectOne('http://localhost:8087/customers');
    expect(req.request.method).toBe('POST');

    req.flush(mockData);
  });

  it('should handle create customer error', () => {
    const mockData = { name: 'Bhanu' };

    service.createCustomer(mockData).subscribe({
      error: err => {
        expect(err.status).toBe(500);
      }
    });

    const req = httpMock.expectOne('http://localhost:8087/customers');
    req.flush('error', { status: 500, statusText: 'Server Error' });
  });

  afterEach(() => {
    httpMock.verify();
  });

});