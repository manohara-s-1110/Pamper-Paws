import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {

  let next: any;

  beforeEach(() => {
    next = vi.fn((req: HttpRequest<any>) => req);

    TestBed.configureTestingModule({});
    sessionStorage.clear();
  });

  it('should add Authorization header when token exists', () => {

    sessionStorage.setItem('token', 'dummy-token');

    const request = new HttpRequest('GET', '/test');

    TestBed.runInInjectionContext(() => {
      authInterceptor(request, next);
    });

    const modifiedRequest = next.mock.calls[0][0];

    expect(modifiedRequest.headers.get('Authorization')).toBe('Bearer dummy-token');
  });

  it('should not add Authorization header when token does not exist', () => {

    const request = new HttpRequest('GET', '/test');

    TestBed.runInInjectionContext(() => {
      authInterceptor(request, next);
    });

    const modifiedRequest = next.mock.calls[0][0];

    expect(modifiedRequest.headers.has('Authorization')).toBeFalsy();
  });

});