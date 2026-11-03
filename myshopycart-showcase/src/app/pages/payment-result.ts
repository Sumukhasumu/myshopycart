
import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

type PaymentStatus = 'SUCCESS' | 'FAILURE';
type PaymentMethod = 'UPI' | 'CARD' | 'DIGITAL_WALLET';

@Component({
  selector: 'app-payment-result-page',
  templateUrl: './payment-result.html',
  styleUrl: './payment-result.css',
})
export class PaymentResultPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly orderId =
    this.route.snapshot.queryParamMap.get('orderId') ?? '';

  readonly status =
    this.route.snapshot.queryParamMap.get('status') as PaymentStatus | null;

  readonly paymentMethod =
    this.route.snapshot.queryParamMap.get('method') as PaymentMethod | null;

  readonly cartCleanupFailed =
    this.route.snapshot.queryParamMap.get('cartCleanup') === 'failed';

  readonly isSuccess = computed(() => this.status === 'SUCCESS');
  readonly isFailure = computed(() => this.status === 'FAILURE');

  readonly hasValidResult = computed(
    () =>
      !!this.orderId &&
      (this.isSuccess() || this.isFailure()),
  );

  readonly paymentMethodLabel = computed(() => {
    switch (this.paymentMethod) {
      case 'UPI':
        return 'UPI';
      case 'CARD':
        return 'Credit or Debit Card';
      case 'DIGITAL_WALLET':
        return 'Digital Wallet';
      default:
        return 'Not available';
    }
  });

  viewOrder(): void {
    if (!this.orderId) {
      return;
    }

    this.router.navigate(['/orders', this.orderId]);
  }

  reviewCart(): void {
    this.router.navigate(['/cart']);
  }

  continueShopping(): void {
    this.router.navigate(['/products']);
  }

  goToOrders(): void {
    this.router.navigate(['/orders']);
  }

  goToHome(): void {
    this.router.navigate(['/']);
  }
}
