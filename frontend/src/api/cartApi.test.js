import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';

import cartApi from './cartApi';
import httpClient from './httpClient';

vi.mock('./httpClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('cartApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets the current cart', async () => {
    const response = {
      cartId: 1,
      userId: 25,
      items: [],
    };

    httpClient.get.mockResolvedValue(response);

    await expect(cartApi.getCart()).resolves.toEqual(response);

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/carts',
    );
  });

  it('adds a product to the cart', async () => {
    const response = {
      cartItemId: 1,
      cartId: 1,
      productId: 'product-test-1',
      quantity: 2,
    };

    httpClient.post.mockResolvedValue(response);

    await expect(
      cartApi.addItem('product-test-1', 2),
    ).resolves.toEqual(response);

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/carts/items',
      {
        productId: 'product-test-1',
        quantity: 2,
      },
    );
  });

  it('uses quantity 1 by default when adding an item', async () => {
    httpClient.post.mockResolvedValue({});

    await cartApi.addItem('product-test-1');

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/carts/items',
      {
        productId: 'product-test-1',
        quantity: 1,
      },
    );
  });

  it('updates product quantity', async () => {
    const response = {
      cartItemId: 1,
      cartId: 1,
      productId: 'product-test-1',
      quantity: 3,
    };

    httpClient.put.mockResolvedValue(response);

    await expect(
      cartApi.updateItemQuantity(
        'product-test-1',
        3,
      ),
    ).resolves.toEqual(response);

    expect(httpClient.put).toHaveBeenCalledWith(
      '/api/carts/items/product-test-1',
      {
        quantity: 3,
      },
    );
  });

  it('encodes product ID when updating quantity', async () => {
    httpClient.put.mockResolvedValue({});

    await cartApi.updateItemQuantity(
      'product/with-special-value',
      2,
    );

    expect(httpClient.put).toHaveBeenCalledWith(
      '/api/carts/items/product%2Fwith-special-value',
      {
        quantity: 2,
      },
    );
  });

  it('removes a product from the cart', async () => {
    httpClient.delete.mockResolvedValue(null);

    await expect(
      cartApi.removeItem('product-test-1'),
    ).resolves.toBeNull();

    expect(httpClient.delete).toHaveBeenCalledWith(
      '/api/carts/items/product-test-1',
    );
  });

  it('encodes product ID when removing an item', async () => {
    httpClient.delete.mockResolvedValue(null);

    await cartApi.removeItem(
      'product/with-special-value',
    );

    expect(httpClient.delete).toHaveBeenCalledWith(
      '/api/carts/items/product%2Fwith-special-value',
    );
  });

  it('clears the cart', async () => {
    httpClient.delete.mockResolvedValue(null);

    await expect(
      cartApi.clearCart(),
    ).resolves.toBeNull();

    expect(httpClient.delete).toHaveBeenCalledWith(
      '/api/carts',
    );
  });
});