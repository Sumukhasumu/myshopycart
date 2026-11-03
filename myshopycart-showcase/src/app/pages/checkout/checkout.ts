
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';
import { CheckoutStateService } from '../../core/services/checkout-state.service';
import { DemoOrderStateService } from '../../core/services/demo-order-state.service';
import type { DeliveryAddress } from '../../core/services/checkout-state.service';

@Component({
  selector: 'app-checkout-page',
  imports: [RouterLink],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class CheckoutPage {
  private readonly storeState = inject(StoreStateService);
  private readonly checkoutState = inject(CheckoutStateService);
  private readonly demoOrderState = inject(DemoOrderStateService);
  private readonly router = inject(Router);

  readonly cartItems = this.storeState.cartItems;
  readonly cartCount = this.storeState.cartCount;
  readonly cartSubtotal = this.storeState.cartSubtotal;

  readonly hasItems = computed(
    () => this.cartItems().length > 0,
  );

  readonly deliveryAddress = this.checkoutState.deliveryAddress;

  readonly errorMessage = signal('');
  readonly creatingOrder = signal(false);

  readonly demoAddressApplied = computed(() =>
    this.deliveryAddress().fullName === 'Demo Customer' &&
    this.deliveryAddress().mobile === '+91-1234567890',
  );

  updateAddressField(
    field: keyof DeliveryAddress,
    value: string,
  ): void {
    this.checkoutState.saveDeliveryAddress({
      ...this.deliveryAddress(),
      [field]: value,
    });

    this.errorMessage.set('');
  }

  useDemoAddress(): void {
    this.checkoutState.useDemoAddress();
    this.errorMessage.set('');
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }

  proceedToPayment(): void {
    if (this.creatingOrder()) {
      return;
    }

    if (!this.hasItems()) {
      this.errorMessage.set(
        'Your cart is empty. Add products before checkout.',
      );
      return;
    }

    const address = this.deliveryAddress();

    if (
      !address.fullName.trim() ||
      !address.mobile.trim() ||
      !address.addressLine.trim() ||
      !address.city.trim() ||
      !address.state.trim() ||
      !address.pincode.trim()
    ) {
      this.errorMessage.set(
        'Please complete all delivery address fields.',
      );
      return;
    }

    const validIndianMobile = /^[6-9]\d{9}$/.test(address.mobile.trim());
    const validDemoMobile = /^\+91-1234567890$/.test(address.mobile.trim());

    if (!validIndianMobile && !validDemoMobile) {
      this.errorMessage.set(
        'Please enter a valid 10-digit Indian mobile number.',
      );
      return;
    }

    if (!/^\d{6}$/.test(address.pincode.trim())) {
      this.errorMessage.set(
        'Please enter a valid 6-digit PIN code.',
      );
      return;
    }

    this.errorMessage.set('');
    this.creatingOrder.set(true);

    const order = this.demoOrderState.createOrder(
      this.cartItems(),
      address,
    );

    if (!order) {
      this.creatingOrder.set(false);
      this.errorMessage.set(
        'Unable to create the order. Please try again.',
      );
      return;
    }

    this.router.navigate(['/payment', order.id]).then((navigated) => {
      if (!navigated) {
        this.creatingOrder.set(false);
        this.errorMessage.set(
          'Order was created, but the payment page could not be opened. Please try again.',
        );
      }
    });
  }

  goBackToCart(): void {
    this.router.navigate(['/cart']);
  }
}