import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth-guard';

describe('authGuard', () => {

  let routerMock: any;

  beforeEach(() => {
    routerMock = {
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerMock }
      ]
    });

    sessionStorage.clear(); // ensure clean state before each test
  });

  it('should allow access when token exists', () => {

    sessionStorage.setItem('token', 'dummy-token');

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );

    expect(result).toBeTruthy();
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to login when token does not exist', () => {

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any)
    );

    expect(result).toBeFalsy();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });

});