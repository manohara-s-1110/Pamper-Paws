import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { SessionAuthService } from '../services/session-auth';

export const sessionAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(SessionAuthService).getToken();

  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
