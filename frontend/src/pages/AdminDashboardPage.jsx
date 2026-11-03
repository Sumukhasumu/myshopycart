import { Link } from 'react-router-dom';
import PageContainer from '../components/layout/PageContainer';
import useAuth from '../auth/useAuth';
import './AdminDashboardPage.css';

function AdminDashboardPage() {
  const { user } = useAuth();

  return (
    <PageContainer>
      <section className="admin-dashboard">
        <header className="admin-dashboard__header">
          <p className="admin-dashboard__eyebrow">
            MYSHOPYCART ADMINISTRATION
          </p>

          <h1 className="admin-dashboard__title">
            Admin Dashboard
          </h1>

          <p className="admin-dashboard__description">
            Welcome to your administration workspace.
            Manage products, maintain product categories,
            browse the catalogue, and access your account.
          </p>
        </header>

        <section
          className="admin-dashboard__account"
          aria-labelledby="admin-account-title"
        >
          <h2 id="admin-account-title">
            Account Information
          </h2>

          <dl className="admin-dashboard__details">
            <div>
              <dt>Username</dt>
              <dd>{user?.username || 'Not available'}</dd>
            </div>

            <div>
              <dt>Email</dt>
              <dd>{user?.email || 'Not available'}</dd>
            </div>

            <div>
              <dt>Role</dt>
              <dd>{user?.role || 'Not available'}</dd>
            </div>
          </dl>
        </section>

        <section
          className="admin-dashboard__sections"
          aria-labelledby="admin-sections-title"
        >
          <h2 id="admin-sections-title">
            Administration Sections
          </h2>

          <div className="admin-dashboard__grid">
            <article className="admin-dashboard__card">
              <h3>Manage Products</h3>

              <p>
                View the catalogue, create products,
                update product details, and remove products.
              </p>

              <Link
                className="admin-dashboard__link"
                to="/admin/products"
              >
                Open Product Management
              </Link>
            </article>

            <article className="admin-dashboard__card">
              <h3>Product Catalogue</h3>

              <p>
                Browse the customer-facing catalogue,
                search products, filter categories, and
                view product details.
              </p>

              <Link
                className="admin-dashboard__link"
                to="/products"
              >
                Open Product Catalogue
              </Link>
            </article>

            <article className="admin-dashboard__card">
              <h3>Manage Categories</h3>

              <p>
                Create categories, update category details,
                and remove categories when appropriate.
              </p>

              <Link
                className="admin-dashboard__link"
                to="/admin/categories"
              >
                Open Category Management
              </Link>
            </article>

            <article className="admin-dashboard__card">
              <h3>My Account</h3>

              <p>
                View your account and access the
                available profile settings.
              </p>

              <Link
                className="admin-dashboard__link"
                to="/profile"
              >
                Open My Profile
              </Link>
            </article>
          </div>
        </section>
      </section>
    </PageContainer>
  );
}

export default AdminDashboardPage;