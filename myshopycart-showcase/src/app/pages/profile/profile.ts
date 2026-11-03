
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile-page',
  imports: [RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfilePage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  readonly isAuthenticated = this.authService.isAuthenticated;

  logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login', {
      replaceUrl: true,
    });
  }
}
