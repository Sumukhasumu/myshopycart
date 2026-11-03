import { useContext, useEffect, useState } from 'react';

import wishlistApi from '../api/wishlistApi';
import productApi from '../api/productApi';

import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';
import ProductCard from '../components/catalog/ProductCard';
import WishlistContext from '../auth/WishlistContext';

import './WishlistPage.css';

function WishlistPage() {
  const wishlistContext = useContext(WishlistContext);
  const refreshWishlistCount =
    wishlistContext?.refreshWishlistCount;

  const [wishlist, setWishlist] = useState(null);
  const [products, setProducts] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    let isActive = true;

    wishlistApi
      .getWishlist()
      .then(async (response) => {
        if (!isActive) {
          return;
        }

        setWishlist(response);
        setError('');

        const wishlistItems = response?.items || [];

        if (wishlistItems.length === 0) {
          setProducts({});
          return;
        }

        const productResults = await Promise.all(
          wishlistItems.map(async (item) => {
            const product =
              await productApi.getProductById(
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

        setWishlist(null);
        setProducts({});

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load your wishlist.',
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

  const wishlistItems = wishlist?.items || [];

  const handleRetry = () => {
    window.location.reload();
  };

  const handleWishlistChange = (
    productId,
    isInWishlist,
  ) => {
    if (isInWishlist) {
      return;
    }

    setActionError('');

    setWishlist((currentWishlist) => {
      if (!currentWishlist) {
        return currentWishlist;
      }

      return {
        ...currentWishlist,
        items: currentWishlist.items.filter(
          (item) => item.productId !== productId,
        ),
      };
    });

    setProducts((currentProducts) => {
      const updatedProducts = {
        ...currentProducts,
      };

      delete updatedProducts[productId];

      return updatedProducts;
    });

    // Update the wishlist badge in the header.
    refreshWishlistCount?.();
  };

  return (
    <main className="wishlist-page">
      <header className="wishlist-page-header">
        <p className="wishlist-page-eyebrow">
          Saved Items
        </p>

        <h1>My Wishlist</h1>

        <p className="wishlist-page-description">
          Keep products you love in one place and
          come back to them whenever you are ready.
        </p>
      </header>

      {error && (
        <Alert
          className="wishlist-error"
          variant="error"
        >
          {error}
        </Alert>
      )}

      {actionError && (
        <Alert
          className="wishlist-error"
          variant="error"
        >
          {actionError}
        </Alert>
      )}

      {isLoading ? (
        <section className="wishlist-loading">
          <Spinner />
          <p>Loading your wishlist...</p>
        </section>
      ) : error ? (
        <section className="wishlist-error-state">
          <EmptyState
            title="Wishlist unavailable"
            description="We could not load your wishlist right now. Please try again."
            actionLabel="Try Again"
            onAction={handleRetry}
          />
        </section>
      ) : wishlistItems.length === 0 ? (
        <section className="wishlist-empty-state">
          <EmptyState
            title="Your wishlist is empty"
            description="Products you save to your wishlist will appear here."
          />
        </section>
      ) : (
        <section className="wishlist-items">
          <div className="wishlist-items-header">
            <h2>Saved Products</h2>

            <span>
              {wishlistItems.length}{' '}
              {wishlistItems.length === 1
                ? 'item'
                : 'items'}
            </span>
          </div>

          <div className="wishlist-product-grid">
            {wishlistItems.map((item) => {
              const product = products[item.productId];

              if (!product) {
                return (
                  <article
                    className="wishlist-product-missing"
                    key={item.productId}
                  >
                    <p>
                      Product {item.productId} is
                      currently unavailable.
                    </p>
                  </article>
                );
              }

              return (
                <article
                  className="wishlist-product-item"
                  key={item.productId}
                >
                  <ProductCard
                    product={product}
                    isInWishlist
                    onWishlistChange={
                      handleWishlistChange
                    }
                  />
                </article>
              );
            })}
          </div>
        </section>
      )}
    </main>
  );
}

export default WishlistPage;