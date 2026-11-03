
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StoreStateService } from '../../core/services/store-state.service';
import { ProductCatalogService } from '../../core/services/product-catalog.service';

interface ProductCategory {
  id: string;
  name: string;
}

@Component({
  selector: 'app-products-page',
  imports: [],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class ProductsPage {
  private readonly pageSize = 2;
  private readonly storeState = inject(StoreStateService);
  private readonly productCatalog = inject(ProductCatalogService);

  readonly categories: ProductCategory[] = [
    { id: 'electronics', name: 'Electronics' },
    { id: 'mobiles', name: 'Mobiles' },
    { id: 'computers', name: 'Computers' },
    { id: 'accessories', name: 'Accessories' },
  ];

  readonly searchKeyword = signal('');
  readonly selectedCategoryId = signal('');
  readonly selectedSort = signal('');
  readonly currentPage = signal(0);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly wishlistProductIds = computed(
    () => new Set(
      this.storeState.wishlistItems().map((item) => item.id),
    ),
  );

  readonly selectedCategory = computed(() =>
    this.categories.find(
      (category) => category.id === this.selectedCategoryId(),
    ),
  );

  readonly pageTitle = computed(() => {
    const category = this.selectedCategory();
    const search = this.searchKeyword();

    if (category) {
      return `${category.name} Products`;
    }

    if (search) {
      return `Search results for "${search}"`;
    }

    return 'Products';
  });

  readonly pageDescription = computed(() => {
    const category = this.selectedCategory();
    const search = this.searchKeyword();

    if (category) {
      return `Explore products available in ${category.name}.`;
    }

    if (search) {
      return 'Products matching your search.';
    }

    return 'Explore our latest products and find something that fits your needs.';
  });

  readonly filteredProducts = computed(() => {
    const search = this.searchKeyword().toLowerCase();
    const categoryId = this.selectedCategoryId();
    const sort = this.selectedSort();

    let result = this.productCatalog.products().filter((product) => {
      const matchesCategory =
        !categoryId || product.categoryId === categoryId;

      const matchesSearch =
        !search ||
        product.name.toLowerCase().includes(search) ||
        product.description.toLowerCase().includes(search) ||
        product.categoryName.toLowerCase().includes(search);

      return matchesCategory && matchesSearch;
    });

    if (sort === 'price,asc') {
      result = [...result].sort((a, b) => a.price - b.price);
    } else if (sort === 'price,desc') {
      result = [...result].sort((a, b) => b.price - a.price);
    } else if (sort === 'name,asc') {
      result = [...result].sort((a, b) =>
        a.name.localeCompare(b.name),
      );
    } else if (sort === 'name,desc') {
      result = [...result].sort((a, b) =>
        b.name.localeCompare(a.name),
      );
    }

    return result;
  });

  readonly totalPages = computed(() =>
    Math.ceil(this.filteredProducts().length / this.pageSize),
  );

  readonly paginatedProducts = computed(() => {
    const start = this.currentPage() * this.pageSize;

    return this.filteredProducts().slice(
      start,
      start + this.pageSize,
    );
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {
    this.route.queryParamMap.subscribe((params) => {
      this.searchKeyword.set(params.get('search')?.trim() || '');
      this.selectedCategoryId.set(params.get('category')?.trim() || '');
      this.selectedSort.set(params.get('sort')?.trim() || '');

      const page = Number.parseInt(params.get('page') || '0', 10);

      this.currentPage.set(
        Number.isNaN(page) || page < 0 ? 0 : page,
      );
    });
  }

  updateSearch(value: string): void {
    this.updateQueryParams({
      search: value.trim() || null,
      page: null,
    });
  }

  updateCategory(categoryId: string): void {
    this.updateQueryParams({
      category: categoryId || null,
      search: null,
      page: null,
    });
  }

  updateSort(sort: string): void {
    this.updateQueryParams({
      sort: sort || null,
      page: null,
    });
  }

  updatePage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }

    this.updateQueryParams({
      page: page === 0 ? null : String(page),
    });
  }

  toggleWishlist(productId: number): void {
    const product = this.productCatalog.getProductById(productId);

    if (!product) {
      return;
    }

    this.storeState.toggleWishlist(product);
  }

  isInWishlist(productId: number): boolean {
    return this.storeState.isInWishlist(productId);
  }

  viewProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
  }

  addToCart(product: {
    id: number;
    name: string;
    description: string;
    price: number;
    categoryId: string;
    categoryName: string;
    image: string;
    rating: number;
    stock: number;
  }): void {
    this.storeState.addToCart(product);

    this.error.set(
      `${product.name} added to your demo cart.`,
    );
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(price);
  }

  private updateQueryParams(
    params: Record<string, string | null>,
  ): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: params,
      queryParamsHandling: 'merge',
    });
  }
}
