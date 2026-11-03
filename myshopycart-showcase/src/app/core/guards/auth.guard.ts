
import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { AuthService, UserRole } from '../services/auth.service';

export const authGuard: CanActivateFn = (
  _route,
  state: RouterStateSnapshot,
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], {
      queryParams: {
        returnUrl: state.url,
      },
    });
  }

  return true;
};

export function roleGuard(allowedRoles: UserRole[]): CanActivateFn {
  return (_route, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const user = authService.currentUser();

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login'], {
        queryParams: {
          returnUrl: state.url,
        },
      });
    }

    if (!user || !allowedRoles.includes(user.role)) {
      return router.createUrlTree(['/']);
    }

    return true;
  };
}
