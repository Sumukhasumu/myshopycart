
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  email = '';
  mobileNumber = '';
  password = '';
  confirmPassword = '';

  readonly errorMessage = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly isSubmitting = signal(false);

  private validateForm(): boolean {
    const errors: Record<string, string> = {};

    if (!this.username.trim()) {
      errors['username'] = 'Username is required.';
    } else if (this.username.trim().length < 3) {
      errors['username'] = 'Username must contain at least 3 characters.';
    }

    if (!this.email.trim()) {
      errors['email'] = 'Email is required.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim())) {
      errors['email'] = 'Please enter a valid email address.';
    }

    if (
      this.mobileNumber.trim() &&
      !/^[0-9+\s()-]{7,20}$/.test(this.mobileNumber.trim())
    ) {
      errors['mobileNumber'] = 'Please enter a valid mobile number.';
    }

    if (!this.password) {
      errors['password'] = 'Password is required.';
    } else if (this.password.length < 8) {
      errors['password'] = 'Password must contain at least 8 characters.';
    }

    if (!this.confirmPassword) {
      errors['confirmPassword'] = 'Please confirm your password.';
    } else if (this.password !== this.confirmPassword) {
      errors['confirmPassword'] = 'Passwords do not match.';
    }

    this.fieldErrors.set(errors);
    return Object.keys(errors).length === 0;
  }

  register(): void {
    this.errorMessage.set('');

    if (!this.validateForm()) {
      return;
    }

    this.isSubmitting.set(true);

    const registered = this.authService.register(
      this.username,
      this.email,
      this.mobileNumber,
      this.password,
    );

    this.isSubmitting.set(false);

    if (!registered) {
      this.errorMessage.set(
        'This username or email is already registered. Please use another.',
      );
      return;
    }

    this.router.navigate(['/login'], {
      state: {
        registrationSuccess:
          'Registration successful. You can now sign in.',
        registeredUsername: this.username.trim(),
      },
    });
  }
}
