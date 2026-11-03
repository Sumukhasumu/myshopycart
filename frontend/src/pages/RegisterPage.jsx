import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import authApi from '../api/authApi';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

import './RegisterPage.css';

const initialForm = {
  username: '',
  email: '',
  mobileNumber: '',
  password: '',
  confirmPassword: '',
};

function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }));

    setErrors((currentErrors) => ({
      ...currentErrors,
      [name]: '',
    }));

    setError('');
  };

  const validateForm = () => {
    const nextErrors = {};

    if (!form.username.trim()) {
      nextErrors.username = 'Username is required.';
    } else if (form.username.trim().length < 3) {
      nextErrors.username =
        'Username must contain at least 3 characters.';
    }

    if (!form.email.trim()) {
      nextErrors.email = 'Email is required.';
    } else if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())
    ) {
      nextErrors.email = 'Enter a valid email address.';
    }

    if (
      form.mobileNumber.trim() &&
      !/^[0-9+\s()-]{7,20}$/.test(form.mobileNumber.trim())
    ) {
      nextErrors.mobileNumber =
        'Enter a valid mobile number.';
    }

    if (!form.password) {
      nextErrors.password = 'Password is required.';
    } else if (form.password.length < 8) {
      nextErrors.password =
        'Password must contain at least 8 characters.';
    }

    if (!form.confirmPassword) {
      nextErrors.confirmPassword =
        'Please confirm your password.';
    } else if (form.password !== form.confirmPassword) {
      nextErrors.confirmPassword =
        'Passwords do not match.';
    }

    setErrors(nextErrors);

    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError('');

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      await authApi.register({
        username: form.username.trim(),
        email: form.email.trim(),
        mobileNumber: form.mobileNumber.trim() || null,
        password: form.password,
      });

      navigate('/login', {
        replace: true,
        state: {
          registrationSuccess:
            'Your account has been created successfully. Please sign in.',
          registeredUsername: form.username.trim(),
        },
      });
    } catch (requestError) {
      setError(
        requestError?.data?.message ||
          requestError?.data?.error ||
          requestError?.message ||
          'Registration failed. Please try again.',
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="register-page" aria-labelledby="register-title">
      <div className="register-card">
        <header className="register-header">
          <p className="register-brand">MYSHOPYCART</p>

          <h1 id="register-title">Create your account</h1>

          <p className="register-description">
            Register to start shopping and manage your orders.
          </p>
        </header>

        <form className="register-form" onSubmit={handleSubmit} noValidate>
          <Input
            id="register-username"
            name="username"
            type="text"
            label="Username"
            value={form.username}
            onChange={handleChange}
            autoComplete="username"
            required
            maxLength={50}
            error={errors.username}
          />

          <Input
            id="register-email"
            name="email"
            type="email"
            label="Email address"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            required
            maxLength={254}
            error={errors.email}
          />

          <Input
            id="register-mobile"
            name="mobileNumber"
            type="tel"
            label="Mobile number (optional)"
            value={form.mobileNumber}
            onChange={handleChange}
            autoComplete="tel"
            maxLength={20}
            error={errors.mobileNumber}
          />

          <Input
            id="register-password"
            name="password"
            type="password"
            label="Password"
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
            required
            maxLength={128}
            helperText="Use at least 8 characters."
            error={errors.password}
          />

          <Input
            id="register-confirm-password"
            name="confirmPassword"
            type="password"
            label="Confirm password"
            value={form.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
            required
            maxLength={128}
            error={errors.confirmPassword}
          />

          {error && (
            <p className="register-error" role="alert">
              {error}
            </p>
          )}

          <Button
            type="submit"
            disabled={isSubmitting}
            className="register-submit"
          >
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </Button>
        </form>

        <p className="register-login-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </section>
  );
}

export default RegisterPage;