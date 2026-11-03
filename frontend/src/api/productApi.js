import httpClient from './httpClient';

function buildQueryString({
  page = 0,
  size = 12,
  sort,
} = {}) {
  const params = new URLSearchParams();

  params.set('page', page);
  params.set('size', size);

  if (sort) {
    params.set('sort', sort);
  }

  return params.toString();
}

const productApi = {
  getProducts(options = {}) {
    const queryString = buildQueryString(options);

    return httpClient.get(`/api/products?${queryString}`);
  },

  getMyProducts(options = {}) {
    const queryString = buildQueryString(options);

    return httpClient.get(`/api/products/my-products?${queryString}`);
  },

  getProductById(productId) {
    return httpClient.get(
      `/api/products/${encodeURIComponent(productId)}`,
    );
  },

  createProduct(product) {
    return httpClient.post('/api/products', product);
  },

  updateProduct(productId, product) {
    return httpClient.put(
      `/api/products/${encodeURIComponent(productId)}`,
      product,
    );
  },

  deleteProduct(productId) {
    return httpClient.delete(
      `/api/products/${encodeURIComponent(productId)}`,
    );
  },

  getProductsByCategory(categoryId, options = {}) {
    const queryString = buildQueryString(options);

    return httpClient.get(
      `/api/products/category/${encodeURIComponent(categoryId)}?${queryString}`,
    );
  },

  searchProducts({
    keyword,
    page = 0,
    size = 12,
    sort,
  }) {
    const normalizedKeyword = keyword?.trim() || '';

    if (!normalizedKeyword) {
      throw new Error('Search keyword is required');
    }

    const params = new URLSearchParams();

    params.set('keyword', normalizedKeyword);
    params.set('page', page);
    params.set('size', size);

    if (sort) {
      params.set('sort', sort);
    }

    return httpClient.get(
      `/api/products/search?${params.toString()}`,
    );
  },
};

export default productApi;