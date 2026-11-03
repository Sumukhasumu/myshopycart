import httpClient from './httpClient';

const authApi = {
  login(credentials) {
    return httpClient.post(
      '/api/auth/login',
      credentials,
    );
  },

  register(userData) {
    return httpClient.post(
      '/api/auth/register',
      userData,
    );
  },
};

export default authApi;