
import { Link } from 'react-router-dom';

import useAuth from '../auth/useAuth';
import './DistributorDashboardPage.css';

function DistributorDashboardPage() {
  const { user } = useAuth();

  const displayName =
    user?.username?.trim() || 'Distributor';

  return (
    <main className="distributor-dashboard">
      <section
        className="distributor-dashboard__welcome"
        aria-labelledby="distributor-dashboard-title"
      >
        <p className="distributor-dashboard__eyebrow">
          MYSHOPYCART PARTNER PORTAL
        </p>

        <h1 id="distributor-dashboard-title">
          Distributor Dashboard
        </h1>

        <p>
          Welcome, {displayName}. Manage your distributor
          activities from one place.
        </p>
      </section>

      <section
        className="distributor-dashboard__account"
        aria-labelledby="distributor-account-title"
      >
        <h2 id="distributor-account-title">
          Account Information
        </h2>

        <dl className="distributor-dashboard__details">
          <div>
            <dt>Username</dt>
            <dd>{user?.username || 'Not available'}</dd>
          </div>

          <div>
            <dt>Email</dt>
            <dd>{user?.email || 'Not available'}</dd>
          </div>

          <div>
            <dt>Account Role</dt>
            <dd>{user?.role || 'Not available'}</dd>
          </div>
        </dl>
      </section>

      <section
        className="distributor-dashboard__section"
        aria-labelledby="distributor-activities-title"
      >
        <h2 id="distributor-activities-title">
          Distributor Activities
        </h2>

        <div className="distributor-dashboard__cards">
          <article className="distributor-dashboard__card">
            <h3>Product Catalogue</h3>
            <p>
              Browse the products currently available in
              MYSHOPYCART.
            </p>
            <Link to="/products">
              Browse Products
            </Link>
          </article>

          <article className="distributor-dashboard__card">
            <h3>Product Management</h3>
            <p>
              View and manage the products associated with
              your distributor account.
            </p>
            <Link to="/distributor/products">
              Manage My Products
            </Link>
          </article>

          <article className="distributor-dashboard__card">
            <h3>Inventory Operations</h3>
            <p>
              View available and reserved stock, initialize
              inventory, and add stock for your products.
            </p>
            <Link to="/distributor/inventory">
              Manage My Inventory
            </Link>
          </article>
        </div>
      </section>
    </main>
  );
}

export default DistributorDashboardPage;