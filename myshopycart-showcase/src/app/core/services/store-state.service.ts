
import { Injectable, computed, signal } from '@angular/core';

export interface StoreProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: string;
  categoryName: string;
  image: string;
  rating: number;
  stock: number;
}

export interface CartItem extends StoreProduct {
  quantity: number;
}

@Injectable({
  providedIn: 'root',
})
export class StoreStateService {
  private readonly _cartItems = signal<CartItem[]>([]);
  private readonly _wishlistItems = signal<StoreProduct[]>([]);

  readonly cartItems = this._cartItems.asReadonly();
  readonly wishlistItems = this._wishlistItems.asReadonly();

  readonly cartCount = computed(() =>
    this._cartItems().reduce(
      (total, item) => total + item.quantity,
      0,
    ),
  );

  readonly wishlistCount = computed(
    () => this._wishlistItems().length,
  );

  readonly cartSubtotal = computed(() =>
    this._cartItems().reduce(
      (total, item) => total + item.price * item.quantity,
      0,
    ),
  );

  addToCart(product: StoreProduct, quantity = 1): void {
    if (quantity < 1 || product.stock < 1) {
      return;
    }

    this._cartItems.update((items) => {
      const existingItem = items.find(
        (item) => item.id === product.id,
      );

      if (existingItem) {
        return items.map((item) =>
          item.id === product.id
            ? {
                ...item,
                quantity: Math.min(
                  item.quantity + quantity,
                  item.stock,
                ),
              }
            : item,
        );
      }

      return [
        ...items,
        {
          ...product,
          quantity: Math.min(quantity, product.stock),
        },
      ];
    });
  }

  updateCartQuantity(productId: number, quantity: number): void {
    if (quantity < 1) {
      this.removeFromCart(productId);
      return;
    }

    this._cartItems.update((items) =>
      items.map((item) =>
        item.id === productId
          ? {
              ...item,
              quantity: Math.min(quantity, item.stock),
            }
          : item,
      ),
    );
  }

  removeFromCart(productId: number): void {
    this._cartItems.update((items) =>
      items.filter((item) => item.id !== productId),
    );
  }

  clearCart(): void {
    this._cartItems.set([]);
  }

  addToWishlist(product: StoreProduct): void {
    this._wishlistItems.update((items) => {
      if (items.some((item) => item.id === product.id)) {
        return items;
      }

      return [...items, product];
    });
  }

  removeFromWishlist(productId: number): void {
    this._wishlistItems.update((items) =>
      items.filter((item) => item.id !== productId),
    );
  }

  isInWishlist(productId: number): boolean {
    return this._wishlistItems().some(
      (item) => item.id === productId,
    );
  }

  toggleWishlist(product: StoreProduct): void {
    if (this.isInWishlist(product.id)) {
      this.removeFromWishlist(product.id);
    } else {
      this.addToWishlist(product);
    }
  }
}
