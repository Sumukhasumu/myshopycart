import httpClient from './httpClient';

const orderApi = {
  createOrderFromCart(checkoutAddress) {
    return httpClient.post('/api/orders/checkout', {
      fullName: checkoutAddress.fullName,
      phone: checkoutAddress.phone,
      address: checkoutAddress.address,
      city: checkoutAddress.city,
      state: checkoutAddress.state,
      pincode: checkoutAddress.pincode,
    });
  },

 processPayment(orderId, paymentMethod, simulationMode) {
    return httpClient.post(
      `/api/orders/${encodeURIComponent(orderId)}/payment`,
      {
        paymentMethod,
        simulationMode,
      },
    );
  },

  getOrders() {
    return httpClient.get('/api/orders');
  },

  getOrderById(orderId) {
    return httpClient.get(
      `/api/orders/${encodeURIComponent(orderId)}`,
    );
  },

  getInvoiceDetails(orderId) {
    return httpClient.get(
      `/api/orders/${encodeURIComponent(orderId)}/invoice/details`
    );
  },

  downloadInvoice(orderId) {
    return httpClient.getBlob(
      `/api/orders/${encodeURIComponent(orderId)}/invoice`,
    );
  },
};

export default orderApi;