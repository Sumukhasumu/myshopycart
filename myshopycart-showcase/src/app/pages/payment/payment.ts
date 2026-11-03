
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';
import { CheckoutStateService } from '../../core/services/checkout-state.service';
import {
  DemoOrderStateService,
  DemoPaymentMethod,
  DemoOrder,
} from '../../core/services/demo-order-state.service';

type PaymentMethod = 'upi' | 'card' | 'wallet';
type PaymentSimulation = 'success' | 'failure';

@Component({
  selector: 'app-payment-page',
  imports: [RouterLink],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
})
export class PaymentPage {
  private readonly storeState = inject(StoreStateService);
  private readonly checkoutState = inject(CheckoutStateService);
  private readonly demoOrderState = inject(DemoOrderStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly orderId = this.route.snapshot.paramMap.get('orderId') ?? '';

  readonly order = signal<DemoOrder | null>(
    this.demoOrderState.getOrderById(this.orderId) ?? null,
  );

  readonly cartItems = computed(() =>
    this.order()?.items.map((item) => ({
      ...item,
      id: item.productId,
      name: item.productName,
    })) ?? [],
  );

  readonly cartCount = computed(() =>
    this.order()?.items.reduce(
      (total, item) => total + item.quantity,
      0,
    ) ?? 0,
  );

  readonly cartSubtotal = computed(
    () => this.order()?.totalAmount ?? 0,
  );

  readonly deliveryAddress = this.checkoutState.deliveryAddress;

  readonly hasItems = computed(
    () => (this.order()?.items.length ?? 0) > 0,
  );

  readonly isOrderPayable = computed(
    () => this.order()?.status === 'CREATED',
  );

  readonly paymentMethod = signal<PaymentMethod>('upi');
  readonly paymentSimulation = signal<PaymentSimulation>('success');
  readonly processing = signal(false);
  readonly errorMessage = signal('');

  selectPaymentMethod(method: PaymentMethod): void {
    this.paymentMethod.set(method);
    this.errorMessage.set('');
  }

  selectPaymentSimulation(simulation: PaymentSimulation): void {
    this.paymentSimulation.set(simulation);
    this.errorMessage.set('');
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }

  private mapPaymentMethod(): DemoPaymentMethod {
    switch (this.paymentMethod()) {
      case 'card':
        return 'CARD';
      case 'wallet':
        return 'DIGITAL_WALLET';
      default:
        return 'UPI';
    }
  }

  payNow(): void {
    if (this.processing()) {
      return;
    }

    const currentOrder = this.order();

    if (!currentOrder) {
      this.errorMessage.set(
        'Order not found. Please return to checkout and create a new order.',
      );
      return;
    }

    if (!this.hasItems()) {
      this.errorMessage.set(
        'This order does not contain any products.',
      );
      return;
    }

    if (!this.isOrderPayable()) {
      this.errorMessage.set(
        `This order cannot be paid because its current status is ${currentOrder.status}.`,
      );
      return;
    }

    this.processing.set(true);
    this.errorMessage.set('');

    const succeeded = this.paymentSimulation() === 'success';
    const method = this.mapPaymentMethod();

    const updatedOrder = this.demoOrderState.updatePayment(
      this.orderId,
      method,
      succeeded,
    );

    if (!updatedOrder) {
      this.processing.set(false);
      this.errorMessage.set(
        'Unable to process this order. Please check its current status.',
      );
      return;
    }

    this.order.set(updatedOrder);

    if (succeeded) {
      this.storeState.clearCart();
    }

    this.router.navigate(['/payment/result'], {
      queryParams: {
        orderId: this.orderId,
        status: succeeded ? 'SUCCESS' : 'FAILURE',
        method,
      },
    });
  }

  goBackToCheckout(): void {
    this.router.navigate(['/checkout']);
  }
}
