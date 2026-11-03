
import { Injectable, signal } from '@angular/core';

export interface Category {
  id: string;
  name: string;
  description: string;
  parentCategoryId: string | null;
}

export interface CategoryPayload {
  name: string;
  description: string;
  parentCategoryId: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly categoriesState = signal<Category[]>([
    {
      id: 'electronics',
      name: 'Electronics',
      description: 'Electronic devices and accessories.',
      parentCategoryId: null,
    },
    {
      id: 'mobiles',
      name: 'Mobiles',
      description: 'Smartphones and mobile devices.',
      parentCategoryId: 'electronics',
    },
    {
      id: 'computers',
      name: 'Computers',
      description: 'Laptops and computing devices.',
      parentCategoryId: 'electronics',
    },
    {
      id: 'accessories',
      name: 'Accessories',
      description: 'Accessories for electronic devices.',
      parentCategoryId: 'electronics',
    },
  ]);

  readonly categories = this.categoriesState.asReadonly();

  createCategory(payload: CategoryPayload): void {
    const baseId = payload.name
      .trim()
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '') || 'category';

    let id = baseId;
    let suffix = 2;

    while (this.categoriesState().some((item) => item.id === id)) {
      id = `${baseId}-${suffix}`;
      suffix++;
    }

    const category: Category = {
      id,
      ...payload,
    };

    this.categoriesState.update((categories) => [
      ...categories,
      category,
    ]);
  }

  updateCategory(id: string, payload: CategoryPayload): void {
    this.categoriesState.update((categories) =>
      categories.map((category) =>
        category.id === id
          ? { ...category, ...payload }
          : category,
      ),
    );
  }

  deleteCategory(id: string): void {
    this.categoriesState.update((categories) =>
      categories.filter((category) => category.id !== id),
    );
  }
}
