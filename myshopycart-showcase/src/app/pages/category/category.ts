
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import {
  Category,
  CategoryService,
} from '../../core/services/category.service';
import { ProductCatalogService } from '../../core/services/product-catalog.service';

@Component({
  selector: 'app-category-page',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './category.html',
  styleUrl: './category.css',
})
export class CategoryPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly categoryService = inject(CategoryService);
  private readonly productCatalog = inject(ProductCatalogService);

  readonly pageSize = 2;

  readonly categoryId = signal('');
  readonly currentPage = signal(0);

  readonly category = computed<Category | null>(
    () =>
      this.categoryService.categories().find(
        (item) => item.id === this.categoryId(),
      ) ?? null,
  );

  readonly categoryProducts = computed(() =>
    this.productCatalog.products().filter(
      (product) => product.categoryId === this.categoryId(),
    ),
  );

  readonly totalPages = computed(() =>
    Math.ceil(this.categoryProducts().length / this.pageSize),
  );

  readonly paginatedProducts = computed(() => {
    const start = this.currentPage() * this.pageSize;

    return this.categoryProducts().slice(
      start,
      start + this.pageSize,
    );
  });

  constructor() {
    this.route.paramMap.subscribe((params) => {
      this.categoryId.set(params.get('categoryId') ?? '');
      this.currentPage.set(0);
    });

    this.route.queryParamMap.subscribe((params) => {
      const pageParam = Number.parseInt(params.get('page') ?? '0', 10);

      this.currentPage.set(
        Number.isNaN(pageParam) || pageParam < 0 ? 0 : pageParam,
      );
    });
  }

  goToPage(nextPage: number): void {
    if (nextPage < 0 || nextPage >= this.totalPages()) {
      return;
    }

    const queryParams = { ...this.route.snapshot.queryParams };

    if (nextPage === 0) {
      delete queryParams['page'];
    } else {
      queryParams['page'] = String(nextPage);
    }

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
    });
  }
}
