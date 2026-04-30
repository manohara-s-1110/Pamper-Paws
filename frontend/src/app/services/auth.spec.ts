import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('AuthService', () => {

  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should call register API (success)', () => {
    const mockData = { username: 'test', password: '123' };

    service.register(mockData).subscribe(res => {
      expect(res).toBe('success');
    });

    const req = httpMock.expectOne('http://localhost:8087/auth/register');
    expect(req.request.method).toBe('POST');

    req.flush('success');
  });

  it('should handle register API error', () => {
    const mockData = { username: 'test', password: '123' };

    service.register(mockData).subscribe({
      error: err => {
        expect(err.status).toBe(400);
      }
    });

    const req = httpMock.expectOne('http://localhost:8087/auth/register');
    req.flush('error', { status: 400, statusText: 'Bad Request' });
  });

  it('should call login API (success)', () => {
    const mockData = { username: 'test', password: '123' };

    service.login(mockData).subscribe(res => {
      expect(res).toBe('token123');
    });

    const req = httpMock.expectOne('http://localhost:8087/auth/login');
    expect(req.request.method).toBe('POST');

    req.flush('token123');
  });

  it('should handle login API error', () => {
    const mockData = { username: 'test', password: '123' };

    service.login(mockData).subscribe({
      error: err => {
        expect(err.status).toBe(401);
      }
    });

    const req = httpMock.expectOne('http://localhost:8087/auth/login');
    req.flush('error', { status: 401, statusText: 'Unauthorized' });
  });

  afterEach(() => {
    httpMock.verify();
  });

});