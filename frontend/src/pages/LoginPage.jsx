import { useState } from 'react';
import {
  Link,
  useLocation,
  useNavigate,
} from 'react-router-dom';

import authApi from '../api/authApi';
import useAuth from '../auth/useAuth';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import './LoginPage.css';

function getLoginErrorMessage(requestError) {
  const status =
    requestError?.status ??
    requestError?.statusCode ??
    requestError?.response?.status;

  const responseData =
    requestError?.data ??
    requestError?.response?.data;

  const serverMessage =
    responseData?.message ??
    responseData?.error ??
    (typeof responseData === 'string'
      ? responseData
      : '');

  if (status === 401 || status === 403) {
    return 'Invalid username or password. Please check your credentials and try again.';
  }

  if (status === 400) {
    return (
      serverMessage ||
      'Please check your username and password.'
    );
  }

  if (status === 404) {
    return 'The login service could not be found. Please try again later.';
  }

  if (typeof status === 'number' && status >= 500) {
    return 'The server encountered a problem. Please try again later.';
  }

  if (
    requestError?.message === 'Failed to fetch' ||
    requestError?.name === 'TypeError'
  ) {
    return 'Unable to connect to the server. Please check your connection and try again.';
  }

  if (serverMessage) {
    return serverMessage;
  }

  return 'Unable to sign in. Please try again.';
}

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const registrationSuccess =
    location.state?.registrationSuccess;

  const [form, setForm] = useState({
    username:
      location.state?.registeredUsername || '',
    password: '',
  });

  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] =
    useState(false);
  const [showPassword, setShowPassword] =
    useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }));

    if (error) {
      setError('');
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    const username = form.username.trim();
    const password = form.password;

    if (!username || !password) {
      setError(
        'Please enter your username and password.',
      );
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await authApi.login({
        username,
        password,
      });

      const accessToken =
        response?.accessToken || response?.token;

      if (!accessToken) {
        setError(
          'The login service returned an invalid response. Please try again later.',
        );
        return;
      }

      const authenticatedUser = {
        id: response?.id ?? null,
        username: response?.username ?? null,
        email: response?.email ?? null,
        role: response?.role ?? null,
      };

      login(accessToken, authenticatedUser);

      const role = String(
        authenticatedUser.role ?? '',
      ).toUpperCase();

      const requestedPath =
        location.state?.from?.pathname;

      const destination =
        role === 'ADMIN'
          ? '/admin/dashboard'
          : role === 'DISTRIBUTOR'
            ? '/distributor/dashboard'
            : requestedPath || '/profile';

      navigate(destination, { replace: true });
    } catch (requestError) {
      setError(getLoginErrorMessage(requestError));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section
      aria-labelledby="login-title"
      className="auth-page"
    >
      <div className="auth-card">
        <h1 id="login-title">Sign in</h1>

        {registrationSuccess && (
          <div
            className="auth-success"
            role="status"
          >
            Registration successful. You can now
            sign in.
          </div>
        )}

        {error && (
          <div
            className="auth-error"
            role="alert"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="auth-form-group">
            <label htmlFor="username">
              Username
            </label>

            <Input
              id="username"
              name="username"
              type="text"
              value={form.username}
              onChange={handleChange}
              autoComplete="username"
              required
            />
          </div>

          <div className="auth-form-group">
            <label htmlFor="password">
              Password
            </label>

            <div className="login-password-field">
              <Input
                id="password"
                name="password"
                type={
                  showPassword ? 'text' : 'password'
                }
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
                required
              />

              <button
                type="button"
                className="login-password-toggle"
                onClick={() =>
                  setShowPassword((visible) => !visible)
                }
                aria-label={
                  showPassword
                    ? 'Hide password'
                    : 'Show password'
                }
                aria-pressed={showPassword}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
          </div>

          <Button
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? 'Signing in...'
              : 'Sign in'}
          </Button>
        </form>

        <p>
          Don't have an account?{' '}
          <Link to="/register">Create one</Link>
        </p>

        <p className="architecture-explorer-link">
          <a
            href="https://myshopycart-architecture.sumu0588s.workers.dev/#overview"
            target="_blank"
            rel="noopener noreferrer"
          >
            Explore MYSHOPYCART Architecture ↗
          </a>
        </p>
      </div>
    </section>
  );
}

export default LoginPage;