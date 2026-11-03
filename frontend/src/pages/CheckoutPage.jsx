import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import cartApi from '../api/cartApi';
import orderApi from '../api/orderApi';
import productApi from '../api/productApi';

import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';
import Button from '../components/ui/Button';

import './CheckoutPage.css';

function formatPrice(price) {
  const numericPrice = Number(price);

  if (Number.isNaN(numericPrice)) {
    return 'Price unavailable';
  }

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(numericPrice);
}

function CheckoutPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [products, setProducts] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [isCreatingOrder, setIsCreatingOrder] = useState(false);
  const [error, setError] = useState('');
  const [orderError, setOrderError] = useState('');
  const [createdOrder, setCreatedOrder] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});
  const [isAddressValid, setIsAddressValid] = useState(false);

  const [address, setAddress] = useState({
    fullName: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    pincode: '',
  });

  useEffect(() => {
    let isActive = true;

    cartApi
      .getCart()
      .then(async (response) => {
        if (!isActive) {
          return;
        }

        setCart(response);
        setError('');
        setOrderError('');
        setCreatedOrder(null);

        const cartItems = response?.items || [];

        if (cartItems.length === 0) {
          setProducts({});
          return;
        }

        const productResults = await Promise.all(
          cartItems.map(async (item) => {
            const product = await productApi.getProductById(
              item.productId,
            );

            return {
              productId: item.productId,
              product,
            };
          }),
        );

        if (!isActive) {
          return;
        }

        const productMap = productResults.reduce(
          (result, item) => {
            result[item.productId] = item.product;

            return result;
          },
          {},
        );

        setProducts(productMap);
      })
      .catch((requestError) => {
        if (!isActive) {
          return;
        }

        setCart(null);
        setProducts({});

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load your cart for checkout.',
        );
      })
      .finally(() => {
        if (!isActive) {
          return;
        }

        setIsLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, []);

  const cartItems = cart?.items || [];

  const total = cartItems.reduce(
    (sum, item) => {
      const product = products[item.productId];
      const price = Number(product?.price || 0);

      return sum + price * item.quantity;
    },
    0,
  );

  const handleAddressChange = (event) => {
    const { name, value } = event.target;

    setAddress((currentAddress) => ({
      ...currentAddress,
      [name]: value,
    }));

    setValidationErrors((currentErrors) => ({
      ...currentErrors,
      [name]: '',
    }));

    setIsAddressValid(false);
    setOrderError('');
    setCreatedOrder(null);
  };

  const validateAddress = () => {
    const errors = {};

    const normalizedName = address.fullName.trim();
    const normalizedPhone = address.phone.trim();
    const normalizedAddress = address.address.trim();
    const normalizedCity = address.city.trim();
    const normalizedState = address.state.trim();
    const normalizedPincode = address.pincode.trim();

    if (!normalizedName) {
      errors.fullName = 'Full name is required.';
    }

    if (!normalizedPhone) {
      errors.phone = 'Phone number is required.';
    } else if (!/^\d{10}$/.test(normalizedPhone)) {
      errors.phone =
        'Phone number must contain exactly 10 digits.';
    }

    if (!normalizedAddress) {
      errors.address = 'Delivery address is required.';
    }

    if (!normalizedCity) {
      errors.city = 'City is required.';
    }

    if (!normalizedState) {
      errors.state = 'State is required.';
    }

    if (!normalizedPincode) {
      errors.pincode = 'PIN code is required.';
    } else if (!/^\d{6}$/.test(normalizedPincode)) {
      errors.pincode =
        'PIN code must contain exactly 6 digits.';
    }

    setValidationErrors(errors);

    const valid = Object.keys(errors).length === 0;

    setIsAddressValid(valid);

    return valid;
  };

  const handleContinue = async (event) => {
    event.preventDefault();

    setOrderError('');
    setCreatedOrder(null);

    const valid = validateAddress();

    if (!valid) {
      return;
    }

    if (isCreatingOrder) {
      return;
    }

    if (cartItems.length === 0) {
      setOrderError(
        'Your cart is empty. Add products before continuing.',
      );
      return;
    }

    setIsCreatingOrder(true);

    try {
      const order = await orderApi.createOrderFromCart(address);

      if (!order?.id) {
        throw new Error(
          'The order was created, but its ID was not returned. Check your Orders page before retrying.',
        );
      }

      navigate(`/payment/${encodeURIComponent(order.id)}`);
    } catch (requestError) {
      setOrderError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to create your order. Please try again.',
      );
    } finally {
      setIsCreatingOrder(false);
    }
  };

  return (
    <main className="checkout-page">
      <header className="checkout-page-header">
        <p className="checkout-page-eyebrow">
          Secure Checkout
        </p>

        <h1>Checkout</h1>

        <p className="checkout-page-description">
          Review your order and provide your delivery
          address before placing the order.
        </p>
      </header>

      {error && (
        <Alert
          className="checkout-error"
          variant="error"
        >
          {error}
        </Alert>
      )}

      {orderError && (
        <Alert
          className="checkout-error"
          variant="error"
        >
          {orderError}
        </Alert>
      )}

      {createdOrder && (
        <Alert
          className="checkout-success"
          variant="success"
        >
          Order created successfully.
          {createdOrder.id
            ? ` Order #${createdOrder.id}.`
            : ''}
        </Alert>
      )}

      {isLoading ? (
        <section className="checkout-loading">
          <Spinner />
          <p>Loading checkout...</p>
        </section>
      ) : error ? (
        <section className="checkout-error-state">
          <EmptyState
            title="Checkout unavailable"
            description="We could not load your cart for checkout. Please try again."
            actionLabel="Try Again"
            onAction={() => window.location.reload()}
          />
        </section>
      ) : cartItems.length === 0 ? (
        <section className="checkout-empty-state">
          <EmptyState
            title="Your cart is empty"
            description="Add products to your cart before continuing to checkout."
          />
        </section>
      ) : (
        <form
          className="checkout-layout"
          onSubmit={handleContinue}
          noValidate
        >
          <div className="checkout-main">
            <section className="checkout-card">
              <div className="checkout-card-header">
                <div>
                  <p className="checkout-section-eyebrow">
                    Step 1
                  </p>

                  <h2>Delivery Address</h2>
                </div>
              </div>

              <div className="checkout-address-grid">
                <div className="checkout-field">
                  <label htmlFor="checkout-full-name">
                    Full Name
                  </label>

                  <input
                    id="checkout-full-name"
                    name="fullName"
                    type="text"
                    autoComplete="name"
                    placeholder="Enter your full name"
                    value={address.fullName}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.fullName,
                    )}
                    aria-describedby={
                      validationErrors.fullName
                        ? 'checkout-full-name-error'
                        : undefined
                    }
                  />

                  {validationErrors.fullName && (
                    <span
                      id="checkout-full-name-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.fullName}
                    </span>
                  )}
                </div>

                <div className="checkout-field">
                  <label htmlFor="checkout-phone">
                    Phone Number
                  </label>

                  <input
                    id="checkout-phone"
                    name="phone"
                    type="tel"
                    inputMode="numeric"
                    autoComplete="tel"
                    placeholder="Enter your phone number"
                    value={address.phone}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.phone,
                    )}
                    aria-describedby={
                      validationErrors.phone
                        ? 'checkout-phone-error'
                        : undefined
                    }
                  />

                  {validationErrors.phone && (
                    <span
                      id="checkout-phone-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.phone}
                    </span>
                  )}
                </div>

                <div className="checkout-field checkout-field-full">
                  <label htmlFor="checkout-address">
                    Address
                  </label>

                  <textarea
                    id="checkout-address"
                    name="address"
                    rows="3"
                    autoComplete="street-address"
                    placeholder="Enter your delivery address"
                    value={address.address}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.address,
                    )}
                    aria-describedby={
                      validationErrors.address
                        ? 'checkout-address-error'
                        : undefined
                    }
                  />

                  {validationErrors.address && (
                    <span
                      id="checkout-address-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.address}
                    </span>
                  )}
                </div>

                <div className="checkout-field">
                  <label htmlFor="checkout-city">
                    City
                  </label>

                  <input
                    id="checkout-city"
                    name="city"
                    type="text"
                    autoComplete="address-level2"
                    placeholder="Enter city"
                    value={address.city}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.city,
                    )}
                    aria-describedby={
                      validationErrors.city
                        ? 'checkout-city-error'
                        : undefined
                    }
                  />

                  {validationErrors.city && (
                    <span
                      id="checkout-city-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.city}
                    </span>
                  )}
                </div>

                <div className="checkout-field">
                  <label htmlFor="checkout-state">
                    State
                  </label>

                  <input
                    id="checkout-state"
                    name="state"
                    type="text"
                    autoComplete="address-level1"
                    placeholder="Enter state"
                    value={address.state}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.state,
                    )}
                    aria-describedby={
                      validationErrors.state
                        ? 'checkout-state-error'
                        : undefined
                    }
                  />

                  {validationErrors.state && (
                    <span
                      id="checkout-state-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.state}
                    </span>
                  )}
                </div>

                <div className="checkout-field">
                  <label htmlFor="checkout-pincode">
                    PIN Code
                  </label>

                  <input
                    id="checkout-pincode"
                    name="pincode"
                    type="text"
                    inputMode="numeric"
                    autoComplete="postal-code"
                    placeholder="Enter PIN code"
                    value={address.pincode}
                    onChange={handleAddressChange}
                    aria-invalid={Boolean(
                      validationErrors.pincode,
                    )}
                    aria-describedby={
                      validationErrors.pincode
                        ? 'checkout-pincode-error'
                        : undefined
                    }
                  />

                  {validationErrors.pincode && (
                    <span
                      id="checkout-pincode-error"
                      className="checkout-field-error"
                    >
                      {validationErrors.pincode}
                    </span>
                  )}
                </div>
              </div>
            </section>

            <section className="checkout-card">
              <div className="checkout-card-header">
                <div>
                  <p className="checkout-section-eyebrow">
                    Step 2
                  </p>

                  <h2>Order Items</h2>
                </div>

                <span className="checkout-item-count">
                  {cartItems.length}{' '}
                  {cartItems.length === 1
                    ? 'item'
                    : 'items'}
                </span>
              </div>

              <div className="checkout-items">
                {cartItems.map((item) => {
                  const product =
                    products[item.productId];

                  const price = Number(
                    product?.price || 0,
                  );

                  const subtotal =
                    price * item.quantity;

                  return (
                    <article
                      className="checkout-item"
                      key={item.productId}
                    >
                      <div className="checkout-item-image">
                        {product?.imageUrls?.[0] ? (
                          <img
                            src={product.imageUrls[0]}
                            alt={product.name}
                          />
                        ) : (
                          <span>No Image</span>
                        )}
                      </div>

                      <div className="checkout-item-details">
                        <h3>
                          {product?.name ||
                            item.productId}
                        </h3>

                        <p>
                          Quantity: {item.quantity}
                        </p>
                      </div>

                      <strong className="checkout-item-subtotal">
                        {formatPrice(subtotal)}
                      </strong>
                    </article>
                  );
                })}
              </div>
            </section>
          </div>

          <aside className="checkout-summary">
            <p className="checkout-section-eyebrow">
              Step 3
            </p>

            <h2>Order Summary</h2>

            <div className="checkout-summary-row">
              <span>Items</span>
              <strong>{cartItems.length}</strong>
            </div>

            <div className="checkout-summary-row">
              <span>Subtotal</span>
              <strong>{formatPrice(total)}</strong>
            </div>

            <div className="checkout-summary-divider" />

            <div className="checkout-summary-total">
              <span>Total</span>
              <strong>{formatPrice(total)}</strong>
            </div>

            <Button
              type="submit"
              variant="primary"
              disabled={isCreatingOrder}
            >
              {isCreatingOrder
                ? 'Creating Order...'
                : 'Continue to Order'}
            </Button>

            {isAddressValid && !createdOrder && (
              <p className="checkout-validation-success">
                Delivery address is valid.
              </p>
            )}

            {createdOrder && (
              <p className="checkout-validation-success">
                Order created successfully.
              </p>
            )}

            <p className="checkout-summary-note">
              Order creation is now connected to the
              Order Service. Payment processing will be
              connected in the next checkout phase.
            </p>
          </aside>
        </form>
      )}
    </main>
  );
}

export default CheckoutPage;