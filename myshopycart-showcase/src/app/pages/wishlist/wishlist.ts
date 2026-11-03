
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';

@Component({
  selector: 'app-wishlist-page',
  imports: [RouterLink],
  templateUrl: './wishlist.html',
  styleUrl: './wishlist.css',
})
export class WishlistPage {
  private readonly storeState = inject(StoreStateService);

  readonly wishlistItems = this.storeState.wishlistItems;
  readonly wishlistCount = this.storeState.wishlistCount;

  readonly message = signal('');

  readonly hasItems = computed(
    () => this.wishlistItems().length > 0,
  );

  removeItem(productId: number): void {
    this.storeState.removeFromWishlist(productId);
    this.message.set('Product removed from your demo wishlist.');
  }

  addToCart(productId: number): void {
  const item = this.wishlistItems().find(
    (product) => product.id === productId,
  );

  if (!item) {
    return;
  }

  if (item.stock < 1) {
    this.message.set(`${item.name} is currently out of stock.`);
    return;
  }

  this.storeState.addToCart(item);
  this.storeState.removeFromWishlist(productId);

  this.message.set(`${item.name} moved to your demo cart.`);
}

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }
}
