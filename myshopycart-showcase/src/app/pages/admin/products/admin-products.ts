
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CatalogProduct,
  ProductCatalogService,
} from '../../../core/services/product-catalog.service';
import { CategoryService } from '../../../core/services/category.service';

interface ProductForm {
  name: string;
  description: string;
  price: string;
  categoryId: string;
  distributorId: string;
  attributesJson: string;
  imageUrlsText: string;
}

const EMPTY_FORM: ProductForm = {
  name: '',
  description: '',
  price: '',
  categoryId: '',
  distributorId: '',
  attributesJson: '{}',
  imageUrlsText: '',
};

@Component({
  selector: 'app-admin-products-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-products.html',
  styleUrl: './admin-products.css',
})
export class AdminProductsPage {
  private readonly productCatalog = inject(ProductCatalogService);
  private readonly categoryService = inject(CategoryService);

  readonly products = this.productCatalog.products;
  readonly categories = this.categoryService.categories;

  readonly pageSize = 12;
  readonly currentPage = signal(1);

  readonly totalElements = computed(() => this.products().length);

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.totalElements() / this.pageSize)),
  );

  readonly paginatedProducts = computed(() => {
    const startIndex = (this.currentPage() - 1) * this.pageSize;

    return this.products().slice(
      startIndex,
      startIndex + this.pageSize,
    );
  });

  form: ProductForm = { ...EMPTY_FORM };

  readonly editingId = signal<number | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deletingId = signal<number | null>(null);
  readonly error = signal('');
  readonly message = signal('');

  updateField(
    field: keyof ProductForm,
    value: string,
  ): void {
    this.form = {
      ...this.form,
      [field]: value,
    };
  }

  resetForm(): void {
    this.form = { ...EMPTY_FORM };
    this.editingId.set(null);
    this.error.set('');
  }

  handleEdit(product: CatalogProduct): void {
    this.error.set('');
    this.message.set('');

    this.editingId.set(product.id);

    this.form = {
      name: product.name,
      description: product.description,
      price: String(product.price),
      categoryId: product.categoryId,
      distributorId: product.distributorId,
      attributesJson: JSON.stringify(product.attributes, null, 2),
      imageUrlsText: product.imageUrls.join('\n'),
    };

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  handleSubmit(): void {
    this.error.set('');
    this.message.set('');

    const name = this.form.name.trim();
    const description = this.form.description.trim();
    const price = Number(this.form.price);
    const categoryId = this.form.categoryId;
    const distributorId = this.form.distributorId.trim();

    if (!name) {
      this.error.set('Product name is required.');
      return;
    }

    if (name.length > 150) {
      this.error.set('Product name cannot exceed 150 characters.');
      return;
    }

    if (!description) {
      this.error.set('Product description is required.');
      return;
    }

    if (description.length > 1000) {
      this.error.set('Product description cannot exceed 1000 characters.');
      return;
    }

    if (!this.form.price.trim() || !Number.isFinite(price) || price <= 0) {
      this.error.set('Please enter a valid product price greater than zero.');
      return;
    }

    const selectedCategory = this.categories().find(
      (category) => category.id === categoryId,
    );

    if (!selectedCategory) {
      this.error.set('Please select a valid product category.');
      return;
    }

    if (!this.editingId() && !distributorId) {
      this.error.set('Distributor ID is required when creating a product.');
      return;
    }

    let attributes: Record<string, string>;

    try {
      const parsedAttributes: unknown = JSON.parse(
        this.form.attributesJson || '{}',
      );

      if (
        parsedAttributes === null ||
        typeof parsedAttributes !== 'object' ||
        Array.isArray(parsedAttributes)
      ) {
        this.error.set('Product attributes must be a valid JSON object.');
        return;
      }

      attributes = Object.fromEntries(
        Object.entries(parsedAttributes).map(([key, value]) => [
          key,
          String(value),
        ]),
      );
    } catch {
      this.error.set('Product attributes must contain valid JSON.');
      return;
    }

    const imageUrls = this.form.imageUrlsText
      .split('\n')
      .map((url) => url.trim())
      .filter(Boolean);

    const existingProduct = this.editingId()
      ? this.products().find(
          (product) => product.id === this.editingId(),
        )
      : undefined;

    this.saving.set(true);

    try {
      const productData = {
        name,
        description,
        price,
        categoryId: selectedCategory.id,
        categoryName: selectedCategory.name,
        image: imageUrls[0] ?? '',
        imageUrls,
        rating: existingProduct?.rating ?? 0,
        stock: existingProduct?.stock ?? 0,
        distributorId: existingProduct?.distributorId ?? distributorId,
        attributes,
      };

      if (this.editingId() !== null) {
        this.productCatalog.updateProduct(
          this.editingId()!,
          productData,
        );

        this.message.set('Product updated successfully.');
      } else {
        this.productCatalog.createProduct(productData);
        this.message.set('Product created successfully.');
        this.currentPage.set(1);
      }

      this.resetForm();
    } catch {
      this.error.set('Unable to save the product. Please try again.');
    } finally {
      this.saving.set(false);
    }
  }

  handleDelete(product: CatalogProduct): void {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${product.name}"?`,
    );

    if (!confirmed) {
      return;
    }

    this.error.set('');
    this.message.set('');
    this.deletingId.set(product.id);

    try {
      this.productCatalog.deleteProduct(product.id);

      if (this.editingId() === product.id) {
        this.resetForm();
      }

      if (
        this.currentPage() > this.totalPages()
      ) {
        this.currentPage.set(this.totalPages());
      }

      this.message.set('Product deleted successfully.');
    } catch {
      this.error.set('Unable to delete the product. Please try again.');
    } finally {
      this.deletingId.set(null);
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);
  }
}
