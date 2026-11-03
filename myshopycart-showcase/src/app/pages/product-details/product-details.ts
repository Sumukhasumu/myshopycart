
import { Component, computed, inject, signal } from '@angular/core';
import { KeyValuePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';
import { ProductCatalogService } from '../../core/services/product-catalog.service';

@Component({
  selector: 'app-product-details-page',
  imports: [RouterLink, KeyValuePipe],
  templateUrl: './product-details.html',
  styleUrl: './product-details.css',
})
export class ProductDetailsPage {
  private readonly storeState = inject(StoreStateService);
  private readonly productCatalog = inject(ProductCatalogService);

  readonly productId = signal<number>(0);

  readonly product = computed(
    () =>
      this.productCatalog.products().find(
        (item) => item.id === this.productId(),
      ) ?? null,
  );

  readonly quantity = signal(1);

  readonly isInWishlist = computed(() => {
    const item = this.product();
    return item ? this.storeState.isInWishlist(item.id) : false;
  });

  readonly cartMessage = signal('');
  readonly wishlistMessage = signal('');

  constructor(private readonly route: ActivatedRoute) {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('productId'));

      this.productId.set(Number.isInteger(id) ? id : 0);
      this.quantity.set(1);
      this.cartMessage.set('');
      this.wishlistMessage.set('');
    });
  }

  increaseQuantity(): void {
    const item = this.product();

    if (item && this.quantity() < item.stock) {
      this.quantity.update((value) => value + 1);
    }
  }

  decreaseQuantity(): void {
    if (this.quantity() > 1) {
      this.quantity.update((value) => value - 1);
    }
  }

  addToCart(): void {
    const item = this.product();

    if (!item) {
      return;
    }

    this.storeState.addToCart(item, this.quantity());

    this.cartMessage.set(
      `${this.quantity()} × ${item.name} added to the demo cart.`,
    );

    this.wishlistMessage.set('');
  }

  toggleWishlist(): void {
    const item = this.product();

    if (!item) {
      return;
    }

    const wasInWishlist = this.storeState.isInWishlist(item.id);

    this.storeState.toggleWishlist(item);

    this.wishlistMessage.set(
      wasInWishlist
        ? 'Product removed from your demo wishlist.'
        : 'Product added to your demo wishlist.',
    );

    this.cartMessage.set('');
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }
}
