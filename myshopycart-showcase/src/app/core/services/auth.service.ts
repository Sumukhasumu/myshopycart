
import { Injectable, signal } from '@angular/core';

export type UserRole = 'USER' | 'ADMIN' | 'DISTRIBUTOR';

export interface DemoUser {
  username: string;
  email: string;
  role: UserRole;
}

interface DemoAccount extends DemoUser {
  password: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly accounts: DemoAccount[] = [
  {
    username: 'demo.admin',
    email: 'admin@example.com',
    password: 'Admin@123',
    role: 'ADMIN',
  },
  {
    username: 'demo.distributor',
    email: 'distributor@example.com',
    password: 'Dist@123',
    role: 'DISTRIBUTOR',
  },
  {
    username: 'demo.customer',
    email: 'customer@example.com',
    password: 'Demo@123',
    role: 'USER',
  },
];

  readonly currentUser = signal<DemoUser | null>(null);

  readonly isAuthenticated = signal(false);

  login(username: string, password: string): DemoUser | null {
    const account = this.accounts.find(
      (item) =>
        item.username.toLowerCase() === username.trim().toLowerCase() &&
        item.password === password,
    );

    if (!account) {
      return null;
    }

    const user: DemoUser = {
      username: account.username,
      email: account.email,
      role: account.role,
    };

    this.currentUser.set(user);
    this.isAuthenticated.set(true);

    return user;
  }

  register(
    username: string,
    email: string,
    mobileNumber: string,
    password: string,
  ): boolean {
    const normalizedUsername = username.trim();
    const normalizedEmail = email.trim().toLowerCase();

    const accountExists = this.accounts.some(
      (item) =>
        item.username.toLowerCase() === normalizedUsername.toLowerCase() ||
        item.email.toLowerCase() === normalizedEmail,
    );

    if (accountExists) {
      return false;
    }

    this.accounts.push({
      username: normalizedUsername,
      email: normalizedEmail,
      password,
      role: 'USER',
    });

    return true;
  }

  logout(): void {
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }
}
