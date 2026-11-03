
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  username = '';
  password = '';

  readonly showPassword = signal(false);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');

  readonly copiedCredential = signal('');
  readonly copyErrorMessage = signal('');

  readonly registrationSuccess = computed(() => {
    return this.router.getCurrentNavigation()?.extras.state?.['registrationSuccess']
      ?? history.state?.registrationSuccess
      ?? '';
  });

  readonly registeredUsername = computed(() => {
    return this.router.getCurrentNavigation()?.extras.state?.['registeredUsername']
      ?? history.state?.registeredUsername
      ?? '';
  });

  togglePasswordVisibility(): void {
    this.showPassword.update((value) => !value);
  }

  async copyDemoCredential(value: string): Promise<void> {
    this.copyErrorMessage.set('');
    this.copiedCredential.set('');

    try {
      await navigator.clipboard.writeText(value);
      this.copiedCredential.set(value);
    } catch {
      this.copyErrorMessage.set(
        'Unable to copy. Please check your browser clipboard permissions.',
      );
    }
  }

  login(): void {
    this.errorMessage.set('');

    const normalizedUsername = this.username.trim();

    if (!normalizedUsername || !this.password) {
      this.errorMessage.set('Please enter your username and password.');
      return;
    }

    this.isSubmitting.set(true);

    const user = this.authService.login(
      normalizedUsername,
      this.password,
    );

    this.isSubmitting.set(false);

    if (!user) {
      this.errorMessage.set(
        'Invalid username or password. Please use a valid demo account.',
      );
      return;
    }

    const requestedPath =
      this.router.getCurrentNavigation()?.extras.state?.['from']?.pathname
      ?? history.state?.from?.pathname;

    const destination =
      user.role === 'ADMIN'
        ? '/admin/dashboard'
        : user.role === 'DISTRIBUTOR'
          ? '/distributor/dashboard'
          : requestedPath || '/profile';

    this.router.navigateByUrl(destination, { replaceUrl: true });
  }
}