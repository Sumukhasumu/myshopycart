import httpClient from './httpClient';

const cartApi = {
  getCart() {
    return httpClient.get('/api/carts');
  },

  addItem(productId, quantity = 1) {
    return httpClient.post('/api/carts/items', {
      productId,
      quantity,
    });
  },

  updateItemQuantity(productId, quantity) {
    return httpClient.put(
      `/api/carts/items/${encodeURIComponent(productId)}`,
      {
        quantity,
      },
    );
  },

  removeItem(productId) {
    return httpClient.delete(
      `/api/carts/items/${encodeURIComponent(productId)}`,
    );
  },

  clearCart() {
    return httpClient.delete('/api/carts');
  },
};

export default cartApi;