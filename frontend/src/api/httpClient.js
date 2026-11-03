import { getSession } from '../auth/sessionStorage';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  'http://localhost:8080';

async function request(
  path,
  options = {},
) {
  const {
    method = 'GET',
    headers = {},
    body,
    responseType = 'json',
  } = options;

  const session = getSession();

  const requestHeaders = {
    Accept:
      responseType === 'blob'
        ? 'application/pdf, application/json'
        : 'application/json',
    ...headers,
  };

  if (body !== undefined) {
    requestHeaders['Content-Type'] =
      'application/json';
  }

  if (session?.accessToken) {
    requestHeaders.Authorization =
      `Bearer ${session.accessToken}`;
  }

  const response = await fetch(
    `${API_BASE_URL}${path}`,
    {
      method,
      headers: requestHeaders,
      body:
        body !== undefined
          ? JSON.stringify(body)
          : undefined,
    },
  );

  if (response.status === 204) {
    return null;
  }

  const contentType =
    response.headers.get('content-type') || '';

  let responseData;

  if (
    responseType === 'blob' &&
    response.ok &&
    contentType.includes('application/pdf')
  ) {
    responseData = await response.blob();
  } else if (
    contentType.includes('application/json')
  ) {
    responseData = await response.json();
  } else if (responseType === 'blob') {
    responseData = await response.blob();
  } else {
    responseData = await response.text();
  }

  if (!response.ok) {
    const errorMessage =
      typeof responseData === 'object' &&
      responseData !== null
        ? responseData.message ||
          responseData.error ||
          `API request failed with status ${response.status}`
        : `API request failed with status ${response.status}`;

    const error = new Error(errorMessage);

    error.status = response.status;
    error.data = responseData;

    throw error;
  }

  return responseData;
}

const httpClient = {
  get(path, options = {}) {
    return request(path, {
      ...options,
      method: 'GET',
    });
  },

  getBlob(path, options = {}) {
    return request(path, {
      ...options,
      method: 'GET',
      responseType: 'blob',
    });
  },

  post(path, body, options = {}) {
    return request(path, {
      ...options,
      method: 'POST',
      body,
    });
  },

  put(path, body, options = {}) {
    return request(path, {
      ...options,
      method: 'PUT',
      body,
    });
  },

  patch(path, body, options = {}) {
    return request(path, {
      ...options,
      method: 'PATCH',
      body,
    });
  },

  delete(path, options = {}) {
    return request(path, {
      ...options,
      method: 'DELETE',
    });
  },
};

export default httpClient;