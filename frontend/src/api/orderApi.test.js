import { beforeEach, describe, expect, it, vi } from 'vitest';

import httpClient from './httpClient';
import orderApi from './orderApi';

vi.mock('./httpClient', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    getBlob: vi.fn(),
  },
}));

describe('orderApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('creates an order from the current cart', async () => {
    const checkoutAddress = {
      fullName: 'Test Customer',
      phone: '9876543210',
      address: '10 Test Street',
      city: 'Bengaluru',
      state: 'Karnataka',
      pincode: '560001',
    };

    const response = {
      orderId: 101,
      status: 'CREATED',
    };

    httpClient.post.mockResolvedValue(response);

    const result = await orderApi.createOrderFromCart(checkoutAddress);

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/orders/checkout',
      {
        fullName: checkoutAddress.fullName,
        phone: checkoutAddress.phone,
        address: checkoutAddress.address,
        city: checkoutAddress.city,
        state: checkoutAddress.state,
        pincode: checkoutAddress.pincode,
      },
    );
    expect(result).toEqual(response);
  });

  it('gets the current user orders', async () => {
    const response = {
      content: [],
      totalElements: 0,
    };

    httpClient.get.mockResolvedValue(response);

    const result = await orderApi.getOrders();

    expect(httpClient.get).toHaveBeenCalledWith('/api/orders');
    expect(result).toEqual(response);
  });

  it('gets an order by id', async () => {
    const response = {
      id: 101,
      status: 'CREATED',
    };

    httpClient.get.mockResolvedValue(response);

    const result = await orderApi.getOrderById(101);

    expect(httpClient.get).toHaveBeenCalledWith('/api/orders/101');
    expect(result).toEqual(response);
  });

  it('encodes an order id when retrieving an order', async () => {
    httpClient.get.mockResolvedValue({
      id: 'order/test',
    });

    await orderApi.getOrderById('order/test');

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/orders/order%2Ftest',
    );
  });
});