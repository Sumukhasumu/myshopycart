
import { Injectable, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

export interface CatalogProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: string;
  categoryName: string;
  image: string;
  imageUrls: string[];
  rating: number;
  stock: number;
  distributorId: string;
  attributes: Record<string, string>;
}

const INITIAL_PRODUCTS: CatalogProduct[] = [
  {
    id: 1,
    name: 'Dell Inspiron Laptop',
    description: 'Reliable laptop for work, study and everyday computing.',
    price: 58999,
    categoryId: 'computers',
    categoryName: 'Computers',
    image: '/product-images/dell-laptop.svg',
    imageUrls: ['/product-images/dell-laptop.svg'],
    rating: 4.5,
    stock: 12,
    distributorId: '',
    attributes: {
      Brand: 'Dell',
      Category: 'Laptop',
      Usage: 'Work and everyday computing',
    },
  },
  {
    id: 2,
    name: 'HP Pavilion Laptop',
    description: 'Powerful everyday laptop with a modern design.',
    price: 64999,
    categoryId: 'computers',
    categoryName: 'Computers',
    image: '/product-images/hp-laptop.svg',
    imageUrls: ['/product-images/hp-laptop.svg'],
    rating: 4.3,
    stock: 8,
    distributorId: '',
    attributes: {
      Brand: 'HP',
      Category: 'Laptop',
      Usage: 'Everyday computing',
    },
  },
  {
    id: 3,
    name: 'Samsung Galaxy Smartphone',
    description: 'Smartphone with a vibrant display and versatile camera.',
    price: 24999,
    categoryId: 'mobiles',
    categoryName: 'Mobiles',
    image: '/product-images/samsung-mobile.svg',
    imageUrls: ['/product-images/samsung-mobile.svg'],
    rating: 4.4,
    stock: 20,
    distributorId: '',
    attributes: {
      Brand: 'Samsung',
      Category: 'Smartphone',
      Usage: 'Communication and entertainment',
    },
  },
  {
    id: 4,
    name: 'Wireless Headphones',
    description: 'Comfortable wireless headphones for music and calls.',
    price: 2999,
    categoryId: 'accessories',
    categoryName: 'Accessories',
    image: '',
    imageUrls: [],
    rating: 4.2,
    stock: 25,
    distributorId: '',
    attributes: {
      Connectivity: 'Wireless',
      Category: 'Audio accessory',
    },
  },
  {
    id: 5,
    name: 'Bluetooth Speaker',
    description: 'Portable speaker for music at home or on the go.',
    price: 1999,
    categoryId: 'electronics',
    categoryName: 'Electronics',
    image: '',
    imageUrls: [],
    rating: 4.1,
    stock: 18,
    distributorId: '',
    attributes: {
      Connectivity: 'Bluetooth',
      Category: 'Speaker',
    },
  },
  {
    id: 6,
    name: 'Wireless Keyboard',
    description: 'Compact wireless keyboard for everyday productivity.',
    price: 1499,
    categoryId: 'accessories',
    categoryName: 'Accessories',
    image: '',
    imageUrls: [],
    rating: 4.0,
    stock: 30,
    distributorId: '',
    attributes: {
      Connectivity: 'Wireless',
      Category: 'Keyboard',
    },
  },
];

@Injectable({
  providedIn: 'root',
})
export class ProductCatalogService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly storageKey = 'myshopycart_products';

  private readonly productsState = signal<CatalogProduct[]>(
    this.loadProducts(),
  );

  readonly products = this.productsState.asReadonly();

  readonly totalProducts = computed(
    () => this.productsState().length,
  );

  private loadProducts(): CatalogProduct[] {
    if (!isPlatformBrowser(this.platformId)) {
      return INITIAL_PRODUCTS.map((product) => ({ ...product }));
    }

    try {
      const savedProducts = localStorage.getItem(this.storageKey);

      if (savedProducts !== null) {
        const parsedProducts: unknown = JSON.parse(savedProducts);

        if (Array.isArray(parsedProducts)) {
          return parsedProducts as CatalogProduct[];
        }
      }
    } catch {
      // Use initial products if stored data cannot be read.
    }

    return INITIAL_PRODUCTS.map((product) => ({ ...product }));
  }

  private saveProducts(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    try {
      localStorage.setItem(
        this.storageKey,
        JSON.stringify(this.productsState()),
      );
    } catch {
      // Keep the current in-memory catalogue if storage is unavailable.
    }
  }

  getProductById(id: number): CatalogProduct | undefined {
    return this.productsState().find(
      (product) => product.id === id,
    );
  }

  createProduct(
    product: Omit<CatalogProduct, 'id'>,
  ): void {
    const nextId =
      Math.max(0, ...this.productsState().map((item) => item.id)) + 1;

    this.productsState.update((products) => [
      ...products,
      {
        ...product,
        id: nextId,
      },
    ]);

    this.saveProducts();
  }

  updateProduct(
    id: number,
    changes: Partial<Omit<CatalogProduct, 'id'>>,
  ): void {
    this.productsState.update((products) =>
      products.map((product) =>
        product.id === id
          ? { ...product, ...changes }
          : product,
      ),
    );

    this.saveProducts();
  }

  deleteProduct(id: number): void {
    this.productsState.update((products) =>
      products.filter((product) => product.id !== id),
    );

    this.saveProducts();
  }
}
