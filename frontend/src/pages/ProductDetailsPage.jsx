import { useContext, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import PageContainer from '../components/layout/PageContainer';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';

import productApi from '../api/productApi';
import cartApi from '../api/cartApi';
import wishlistApi from '../api/wishlistApi';
import CartContext from '../auth/CartContext';

import './ProductDetailsPage.css';

function formatPrice(price) {
  if (
    price === null ||
    price === undefined ||
    price === ''
  ) {
    return 'Price unavailable';
  }

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

function ProductDetailsPage() {
  const { productId } = useParams();
  const cartContext = useContext(CartContext);
  const refreshCartCount = cartContext?.refreshCartCount;

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [isAddingToCart, setIsAddingToCart] =
    useState(false);
  const [cartMessage, setCartMessage] =
    useState('');
  const [cartError, setCartError] =
    useState('');

  const [isInWishlist, setIsInWishlist] =
    useState(false);
  const [isUpdatingWishlist, setIsUpdatingWishlist] =
    useState(false);
  const [wishlistMessage, setWishlistMessage] =
    useState('');
  const [wishlistError, setWishlistError] =
    useState('');

  useEffect(() => {
    if (!cartMessage) {
      return undefined;
    }

    const timeoutId = setTimeout(() => {
      setCartMessage('');
    }, 3000);

    return () => {
      clearTimeout(timeoutId);
    };
  }, [cartMessage]);

  useEffect(() => {
    if (!wishlistMessage) {
      return undefined;
    }

    const timeoutId = setTimeout(() => {
      setWishlistMessage('');
    }, 3000);

    return () => {
      clearTimeout(timeoutId);
    };
  }, [wishlistMessage]);

  useEffect(() => {
    let isMounted = true;

    async function loadProduct() {
      setLoading(true);
      setError(null);
      setCartMessage('');
      setCartError('');
      setWishlistMessage('');
      setWishlistError('');

      try {
        const response =
          await productApi.getProductById(
            productId,
          );

        if (!isMounted) {
          return;
        }

        setProduct(response);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load this product.',
        );
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    if (productId) {
      loadProduct();
    }

    return () => {
      isMounted = false;
    };
  }, [productId]);

  useEffect(() => {
    let isMounted = true;

    async function loadWishlist() {
      try {
        const response =
          await wishlistApi.getWishlist();

        if (!isMounted) {
          return;
        }

        const isProductInWishlist =
          (response?.items || []).some(
            (item) =>
              item.productId === productId,
          );

        setIsInWishlist(isProductInWishlist);
      } catch {
        if (isMounted) {
          setIsInWishlist(false);
        }
      }
    }

    if (productId) {
      loadWishlist();
    }

    return () => {
      isMounted = false;
    };
  }, [productId]);

  const handleAddToCart = async () => {
    if (!product?.id) {
      return;
    }

    setIsAddingToCart(true);
    setCartMessage('');
    setCartError('');

    try {
      await cartApi.addItem(product.id, 1);
      await refreshCartCount?.();

      setCartMessage('Added to cart.');
    } catch (requestError) {
      setCartError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to add this product to your cart.',
      );
    } finally {
      setIsAddingToCart(false);
    }
  };

  const handleWishlistToggle = async () => {
    if (!product?.id) {
      return;
    }

    setIsUpdatingWishlist(true);
    setWishlistMessage('');
    setWishlistError('');

    try {
      if (isInWishlist) {
        await wishlistApi.removeItem(product.id);

        setIsInWishlist(false);
        setWishlistMessage(
          'Removed from wishlist.',
        );
      } else {
        await wishlistApi.addItem(product.id);

        setIsInWishlist(true);
        setWishlistMessage(
          'Added to wishlist.',
        );
      }
    } catch (requestError) {
      setWishlistError(
        requestError?.data?.message ||
          requestError?.message ||
          'Unable to update your wishlist.',
      );
    } finally {
      setIsUpdatingWishlist(false);
    }
  };

  if (loading) {
    return (
      <PageContainer>
        <section className="product-details-page">
          <div className="product-details-page__status">
            <Spinner />
            <p>Loading product...</p>
          </div>
        </section>
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer>
        <section className="product-details-page">
          <div className="product-details-page__status">
            <Alert variant="error">
              {error}
            </Alert>

            <Button
              as={Link}
              to="/products"
              variant="secondary"
            >
              Back to Products
            </Button>
          </div>
        </section>
      </PageContainer>
    );
  }

  if (!product) {
    return (
      <PageContainer>
        <section className="product-details-page">
          <div className="product-details-page__status">
            <Alert variant="error">
              Product not found.
            </Alert>

            <Button
              as={Link}
              to="/products"
              variant="secondary"
            >
              Back to Products
            </Button>
          </div>
        </section>
      </PageContainer>
    );
  }

  const {
    name,
    description,
    price,
    categoryId,
    attributes,
    imageUrls,
  } = product;

  const hasImages =
    Array.isArray(imageUrls) &&
    imageUrls.length > 0;

  return (
    <PageContainer>
      <section className="product-details-page">
        <Link
          to="/products"
          className="product-details-page__back"
        >
          ← Back to Products
        </Link>

        <div className="product-details-page__layout">
          <Card className="product-details-page__image-card">
            <div className="product-details-page__image-wrapper">
              {hasImages ? (
                <img
                  src={imageUrls[0]}
                  alt={name}
                  className="product-details-page__image"
                />
              ) : (
                <div
                  className="product-details-page__image-placeholder"
                  aria-label="No product image available"
                >
                  <span>No Image</span>
                </div>
              )}
            </div>
          </Card>

          <Card className="product-details-page__content-card">
            <div className="product-details-page__content">
              <p className="product-details-page__eyebrow">
                PRODUCT DETAILS
              </p>

              <h1 className="product-details-page__title">
                {name}
              </h1>

              {description && (
                <p className="product-details-page__description">
                  {description}
                </p>
              )}

              <p className="product-details-page__price">
                {formatPrice(price)}
              </p>

              <div className="product-details-page__meta">
                {categoryId && (
                  <div className="product-details-page__meta-item">
                    <span>Category</span>
                    <strong>{categoryId}</strong>
                  </div>
                )}

                <div className="product-details-page__meta-item">
                  <span>Product ID</span>
                  <strong>{product.id}</strong>
                </div>
              </div>

              {attributes &&
                Object.keys(attributes).length > 0 && (
                  <div className="product-details-page__attributes">
                    <h2>Product Attributes</h2>

                    <dl>
                      {Object.entries(
                        attributes,
                      ).map(([key, value]) => (
                        <div
                          key={key}
                          className="product-details-page__attribute"
                        >
                          <dt>{key}</dt>
                          <dd>
                            {typeof value ===
                            'object'
                              ? JSON.stringify(value)
                              : String(value)}
                          </dd>
                        </div>
                      ))}
                    </dl>
                  </div>
                )}

              <div className="product-details-page__actions">
                <Button
                  type="button"
                  onClick={handleAddToCart}
                  disabled={isAddingToCart}
                >
                  {isAddingToCart
                    ? 'Adding...'
                    : 'Add to Cart'}
                </Button>

                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleWishlistToggle}
                  disabled={isUpdatingWishlist}
                >
                  {isUpdatingWishlist
                    ? 'Updating...'
                    : isInWishlist
                      ? 'Remove from Wishlist'
                      : 'Add to Wishlist'}
                </Button>

                <Button
                  as={Link}
                  to="/products"
                  variant="secondary"
                >
                  Continue Shopping
                </Button>
              </div>

              {cartMessage && (
                <p
                  className="product-details-page__cart-message"
                  role="status"
                >
                  {cartMessage}
                </p>
              )}

              {cartError && (
                <p
                  className="product-details-page__cart-error"
                  role="alert"
                >
                  {cartError}
                </p>
              )}

              {wishlistMessage && (
                <p
                  className="product-details-page__wishlist-message"
                  role="status"
                >
                  {wishlistMessage}
                </p>
              )}

              {wishlistError && (
                <p
                  className="product-details-page__wishlist-error"
                  role="alert"
                >
                  {wishlistError}
                </p>
              )}
            </div>
          </Card>
        </div>
      </section>
    </PageContainer>
  );
}

export default ProductDetailsPage;