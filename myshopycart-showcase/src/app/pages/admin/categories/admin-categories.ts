
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  Category,
  CategoryPayload,
  CategoryService,
} from '../../../core/services/category.service';

interface CategoryForm {
  name: string;
  description: string;
  parentCategoryId: string;
}

const EMPTY_FORM: CategoryForm = {
  name: '',
  description: '',
  parentCategoryId: '',
};

@Component({
  selector: 'app-admin-categories-page',
  imports: [FormsModule],
  templateUrl: './admin-categories.html',
  styleUrl: './admin-categories.css',
})
export class AdminCategoriesPage {
  private readonly categoryService = inject(CategoryService);

  readonly categories = this.categoryService.categories;

  readonly form = signal<CategoryForm>({ ...EMPTY_FORM });
  readonly editingId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deletingId = signal<string | null>(null);
  readonly error = signal('');
  readonly message = signal('');

  resetForm(): void {
    this.form.set({ ...EMPTY_FORM });
    this.editingId.set(null);
    this.error.set('');
    this.message.set('');
  }

  updateField(
    field: keyof CategoryForm,
    value: string,
  ): void {
    this.form.update((current) => ({
      ...current,
      [field]: value,
    }));
  }

  handleEdit(category: Category): void {
    this.editingId.set(category.id);

    this.form.set({
      name: category.name || '',
      description: category.description || '',
      parentCategoryId: category.parentCategoryId || '',
    });

    this.error.set('');
    this.message.set('');
  }

  handleSubmit(event: Event): void {
    event.preventDefault();

    this.error.set('');
    this.message.set('');

    const currentForm = this.form();
    const name = currentForm.name.trim();
    const description = currentForm.description.trim();

    if (!name) {
      this.error.set('Category name is required.');
      return;
    }

    const payload: CategoryPayload = {
      name,
      description,
      parentCategoryId:
        currentForm.parentCategoryId.trim() || null,
    };

    this.saving.set(true);

    try {
      const currentEditingId = this.editingId();

      if (currentEditingId) {
        this.categoryService.updateCategory(
          currentEditingId,
          payload,
        );

        this.message.set('Category updated successfully.');
      } else {
        this.categoryService.createCategory(payload);

        this.message.set('Category created successfully.');
      }

      this.form.set({ ...EMPTY_FORM });
      this.editingId.set(null);
    } catch {
      this.error.set(
        'Unable to save the category. Please try again.',
      );
    } finally {
      this.saving.set(false);
    }
  }

  handleDelete(category: Category): void {
    const confirmed = window.confirm(
      `Delete the category "${category.name}"? This operation cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(category.id);
    this.error.set('');
    this.message.set('');

    try {
      this.categoryService.deleteCategory(category.id);

      if (this.editingId() === category.id) {
        this.form.set({ ...EMPTY_FORM });
        this.editingId.set(null);
      }

      this.message.set('Category deleted successfully.');
    } catch {
      this.error.set(
        'Unable to delete the category. It may still be in use.',
      );
    } finally {
      this.deletingId.set(null);
    }
  }
}
