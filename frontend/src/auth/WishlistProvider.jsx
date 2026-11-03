
import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';

import wishlistApi from '../api/wishlistApi';
import WishlistContext from './WishlistContext';

function getWishlistCount(wishlist) {
  return Array.isArray(wishlist?.items)
    ? wishlist.items.length
    : 0;
}

function WishlistProvider({ children }) {
  const [wishlistCount, setWishlistCount] = useState(0);

  const refreshWishlistCount = useCallback(async () => {
    try {
      const wishlist = await wishlistApi.getWishlist();
      setWishlistCount(getWishlistCount(wishlist));
    } catch {
      // Preserve the last known count if a refresh temporarily fails.
    }
  }, []);

  useEffect(() => {
    let active = true;

    wishlistApi
      .getWishlist()
      .then((wishlist) => {
        if (active) {
          setWishlistCount(getWishlistCount(wishlist));
        }
      })
      .catch(() => {
        if (active) {
          setWishlistCount(0);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const value = useMemo(
    () => ({
      wishlistCount,
      refreshWishlistCount,
    }),
    [wishlistCount, refreshWishlistCount],
  );

  return (
    <WishlistContext.Provider value={value}>
      {children}
    </WishlistContext.Provider>
  );
}

export default WishlistProvider;
