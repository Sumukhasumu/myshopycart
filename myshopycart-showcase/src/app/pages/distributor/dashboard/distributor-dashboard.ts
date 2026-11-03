
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-distributor-dashboard-page',
  imports: [RouterLink],
  templateUrl: './distributor-dashboard.html',
  styleUrl: './distributor-dashboard.css',
})
export class DistributorDashboardPage {
  private readonly authService = inject(AuthService);

  readonly user = this.authService.currentUser;

  readonly displayName = computed(
    () => this.user()?.username?.trim() || 'Distributor',
  );
}
