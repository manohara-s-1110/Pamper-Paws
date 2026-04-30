import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { UserRole } from '../models/app.models';
import { SessionAuthService } from '../services/session-auth';

export const roleAccessGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(SessionAuthService);
  const router = inject(Router);
  const session = auth.session();
  const allowedRoles = (route.data['roles'] ?? []) as UserRole[];

  if (!session) {
    router.navigate(['/login']);
    return false;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(session.role)) {
    router.navigateByUrl(auth.landingPathForRole(session.role));
    return false;
  }

  return true;
};
