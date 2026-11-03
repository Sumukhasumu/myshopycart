import httpClient from './httpClient';

const wishlistApi = {
  getWishlist() {
    return httpClient.get('/api/wishlists');
  },

  addItem(productId) {
    return httpClient.post('/api/wishlists/items', {
      productId,
    });
  },

  removeItem(productId) {
    return httpClient.delete(
      `/api/wishlists/items/${encodeURIComponent(productId)}`,
    );
  },
};

export default wishlistApi;