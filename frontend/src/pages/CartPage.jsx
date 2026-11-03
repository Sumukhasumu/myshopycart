import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import CartContext from '../auth/CartContext';
import cartApi from '../api/cartApi';
import productApi from '../api/productApi';

import Alert from '../components/ui/Alert';
import Button from '../components/ui/Button';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';

import './CartPage.css';

function CartPage() {
  const navigate = useNavigate();
  const { refreshCartCount } = useContext(CartContext) || {};
  const [cart, setCart] = useState(null);
  const [products, setProducts] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [removingProductId, setRemovingProductId] = useState('');
  const [updatingProductId, setUpdatingProductId] = useState('');

  const loadCart = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const cartResponse = await cartApi.getCart();

      setCart(cartResponse);

      if (!cartResponse?.items?.length) {
        setProducts({});
        return;
      }

      const productResults = await Promise.all(
        cartResponse.items.map(async (item) => {
          try {
            const product = await productApi.getProductById(
              item.productId,
            );

            return {
              productId: item.productId,
              product,
            };
          } catch {
            return {
              productId: item.productId,
              product: null,
            };
          }
        }),
      );

      const productMap = {};

      productResults.forEach(({ productId, product }) => {
        productMap[productId] = product;
      });

      setProducts(productMap);
    } catch (requestError) {
      setError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to load your cart.',
      );
      setCart(null);
      setProducts({});
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    const initializeCart = async () => {
      setLoading(true);
      setError('');

      try {
        const cartResponse = await cartApi.getCart();

        if (cancelled) {
          return;
        }

        setCart(cartResponse);

        if (!cartResponse?.items?.length) {
          setProducts({});
          return;
        }

        const productResults = await Promise.all(
          cartResponse.items.map(async (item) => {
            try {
              const product = await productApi.getProductById(
                item.productId,
              );

              return {
                productId: item.productId,
                product,
              };
            } catch {
              return {
                productId: item.productId,
                product: null,
              };
            }
          }),
        );

        if (cancelled) {
          return;
        }

        const productMap = {};

        productResults.forEach(({ productId, product }) => {
          productMap[productId] = product;
        });

        setProducts(productMap);
      } catch (requestError) {
        if (cancelled) {
          return;
        }

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load your cart.',
        );
        setCart(null);
        setProducts({});
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    initializeCart();

    return () => {
      cancelled = true;
    };
  }, []);

  const cartItems = useMemo(
    () => cart?.items || [],
    [cart],
  );

  const totalQuantity = useMemo(
    () =>
      cartItems.reduce(
        (sum, item) => sum + (Number(item.quantity) || 0),
        0,
      ),
    [cartItems],
  );

  const total = useMemo(
    () =>
      cartItems.reduce((sum, item) => {
        const product = products[item.productId];
        const price = Number(product?.price || 0);

        return sum + price * item.quantity;
      }, 0),
    [cartItems, products],
  );

  const formatPrice = (price) =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(price);

  const handleQuantityChange = async (
    productId,
    currentQuantity,
    change,
  ) => {
    const nextQuantity = currentQuantity + change;

    if (nextQuantity < 1) {
      return;
    }

    setUpdatingProductId(productId);
    setError('');

    try {
      await cartApi.updateItemQuantity(
        productId,
        nextQuantity,
      );

      await loadCart();
      await refreshCartCount?.();
    } catch (requestError) {
      setError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to update the item quantity.',
      );
    } finally {
      setUpdatingProductId('');
    }
  };

  const handleRemove = async (productId) => {
    setRemovingProductId(productId);
    setError('');

    try {
      await cartApi.removeItem(productId);
      await loadCart();
      await refreshCartCount?.();
    } catch (requestError) {
      setError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to remove the item from your cart.',
      );
    } finally {
      setRemovingProductId('');
    }
  };

  if (loading) {
    return (
      <section className="cart-page">
        <div className="page-container">
          <div className="cart-page-header">
            <p className="cart-page-eyebrow">MYSHOPYCART</p>
            <h1>Your Cart</h1>
          </div>

          <div className="cart-loading">
            <Spinner />
            <p>Loading your cart...</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="cart-page">
      <div className="page-container">
        <header className="cart-page-header">
          <p className="cart-page-eyebrow">MYSHOPYCART</p>

          <h1>Your Cart</h1>

          <p className="cart-page-description">
            Review the products you have added to your shopping cart.
          </p>
        </header>

        {error && (
          <div className="cart-error">
            <Alert
              variant="error"
              title="Cart unavailable"
              message={error}
            />
          </div>
        )}

        {!error && cartItems.length === 0 && (
          <EmptyState
            title="Your cart is empty"
            description="Browse our products and add something you would like to purchase."
          />
        )}

        {!error && cartItems.length > 0 && (
          <div className="cart-layout">
            <div className="cart-items-card">
              <div className="cart-items-header">
                <h2>Cart Items</h2>

                <span>
                  {totalQuantity}{' '}
                  {totalQuantity === 1 ? 'item' : 'items'}
                </span>
              </div>

              <div className="cart-items-list">
                {cartItems.map((item) => {
                  const product = products[item.productId];

                  const price = Number(product?.price || 0);
                  const subtotal = price * item.quantity;

                  const imageUrl = product?.imageUrls?.[0];

                  const isUpdating =
                    updatingProductId === item.productId;

                  const isRemoving =
                    removingProductId === item.productId;

                  const controlsDisabled =
                    isUpdating || isRemoving;

                  return (
                    <article
                      className="cart-item"
                      key={item.cartItemId}
                    >
                      <div className="cart-item-image">
                        {imageUrl ? (
                          <img
                            src={imageUrl}
                            alt={product?.name || 'Product'}
                          />
                        ) : (
                          <span>No Image</span>
                        )}
                      </div>

                      <div className="cart-item-details">
                        <h3>
                          {product?.name || item.productId}
                        </h3>

                        {product?.description && (
                          <p className="cart-item-description">
                            {product.description}
                          </p>
                        )}

                        <p className="cart-item-product-id">
                          Product ID: {item.productId}
                        </p>
                      </div>

                      <div className="cart-item-price">
                        <span className="cart-item-label">
                          Unit Price
                        </span>

                        <strong>
                          {formatPrice(price)}
                        </strong>
                      </div>

                      <div className="cart-item-quantity">
                        <span className="cart-item-label">
                          Quantity
                        </span>

                        <div className="quantity-control">
                          <button
                            type="button"
                            className="quantity-button"
                            aria-label={`Decrease quantity of ${
                              product?.name || item.productId
                            }`}
                            disabled={
                              controlsDisabled ||
                              item.quantity <= 1
                            }
                            onClick={() =>
                              handleQuantityChange(
                                item.productId,
                                item.quantity,
                                -1,
                              )
                            }
                          >
                            −
                          </button>

                          <span
                            className="quantity-value"
                            aria-live="polite"
                          >
                            {isUpdating
                              ? '...'
                              : item.quantity}
                          </span>

                          <button
                            type="button"
                            className="quantity-button"
                            aria-label={`Increase quantity of ${
                              product?.name || item.productId
                            }`}
                            disabled={controlsDisabled}
                            onClick={() =>
                              handleQuantityChange(
                                item.productId,
                                item.quantity,
                                1,
                              )
                            }
                          >
                            +
                          </button>
                        </div>
                      </div>

                      <div className="cart-item-subtotal">
                        <span className="cart-item-label">
                          Subtotal
                        </span>

                        <strong>
                          {formatPrice(subtotal)}
                        </strong>
                      </div>

                      <div className="cart-item-actions">
                        <Button
                          variant="secondary"
                          type="button"
                          disabled={controlsDisabled}
                          onClick={() =>
                            handleRemove(item.productId)
                          }
                        >
                          {isRemoving
                            ? 'Removing...'
                            : 'Remove'}
                        </Button>
                      </div>
                    </article>
                  );
                })}
              </div>
            </div>

            <aside className="cart-summary">
              <h2>Order Summary</h2>

              <div className="cart-summary-row">
                <span>Items</span>

                <strong>{totalQuantity}</strong>
              </div>

              <div className="cart-summary-divider" />

              <div className="cart-summary-total">
                <span>Total</span>

                <strong>{formatPrice(total)}</strong>
              </div>

              <Button
                type="button"
                variant="primary"
                disabled={loading || cartItems.length === 0 || Boolean(error)}
                onClick={() => navigate('/checkout')}
              >
                Proceed to Checkout
              </Button>

              <p className="cart-checkout-note">
                Continue to checkout to review your order and delivery details.
              </p>
            </aside>
          </div>
        )}
      </div>
    </section>
  );
}

export default CartPage;