
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';

@Component({
  selector: 'app-cart-page',
  imports: [RouterLink],
  templateUrl: './cart.html',
  styleUrl: './cart.css',
})
export class CartPage {
  private readonly storeState = inject(StoreStateService);
  private readonly router = inject(Router);

  readonly cartItems = this.storeState.cartItems;
  readonly cartCount = this.storeState.cartCount;
  readonly cartSubtotal = this.storeState.cartSubtotal;

  readonly message = signal('');

  readonly hasItems = computed(
    () => this.cartItems().length > 0,
  );

  increaseQuantity(productId: number): void {
    const item = this.cartItems().find(
      (product) => product.id === productId,
    );

    if (!item) {
      return;
    }

    if (item.quantity >= item.stock) {
      this.message.set(
        `Only ${item.stock} units of ${item.name} are available.`,
      );
      return;
    }

    this.storeState.updateCartQuantity(
      productId,
      item.quantity + 1,
    );

    this.message.set('');
  }

  decreaseQuantity(productId: number): void {
    const item = this.cartItems().find(
      (product) => product.id === productId,
    );

    if (!item) {
      return;
    }

    if (item.quantity <= 1) {
      return;
    }

    this.storeState.updateCartQuantity(
      productId,
      item.quantity - 1,
    );

    this.message.set('');
  }

  removeItem(productId: number): void {
    this.storeState.removeFromCart(productId);
    this.message.set('Product removed from your demo cart.');
  }

  clearCart(): void {
    this.storeState.clearCart();
    this.message.set('Your demo cart has been cleared.');
  }

  getItemTotal(price: number, quantity: number): number {
    return price * quantity;
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }

  checkout(): void {
  this.router.navigate(['/checkout']);
  }
}
