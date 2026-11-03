import { beforeEach, describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';
import productApi from './productApi';

vi.mock('./httpClient', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('productApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets paginated products', async () => {
    const response = {
      content: [],
      totalElements: 0,
      totalPages: 0,
    };

    httpClient.get.mockResolvedValue(response);

    await expect(
      productApi.getProducts({
        page: 0,
        size: 12,
      }),
    ).resolves.toEqual(response);

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products?page=0&size=12',
    );
  });

  it('gets products with sorting', async () => {
    httpClient.get.mockResolvedValue({
      content: [],
    });

    await productApi.getProducts({
      page: 0,
      size: 12,
      sort: 'name,asc',
    });

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products?page=0&size=12&sort=name%2Casc',
    );
  });

  it('gets a product by id', async () => {
    httpClient.get.mockResolvedValue({
      id: 'product-test-1',
      name: 'Dell Laptop',
    });

    await productApi.getProductById(
      'product-test-1',
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/product-test-1',
    );
  });

  it('gets products by category', async () => {
    httpClient.get.mockResolvedValue({
      content: [],
    });

    await productApi.getProductsByCategory(
      'cat-electronics',
      {
        page: 0,
        size: 12,
      },
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/category/cat-electronics?page=0&size=12',
    );
  });

  it('searches products with a keyword', async () => {
    httpClient.get.mockResolvedValue({
      content: [],
    });

    await productApi.searchProducts({
      keyword: 'phone',
      page: 0,
      size: 12,
    });

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/search?keyword=phone&page=0&size=12',
    );
  });

  it('trims the search keyword', async () => {
    httpClient.get.mockResolvedValue({
      content: [],
    });

    await productApi.searchProducts({
      keyword: '  phone  ',
      page: 0,
      size: 12,
    });

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/products/search?keyword=phone&page=0&size=12',
    );
  });

  it('rejects an empty search keyword', () => {
    expect(() =>
      productApi.searchProducts({
        keyword: '   ',
        page: 0,
        size: 12,
      }),
    ).toThrow('Search keyword is required');

    expect(httpClient.get).not.toHaveBeenCalled();
  });
});