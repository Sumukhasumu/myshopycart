import httpClient from './httpClient';

const inventoryApi = {
  getMyInventory() {
    return httpClient.get(
      '/api/inventory/my-inventory',
    );
  },

  getInventoryByProductId(productId) {
    return httpClient.get(
      `/api/inventory/${encodeURIComponent(productId)}`,
    );
  },

  createInventory({
    productId,
    availableStock,
  }) {
    return httpClient.post(
      '/api/inventory',
      {
        productId: String(productId).trim(),
        availableStock: Number(availableStock),
      },
    );
  },

  addStock(productId, quantity) {
    return httpClient.post(
      `/api/inventory/${encodeURIComponent(productId)}/stock`,
      {
        quantity: Number(quantity),
      },
    );
  },

  adjustStock(productId, { quantity, operation, reason }) {
    return httpClient.post(
      `/api/inventory/${encodeURIComponent(productId)}/adjust`,
      {
        quantity: Number(quantity),
        operation,
        reason: String(reason).trim(),
      },
    );
  },

  getTransactionHistory(productId) {
    return httpClient.get(
      `/api/inventory/${encodeURIComponent(productId)}/history`,
    );
  },

  getAvailability(productId, quantity = 1) {
    const params = new URLSearchParams();
    params.set('quantity', String(quantity));

    return httpClient.get(
      `/api/inventory/${encodeURIComponent(productId)}/availability?${params.toString()}`,
    );
  },
};

export default inventoryApi;