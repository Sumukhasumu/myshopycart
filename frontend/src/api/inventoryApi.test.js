
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest';
import httpClient from './httpClient';
import inventoryApi from './inventoryApi';

vi.mock('./httpClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('inventoryApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets the current distributor inventory', async () => {
    const response = [
      {
        productId: 'product-test-1',
        availableStock: 6,
        reservedStock: 2,
      },
    ];

    httpClient.get.mockResolvedValue(response);

    await expect(
      inventoryApi.getMyInventory(),
    ).resolves.toEqual(response);

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/my-inventory',
    );
  });

  it('gets inventory by product id', async () => {
    httpClient.get.mockResolvedValue({
      productId: 'product-test-1',
      availableStock: 6,
      reservedStock: 2,
    });

    await inventoryApi.getInventoryByProductId(
      'product-test-1',
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/product-test-1',
    );
  });

  it('encodes special characters in product ids', async () => {
    httpClient.get.mockResolvedValue({});

    await inventoryApi.getInventoryByProductId(
      'product/one',
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/product%2Fone',
    );
  });

  it('creates inventory with a trimmed product id and numeric stock', async () => {
    const response = {
      productId: 'product-test-1',
      availableStock: 10,
      reservedStock: 0,
    };

    httpClient.post.mockResolvedValue(response);

    await expect(
      inventoryApi.createInventory({
        productId: '  product-test-1  ',
        availableStock: '10',
      }),
    ).resolves.toEqual(response);

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/inventory',
      {
        productId: 'product-test-1',
        availableStock: 10,
      },
    );
  });

  it('adds stock using a numeric quantity', async () => {
    const response = {
      productId: 'product-test-1',
      availableStock: 9,
      reservedStock: 2,
    };

    httpClient.post.mockResolvedValue(response);

    await expect(
      inventoryApi.addStock(
        'product-test-1',
        '3',
      ),
    ).resolves.toEqual(response);

    expect(httpClient.post).toHaveBeenCalledWith(
      '/api/inventory/product-test-1/stock',
      {
        quantity: 3,
      },
    );
  });

  it('gets availability with the default quantity of one', async () => {
    httpClient.get.mockResolvedValue({
      available: true,
    });

    await inventoryApi.getAvailability(
      'product-test-1',
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/product-test-1/availability?quantity=1',
    );
  });

  it('gets availability with the requested quantity', async () => {
    httpClient.get.mockResolvedValue({
      available: true,
    });

    await inventoryApi.getAvailability(
      'product-test-1',
      4,
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/product-test-1/availability?quantity=4',
    );
  });

  it('encodes special characters in availability product ids', async () => {
    httpClient.get.mockResolvedValue({
      available: true,
    });

    await inventoryApi.getAvailability(
      'product/one',
      2,
    );

    expect(httpClient.get).toHaveBeenCalledWith(
      '/api/inventory/product%2Fone/availability?quantity=2',
    );
  });
});
