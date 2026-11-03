
import httpClient from './httpClient';

const categoryApi = {
  getCategories() {
    return httpClient.get('/api/products/categories');
  },

  getCategoryById(categoryId) {
    return httpClient.get(
      `/api/products/categories/${encodeURIComponent(categoryId)}`,
    );
  },

  createCategory(category) {
    return httpClient.post('/api/products/categories', category);
  },

  updateCategory(categoryId, category) {
    return httpClient.put(
      `/api/products/categories/${encodeURIComponent(categoryId)}`,
      category,
    );
  },

  deleteCategory(categoryId) {
    return httpClient.delete(
      `/api/products/categories/${encodeURIComponent(categoryId)}`,
    );
  },
};

export default categoryApi;