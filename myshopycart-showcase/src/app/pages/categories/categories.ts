
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Category, CategoryService } from '../../core/services/category.service';

@Component({
  selector: 'app-categories-page',
  imports: [RouterLink],
  templateUrl: './categories.html',
  styleUrl: './categories.css',
})
export class CategoriesPage {
  private readonly categoryService = inject(CategoryService);

  readonly categories = this.categoryService.categories;

  readonly parentCategories = computed(() =>
    this.categories().filter(
      (category) => category.parentCategoryId === null,
    ),
  );

  readonly getSubcategories = (parentId: string): Category[] =>
    this.categories().filter(
      (category) => category.parentCategoryId === parentId,
    );
}
