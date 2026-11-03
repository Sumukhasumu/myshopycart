
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import orderApi from '../api/orderApi';

import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import Spinner from '../components/ui/Spinner';

import './OrdersPage.css';

function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const loadOrders = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await orderApi.getOrders();

        if (cancelled) {
          return;
        }

        setOrders(Array.isArray(response) ? response : []);
      } catch (requestError) {
        if (cancelled) {
          return;
        }

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load your orders.',
        );
        setOrders([]);
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  const getStatusClassName = (status) => {
    switch (status) {
      case 'CREATED':
        return 'order-status--created';
      case 'CONFIRMED':
        return 'order-status--confirmed';
      case 'PROCESSING':
        return 'order-status--processing';
      case 'SHIPPED':
        return 'order-status--shipped';
      case 'DELIVERED':
        return 'order-status--delivered';
      case 'CANCELLED':
        return 'order-status--cancelled';
      default:
        return 'order-status--unknown';
    }
  };

  const formatPrice = (price) =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(Number(price || 0));

  const formatDate = (dateValue) => {
    if (!dateValue) {
      return 'Date unavailable';
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
      return 'Date unavailable';
    }

    return new Intl.DateTimeFormat('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(date);
  };

  if (loading) {
    return (
      <section className="orders-page">
        <div className="page-container">
          <header className="orders-page-header">
            <p className="orders-page-eyebrow">MYSHOPYCART</p>
            <h1>Your Orders</h1>
            <p className="orders-page-description">
              View your recent orders and their current status.
            </p>
          </header>

          <div className="orders-loading">
            <Spinner />
            <p>Loading your orders...</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="orders-page">
      <div className="page-container">
        <header className="orders-page-header">
          <p className="orders-page-eyebrow">MYSHOPYCART</p>
          <h1>Your Orders</h1>
          <p className="orders-page-description">
            View your recent orders and their current status.
          </p>
        </header>

        {error && (
          <div className="orders-error">
            <Alert
              variant="error"
              title="Orders unavailable"
              message={error}
            />
          </div>
        )}

        {!error && orders.length === 0 && (
          <EmptyState
            title="No orders yet"
            description="Orders you place through MYSHOPYCART will appear here."
          />
        )}

        {!error && orders.length > 0 && (
          <div className="orders-list">
            {orders.map((order) => (
              <article className="order-card" key={order.id}>
                <div className="order-card-header">
                  <div>
                    <p className="order-card-label">Order</p>
                    <h2>#{order.id}</h2>
                  </div>

                  <span
                    className={`order-status ${getStatusClassName(
                      order.status,
                    )}`}
                  >
                    {order.status || 'UNKNOWN'}
                  </span>
                </div>

                <div className="order-card-summary">
                  <div>
                    <span className="order-card-label">
                      Order Date
                    </span>
                    <strong>{formatDate(order.createdAt)}</strong>
                  </div>

                  <div>
                    <span className="order-card-label">Items</span>
                    <strong>{order.items?.length || 0}</strong>
                  </div>

                  <div>
                    <span className="order-card-label">Total</span>
                    <strong>{formatPrice(order.totalAmount)}</strong>
                  </div>
                </div>

                <div className="order-card-items">
                  <h3>Items</h3>

                  {order.items?.map((item) => (
                    <div
                      className="order-item-row"
                      key={item.id}
                    >
                      <div>
                        <strong>
                          {item.productName || item.productId}
                        </strong>

                        <span>
                          Product ID: {item.productId}
                        </span>
                      </div>

                      <div className="order-item-quantity">
                        <span>Qty: {item.quantity}</span>

                        <strong>
                          {formatPrice(
                            Number(item.price || 0) *
                              Number(item.quantity || 0),
                          )}
                        </strong>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="order-card-actions">
                  <Link
                    className="order-details-link"
                    to={`/orders/${order.id}`}
                    aria-label={`View details for order ${order.id}`}
                  >
                    View Order Details
                    <span aria-hidden="true"> &rarr;</span>
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default OrdersPage;
