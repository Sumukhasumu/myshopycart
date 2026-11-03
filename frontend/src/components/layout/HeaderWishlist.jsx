
import { useContext } from 'react';
import { Link } from 'react-router-dom';

import WishlistContext from '../../auth/WishlistContext';

function HeaderWishlist() {
  const wishlistContext = useContext(WishlistContext);
  const wishlistCount = wishlistContext?.wishlistCount ?? 0;

  return (
    <Link
      to="/wishlist"
      className="header-wishlist"
      aria-label={`Wishlist${wishlistCount > 0
        ? `, ${wishlistCount} ${wishlistCount === 1 ? 'item' : 'items'}`
        : ', empty'}`}
    >
      <span className="header-wishlist-icon" aria-hidden="true">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78Z"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </span>

      {wishlistCount > 0 && (
        <span className="header-cart-badge" aria-hidden="true">
          {wishlistCount > 99 ? '99+' : wishlistCount}
        </span>
      )}

      <span className="header-wishlist-label">Wishlist</span>
    </Link>
  );
}

export default HeaderWishlist;
