
import { beforeEach, describe, expect, it, vi } from 'vitest';
import categoryApi from './categoryApi';
import httpClient from './httpClient';

vi.mock('./httpClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('categoryApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets all categories', async () => {
    const categories = [
      {
        id: 'cat-electronics',
        name: 'Electronics',
        description: 'Electronic devices and computer products',
        parentCategoryId: null,
      },
      {
        id: 'cat-mobile',
        name: 'Mobile',
        description: 'Mobile phones and related devices',
        parentCategoryId: null,
      },
    ];

    httpClient.get.mockResolvedValue(categories);

    const result = await categoryApi.getCategories();

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/categories',
    );
    expect(result).toEqual(categories);
  });

  it('gets a category by id', async () => {
    const category = {
      id: 'cat-electronics',
      name: 'Electronics',
      description: 'Electronic devices and computer products',
      parentCategoryId: null,
    };

    httpClient.get.mockResolvedValue(category);

    const result = await categoryApi.getCategoryById(
      'cat-electronics',
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/categories/cat-electronics',
    );
    expect(result).toEqual(category);
  });

  it('encodes the category id when getting a category', async () => {
    httpClient.get.mockResolvedValue({
      id: 'cat electronics',
      name: 'Electronics',
    });

    await categoryApi.getCategoryById('cat electronics');

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/categories/cat%20electronics',
    );
  });

  it('creates a category', async () => {
    const category = {
      name: 'Accessories',
      description: 'Accessories and add-ons',
      parentCategoryId: null,
    };
    const createdCategory = { id: 'cat-accessories', ...category };

    httpClient.post.mockResolvedValue(createdCategory);

    const result = await categoryApi.createCategory(category);

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/products/categories',
      category,
    );
    expect(result).toEqual(createdCategory);
  });

  it('updates a category', async () => {
    const category = {
      name: 'Updated Electronics',
      description: 'Updated description',
      parentCategoryId: null,
    };

    httpClient.put.mockResolvedValue({
      id: 'cat-electronics',
      ...category,
    });

    const result = await categoryApi.updateCategory(
      'cat-electronics',
      category,
    );

    expect(httpClient.put).toHaveBeenCalledWith(
      '/api/products/categories/cat-electronics',
      category,
    );
    expect(result.name).toBe('Updated Electronics');
  });

  it('encodes the category id when updating a category', async () => {
    httpClient.put.mockResolvedValue({ id: 'cat electronics' });

    await categoryApi.updateCategory('cat electronics', {
      name: 'Electronics',
    });

    expect(httpClient.put).toHaveBeenCalledWith(
      '/api/products/categories/cat%20electronics',
      { name: 'Electronics' },
    );
  });

  it('deletes a category', async () => {
    httpClient.delete.mockResolvedValue(undefined);

    await categoryApi.deleteCategory('cat-electronics');

    expect(httpClient.delete).toHaveBeenCalledWith(
      '/api/products/categories/cat-electronics',
    );
  });

  it('encodes the category id when deleting a category', async () => {
    httpClient.delete.mockResolvedValue(undefined);

    await categoryApi.deleteCategory('cat electronics');

    expect(httpClient.delete).toHaveBeenCalledWith(
      '/api/products/categories/cat%20electronics',
    );
  });
});