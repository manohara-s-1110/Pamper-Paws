import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessionAuthService } from '../services/session-auth';

export const portalAuthGuard: CanActivateFn = () => {
  const auth = inject(SessionAuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
