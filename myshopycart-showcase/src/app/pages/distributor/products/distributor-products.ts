
import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import {
  CatalogProduct,
  ProductCatalogService,
} from '../../../core/services/product-catalog.service';
import { CategoryService } from '../../../core/services/category.service';

@Component({
  selector: 'app-distributor-products-page',
  imports: [RouterLink, CurrencyPipe, FormsModule],
  templateUrl: './distributor-products.html',
  styleUrl: './distributor-products.css',
})
export class DistributorProductsPage {
  private readonly authService = inject(AuthService);
  private readonly productCatalog = inject(ProductCatalogService);
  private readonly categoryService = inject(CategoryService);

  readonly pageSize = 12;
  readonly currentPage = signal(0);
  readonly user = this.authService.currentUser;
  readonly categories = this.categoryService.categories;

  readonly myProducts = computed(() => {
    const username = this.user()?.username;

    if (!username) {
      return [];
    }

    return this.productCatalog.products().filter(
      (product) => product.distributorId === username,
    );
  });

  readonly totalElements = computed(() => this.myProducts().length);

  readonly totalPages = computed(() =>
    Math.ceil(this.totalElements() / this.pageSize),
  );

  readonly paginatedProducts = computed(() => {
    const start = this.currentPage() * this.pageSize;

    return this.myProducts().slice(start, start + this.pageSize);
  });

  readonly isFormVisible = signal(false);
  readonly editingProductId = signal<number | null>(null);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  productName = '';
  description = '';
  price: number | null = null;
  categoryId = '';
  stock: number | null = 0;

  openAddForm(): void {
    this.resetForm();
    this.editingProductId.set(null);
    this.isFormVisible.set(true);
  }

  openEditForm(product: CatalogProduct): void {
    this.resetMessages();

    this.editingProductId.set(product.id);
    this.productName = product.name;
    this.description = product.description;
    this.price = product.price;
    this.categoryId = product.categoryId;
    this.stock = product.stock;
    this.isFormVisible.set(true);
  }

  closeForm(): void {
    this.isFormVisible.set(false);
    this.resetForm();
  }

  saveProduct(): void {
    this.resetMessages();

    const username = this.user()?.username;

    if (!username) {
      this.errorMessage.set('Unable to identify the distributor account.');
      return;
    }

    if (
      !this.productName.trim() ||
      !this.description.trim() ||
      !this.categoryId ||
      this.price === null ||
      !Number.isFinite(this.price) ||
      this.price <= 0 ||
      this.stock === null ||
      !Number.isInteger(this.stock) ||
      this.stock < 0
    ) {
      this.errorMessage.set(
        'Please enter valid product details, price and stock.',
      );
      return;
    }

    const selectedCategory = this.categories().find(
      (category) => category.id === this.categoryId,
    );

    if (!selectedCategory) {
      this.errorMessage.set('Please select a valid category.');
      return;
    }

    const existingId = this.editingProductId();

    if (existingId !== null) {
      const existingProduct = this.productCatalog.getProductById(existingId);

      if (!existingProduct || existingProduct.distributorId !== username) {
        this.errorMessage.set('You cannot edit this product.');
        return;
      }

      this.productCatalog.updateProduct(existingId, {
        name: this.productName.trim(),
        description: this.description.trim(),
        price: this.price,
        categoryId: selectedCategory.id,
        categoryName: selectedCategory.name,
        stock: this.stock,
      });

      this.successMessage.set('Product updated successfully.');
    } else {
      const newProduct: Omit<CatalogProduct, 'id'> = {
        name: this.productName.trim(),
        description: this.description.trim(),
        price: this.price,
        categoryId: selectedCategory.id,
        categoryName: selectedCategory.name,
        image: '',
        imageUrls: [],
        rating: 0,
        stock: this.stock,
        distributorId: username,
        attributes: {},
      };

      this.productCatalog.createProduct(newProduct);
      this.successMessage.set('Product added successfully.');
    }

    this.closeForm();
    this.currentPage.set(
      Math.max(0, this.totalPages() - 1),
    );
  }

  deleteProduct(product: CatalogProduct): void {
    this.resetMessages();

    const username = this.user()?.username;

    if (!username || product.distributorId !== username) {
      this.errorMessage.set('You cannot delete this product.');
      return;
    }

    this.productCatalog.deleteProduct(product.id);
    this.successMessage.set('Product deleted successfully.');

    if (this.currentPage() >= this.totalPages() && this.currentPage() > 0) {
      this.currentPage.update((page) => page - 1);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update((page) => page - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage() + 1 < this.totalPages()) {
      this.currentPage.update((page) => page + 1);
    }
  }

  private resetForm(): void {
    this.productName = '';
    this.description = '';
    this.price = null;
    this.categoryId = '';
    this.stock = 0;
  }

  private resetMessages(): void {
    this.successMessage.set('');
    this.errorMessage.set('');
  }
}
