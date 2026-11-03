
import { Routes } from '@angular/router';
import { LoginPage } from './pages/login/login';
import { RegisterPage } from './pages/register/register';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Public routes
  {
    path: '',
    title: 'MYSHOPYCART',
    loadComponent: () =>
      import('./pages/home/home').then((m) => m.HomePage),
  },
  {
    path: 'login',
    title: 'Sign in | MYSHOPYCART',
    component: LoginPage,
  },
  {
    path: 'register',
    title: 'Create Account | MYSHOPYCART',
    component: RegisterPage,
  },
  {
    path: 'products',
    title: 'Products | MYSHOPYCART',
    loadComponent: () =>
      import('./pages/products/products').then((m) => m.ProductsPage),
  },
  {
    path: 'products/:productId',
    title: 'Product Details | MYSHOPYCART',
    loadComponent: () =>
      import('./pages/product-details/product-details').then(
        (m) => m.ProductDetailsPage,
      ),
  },
  {
    path: 'categories',
    title: 'Categories | MYSHOPYCART',
    loadComponent: () =>
      import('./pages/categories/categories').then((m) => m.CategoriesPage),
  },
  {
    path: 'categories/:categoryId',
    title: 'Category | MYSHOPYCART',
    loadComponent: () =>
      import('./pages/category/category').then((m) => m.CategoryPage),
  },

  // Admin-only routes
  {
    path: 'admin/dashboard',
    title: 'Admin Dashboard | MYSHOPYCART',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () =>
      import('./pages/admin/dashboard/admin-dashboard').then(
        (m) => m.AdminDashboardPage,
      ),
  },
  {
    path: 'admin/categories',
    title: 'Manage Categories | MYSHOPYCART',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () =>
      import('./pages/admin/categories/admin-categories').then(
        (m) => m.AdminCategoriesPage,
      ),
  },
  {
    path: 'admin/products',
    title: 'Manage Products | MYSHOPYCART',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () =>
      import('./pages/admin/products/admin-products').then(
        (m) => m.AdminProductsPage,
      ),
  },

  // Distributor-only routes
  {
    path: 'distributor/dashboard',
    title: 'Distributor Dashboard | MYSHOPYCART',
    canActivate: [roleGuard(['DISTRIBUTOR'])],
    loadComponent: () =>
      import('./pages/distributor/dashboard/distributor-dashboard').then(
        (m) => m.DistributorDashboardPage,
      ),
  },
  {
    path: 'distributor/products',
    title: 'Distributor Products | MYSHOPYCART',
    canActivate: [roleGuard(['DISTRIBUTOR'])],
    loadComponent: () =>
      import('./pages/distributor/products/distributor-products').then(
        (m) => m.DistributorProductsPage,
      ),
  },
  {
    path: 'distributor/inventory',
    title: 'Distributor Inventory | MYSHOPYCART',
    canActivate: [roleGuard(['DISTRIBUTOR'])],
    loadComponent: () =>
      import('./pages/distributor/inventory/distributor-inventory').then(
        (m) => m.DistributorInventoryPage,
      ),
  },

  // Authenticated-user routes
  {
    path: 'cart',
    title: 'Shopping Cart | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/cart/cart').then((m) => m.CartPage),
  },
  {
    path: 'wishlist',
    title: 'My Wishlist | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/wishlist/wishlist').then((m) => m.WishlistPage),
  },
  {
    path: 'checkout',
    title: 'Checkout | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/checkout/checkout').then((m) => m.CheckoutPage),
  },
  {
    path: 'payment/result',
    title: 'Payment Result | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/payment-result').then((m) => m.PaymentResultPage),
  },
  {
    path: 'payment/:orderId',
    title: 'Payment | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/payment/payment').then((m) => m.PaymentPage),
  },
  {
    path: 'orders/:orderId',
    title: 'Order Details | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/order-details/order-details').then(
        (m) => m.OrderDetailsPage,
      ),
  },
  {
    path: 'orders',
    title: 'My Orders | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/orders/orders').then((m) => m.OrdersPage),
  },

  // Profile and Settings will be implemented next.
  {
    path: 'profile',
    title: 'Profile | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile/profile').then((m) => m.ProfilePage),
  },
  {
    path: 'settings',
    title: 'Settings | MYSHOPYCART',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/settings/settings').then((m) => m.SettingsPage),
  },

  // Fallback
  {
    path: '**',
    redirectTo: '',
  },
];
