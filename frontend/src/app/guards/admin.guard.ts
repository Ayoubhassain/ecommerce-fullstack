import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Hides the admin pages from non-admins. This is only for the interface:
 * the real protection is on the backend, which checks the ADMIN role on every /api/admin call.
 */
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (this.authService.isAdmin()) {
      return true;
    }
    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    return this.router.createUrlTree(['/products']);
  }
}
