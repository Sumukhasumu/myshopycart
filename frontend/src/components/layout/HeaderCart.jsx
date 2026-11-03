import { useContext } from 'react';
import { Link } from 'react-router-dom';

import CartContext from '../../auth/CartContext';

function HeaderCart() {
  const cartContext = useContext(CartContext);
  const cartCount = cartContext?.cartCount ?? 0;

  return (
    <Link
      className="header-cart"
      to="/cart"
      aria-label={`Shopping cart${cartCount > 0 ? `, ${cartCount} ${cartCount === 1 ? 'item' : 'items'}` : ', empty'}`}
    >
      <span className="header-cart-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" role="img">
          <path
            d="M3 4h2l2.2 10.2a2 2 0 0 0 2 1.6h7.7a2 2 0 0 0 1.9-1.4L21 7H6"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <circle cx="10" cy="19" r="1.4" fill="currentColor" />
          <circle cx="18" cy="19" r="1.4" fill="currentColor" />
        </svg>
      </span>

      {cartCount > 0 && (
        <span className="header-cart-badge" aria-hidden="true">
          {cartCount > 99 ? '99+' : cartCount}
        </span>
      )}

      <span className="header-cart-label">Cart</span>
    </Link>
  );
}

export default HeaderCart;