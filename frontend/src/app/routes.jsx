import {
  createBrowserRouter,
  Navigate,
} from 'react-router-dom';

import AppShell from './AppShell';
import ProtectedRoute from '../auth/ProtectedRoute';

import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import ProductsPage from '../pages/ProductsPage';
import CategoryPage from '../pages/CategoryPage';
import ProductDetailsPage from '../pages/ProductDetailsPage';
import CartPage from '../pages/CartPage';
import WishlistPage from '../pages/WishlistPage';
import CheckoutPage from '../pages/CheckoutPage';
import OrdersPage from '../pages/OrdersPage';
import OrderDetailsPage from '../pages/OrderDetailsPage';
import PaymentPage from '../pages/PaymentPage';
import PaymentResultPage from '../pages/PaymentResultPage';
import ProfilePage from '../pages/ProfilePage';
import SettingsPage from '../pages/SettingsPage';
import DistributorDashboardPage from '../pages/DistributorDashboardPage';
import DistributorProductsPage from '../pages/DistributorProductsPage';
import DistributorInventoryPage from '../pages/DistributorInventoryPage';
import AdminDashboardPage from '../pages/AdminDashboardPage';
import AdminCategoriesPage from '../pages/AdminCategoriesPage';
import AdminProductsPage from '../pages/AdminProductsPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'register',
        lazy: async () => {
          const { default: RegisterPage } =
            await import('../pages/RegisterPage');

          return { Component: RegisterPage };
        },
      },
      {
        path: 'products',
        element: <ProductsPage />,
      },
      {
        path: 'products/:productId',
        element: <ProductDetailsPage />,
      },
      {
        path: 'categories/:categoryId',
        element: <CategoryPage />,
      },
      {
        element: <ProtectedRoute allowedRoles={['ADMIN']} />,
        children: [
          {
            path: 'admin/dashboard',
            element: <AdminDashboardPage />,
          },
          {
            path: 'admin/categories',
            element: <AdminCategoriesPage />,
          },
          {
            path: 'admin/products',
            element: <AdminProductsPage />,
          },
        ],
      },
      {
        element: <ProtectedRoute allowedRoles={['DISTRIBUTOR']} />,
        children: [
          {
            path: 'distributor/dashboard',
            element: <DistributorDashboardPage />,
          },
          {
            path: 'distributor/products',
            element: <DistributorProductsPage />,
          },
          {
            path: 'distributor/inventory',
            element: <DistributorInventoryPage />,
          },
        ],
      },
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: 'cart',
            element: <CartPage />,
          },
          {
            path: 'wishlist',
            element: <WishlistPage />,
          },
          {
            path: 'checkout',
            element: <CheckoutPage />,
          },
          {
            path: 'payment/:orderId',
            element: <PaymentPage />,
          },
          {
            path: 'payment/result',
            element: <PaymentResultPage />,
          },
          {
            path: 'orders',
            element: <OrdersPage />,
          },
          {
            path: 'orders/:orderId',
            element: <OrderDetailsPage />,
          },
          {
            path: 'profile',
            element: <ProfilePage />,
          },
          {
            path: 'settings',
            element: <SettingsPage />,
          },
        ],
      },
      {
        path: '*',
        element: <Navigate to="/" replace />,
      },
    ],
  },
]);

export default router;