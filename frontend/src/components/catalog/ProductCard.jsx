
import { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import Card from '../ui/Card';
import Button from '../ui/Button';

import cartApi from '../../api/cartApi';
import wishlistApi from '../../api/wishlistApi';
import CartContext from '../../auth/CartContext';
import WishlistContext from '../../auth/WishlistContext';

function formatPrice(price) {
  if (price === null || price === undefined || price === '') {
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

function ProductCard({
  product,
  isInWishlist = false,
  onWishlistChange,
}) {
  const {
    id,
    name,
    description,
    price,
    imageUrls,
  } = product;

  const cartContext = useContext(CartContext);
  const refreshCartCount = cartContext?.refreshCartCount;

  const wishlistContext = useContext(WishlistContext);
  const refreshWishlistCount = wishlistContext?.refreshWishlistCount;

  const [isAddingToCart, setIsAddingToCart] = useState(false);
  const [cartMessage, setCartMessage] = useState('');
  const [cartError, setCartError] = useState('');

  const [isUpdatingWishlist, setIsUpdatingWishlist] = useState(false);
  const [wishlistMessage, setWishlistMessage] = useState('');
  const [wishlistError, setWishlistError] = useState('');

  useEffect(() => {
    if (!cartMessage) return undefined;

    const timeoutId = setTimeout(() => setCartMessage(''), 3000);
    return () => clearTimeout(timeoutId);
  }, [cartMessage]);

  useEffect(() => {
    if (!wishlistMessage) return undefined;

    const timeoutId = setTimeout(() => setWishlistMessage(''), 3000);
    return () => clearTimeout(timeoutId);
  }, [wishlistMessage]);

  const imageUrl =
    Array.isArray(imageUrls) && imageUrls.length > 0
      ? imageUrls[0]
      : null;

  const handleAddToCart = async () => {
    setIsAddingToCart(true);
    setCartMessage('');
    setCartError('');
    setWishlistMessage('');
    setWishlistError('');

    try {
      await cartApi.addItem(id, 1);

      try {
        await refreshCartCount?.();
      } catch {
        // A badge refresh failure must not undo a successful cart addition.
      }

      if (isInWishlist) {
        try {
          await wishlistApi.removeItem(id);
          onWishlistChange?.(id, false);

          try {
            await refreshWishlistCount?.();
          } catch {
            // Keep the successful cart addition even if refreshing fails.
          }

          setCartMessage('Added to cart.');
          setWishlistMessage('Removed from wishlist.');
        } catch (requestError) {
          setCartMessage(
            'Added to cart, but the wishlist could not be updated.',
          );
          setWishlistError(
            requestError?.data?.message ||
              requestError?.message ||
              'The product was added to the cart, but could not be removed from your wishlist. Please try again.',
          );
        }
      } else {
        setCartMessage('Added to cart.');
      }
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
    setIsUpdatingWishlist(true);
    setWishlistMessage('');
    setWishlistError('');

    try {
      if (isInWishlist) {
        await wishlistApi.removeItem(id);
        onWishlistChange?.(id, false);
        setWishlistMessage('Removed from wishlist.');
      } else {
        await wishlistApi.addItem(id);
        onWishlistChange?.(id, true);
        setWishlistMessage('Added to wishlist.');
      }

      try {
        await refreshWishlistCount?.();
      } catch {
        // The wishlist action succeeded; a count refresh is best-effort.
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

  const wishlistLabel = isInWishlist
    ? `Remove ${name} from wishlist`
    : `Add ${name} to wishlist`;

  return (
    <Card className="product-card">
      <Link
        to={`/products/${encodeURIComponent(id)}`}
        className="product-card__image-link"
        aria-label={`View ${name}`}
      >
        <div className="product-card__image-wrapper">
          {imageUrl ? (
            <img
              src={imageUrl}
              alt={name}
              className="product-card__image"
            />
          ) : (
            <div
              className="product-card__image-placeholder"
              aria-hidden="true"
            >
              <span>No Image</span>
            </div>
          )}
        </div>
      </Link>

      <div className="product-card__content">
        <h2 className="product-card__name">
          <Link to={`/products/${encodeURIComponent(id)}`}>
            {name}
          </Link>
        </h2>

        {description && (
          <p className="product-card__description">{description}</p>
        )}

        <div className="product-card__footer">
          <span className="product-card__price">
            {formatPrice(price)}
          </span>

          <Button
            as={Link}
            to={`/products/${encodeURIComponent(id)}`}
            variant="secondary"
          >
            View
          </Button>
        </div>

        <div className="product-card__cart-action">
          <Button
            type="button"
            onClick={handleAddToCart}
            disabled={isAddingToCart}
          >
            {isAddingToCart ? 'Adding...' : 'Add to Cart'}
          </Button>
        </div>

        {cartMessage && (
          <p className="product-card__cart-message" role="status">
            {cartMessage}
          </p>
        )}

        {cartError && (
          <p className="product-card__cart-error" role="alert">
            {cartError}
          </p>
        )}

        <div className="product-card__wishlist-action">
          <button
            type="button"
            className={`product-card__wishlist-heart${
              isInWishlist
                ? ' product-card__wishlist-heart--active'
                : ''
            }`}
            onClick={handleWishlistToggle}
            disabled={isUpdatingWishlist || isAddingToCart}
            aria-label={wishlistLabel}
            title={wishlistLabel}
          >
            <span aria-hidden="true">{isInWishlist ? '♥' : '♡'}</span>
          </button>
        </div>

        {wishlistMessage && (
          <p className="product-card__wishlist-message" role="status">
            {wishlistMessage}
          </p>
        )}

        {wishlistError && (
          <p className="product-card__wishlist-error" role="alert">
            {wishlistError}
          </p>
        )}
      </div>
    </Card>
  );
}

export default ProductCard;