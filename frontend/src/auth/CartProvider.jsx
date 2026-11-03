import { useCallback, useEffect, useMemo, useState } from 'react';

import cartApi from '../api/cartApi';
import CartContext from './CartContext';

function getTotalQuantity(cart) {
  return (cart?.items || []).reduce(
    (total, item) => total + (Number(item.quantity) || 0),
    0,
  );
}

function CartProvider({ children }) {
  const [cartCount, setCartCount] = useState(0);

  const refreshCartCount = useCallback(async () => {
    try {
      const cart = await cartApi.getCart();
      setCartCount(getTotalQuantity(cart));
    } catch {
      setCartCount(0);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function loadCartCount() {
      try {
        const cart = await cartApi.getCart();

        if (!cancelled) {
          setCartCount(getTotalQuantity(cart));
        }
      } catch {
        if (!cancelled) {
          setCartCount(0);
        }
      }
    }

    loadCartCount();

    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo(
    () => ({
      cartCount,
      refreshCartCount,
    }),
    [cartCount, refreshCartCount],
  );

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
}

export default CartProvider;