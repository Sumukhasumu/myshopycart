
import {
  Component,
  computed,
  inject,
  ElementRef,
  HostListener,
} from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StoreStateService } from '../../core/services/store-state.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly storeState = inject(StoreStateService);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  // Mobile navigation and account dropdown have separate states.
  isMenuOpen = false;
  isAccountMenuOpen = false;

  readonly cartCount = this.storeState.cartCount;
  readonly wishlistCount = this.storeState.wishlistCount;

  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly currentUser = this.authService.currentUser;

  readonly accountLabel = computed(() => {
    const user = this.currentUser();

    return this.isAuthenticated()
      ? user?.username || 'Account'
      : 'Login';
  });

  readonly isDistributor = computed(
    () => this.currentUser()?.role === 'DISTRIBUTOR',
  );

  readonly isAdmin = computed(
    () => this.currentUser()?.role === 'ADMIN',
  );

  readonly dashboardDestination = computed(() => {
    if (this.isDistributor()) {
      return '/distributor/dashboard';
    }

    if (this.isAdmin()) {
      return '/admin/dashboard';
    }

    return '/profile';
  });

  get cartBadge(): string {
    return this.cartCount() > 99
      ? '99+'
      : String(this.cartCount());
  }

  get wishlistBadge(): string {
    return this.wishlistCount() > 99
      ? '99+'
      : String(this.wishlistCount());
  }

  @HostListener('document:pointerdown', ['$event'])
  onDocumentPointerDown(event: PointerEvent): void {
    if (!this.isAccountMenuOpen) {
      return;
    }

    const accountMenu =
      this.elementRef.nativeElement.querySelector('.header-account-menu');

    const target = event.target;

    if (
      accountMenu &&
      target instanceof Node &&
      !accountMenu.contains(target)
    ) {
      this.isAccountMenuOpen = false;
    }
  }

  @HostListener('document:keydown', ['$event'])
  onDocumentKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.isAccountMenuOpen = false;
    }
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
    this.isAccountMenuOpen = false;
  }

  toggleAccountMenu(): void {
    this.isAccountMenuOpen = !this.isAccountMenuOpen;
    this.isMenuOpen = false;
  }

  closeMenu(): void {
    this.isMenuOpen = false;
    this.isAccountMenuOpen = false;
  }

  navigateToDashboard(): void {
    void this.router.navigateByUrl(this.dashboardDestination());
    this.closeMenu();
  }

  logout(): void {
    this.authService.logout();
    this.closeMenu();
    void this.router.navigate(['/']);
  }

  searchProducts(event: Event): void {
    event.preventDefault();

    const form = event.target as HTMLFormElement;
    const formData = new FormData(form);
    const searchTerm = String(formData.get('search') || '').trim();

    this.closeMenu();

    void this.router.navigate(['/products'], {
      queryParams: searchTerm ? { search: searchTerm } : {},
    });
  }
}