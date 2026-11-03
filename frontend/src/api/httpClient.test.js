import { describe, expect, it, vi } from 'vitest';
import httpClient from './httpClient';

describe('httpClient', () => {
  it('throws an error for a failed API response', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      headers: {
        get: () => 'application/json',
      },
      json: async () => ({
        message: 'Unauthorized',
      }),
    });

    await expect(
      httpClient.get('/api/orders'),
    ).rejects.toMatchObject({
      status: 401,
      data: {
        message: 'Unauthorized',
      },
    });
  });
});