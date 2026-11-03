
import { RouterProvider } from 'react-router-dom';

import './App.css';
import AuthProvider from './auth/AuthContext.jsx';
import CartProvider from './auth/CartProvider.jsx';
import WishlistProvider from './auth/WishlistProvider.jsx';
import router from './app/routes';

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <WishlistProvider>
          <RouterProvider router={router} />
        </WishlistProvider>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
