
import { useContext, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import orderApi from '../api/orderApi';
import cartApi from '../api/cartApi';
import CartContext from '../auth/CartContext';
import Alert from '../components/ui/Alert';
import Button from '../components/ui/Button';
import Spinner from '../components/ui/Spinner';

import './PaymentPage.css';

function formatPrice(value) {
  const amount = Number(value);

  if (!Number.isFinite(amount)) {
    return 'Price unavailable';
  }

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount);
}

const PAYMENT_METHODS = [
  {
    value: 'UPI',
    title: 'UPI',
    description: 'Simulated UPI payment',
    icon: '↗',
  },
  {
    value: 'CARD',
    title: 'Credit or Debit Card',
    description: 'Simulated card payment',
    icon: '▤',
  },
  {
    value: 'DIGITAL_WALLET',
    title: 'Digital Wallet',
    description: 'Simulated wallet payment',
    icon: '▣',
  },
];

function PaymentPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { refreshCartCount } = useContext(CartContext) || {};

  const [order, setOrder] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState('UPI');
  const [simulationMode, setSimulationMode] = useState('SUCCESS');
  const [isLoading, setIsLoading] = useState(true);
  const [isPaying, setIsPaying] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    orderApi
      .getOrderById(orderId)
      .then((result) => {
        if (!active) return;

        setOrder(result);

        if (result?.status && result.status !== 'CREATED') {
          setError(
            `This order is ${result.status} and is not awaiting payment.`,
          );
        }
      })
      .catch((requestError) => {
        if (!active) return;

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load this order for payment.',
        );
      })
      .finally(() => {
        if (active) setIsLoading(false);
      });

    return () => {
      active = false;
    };
  }, [orderId]);

  const handlePayment = async (event) => {
    event.preventDefault();

    if (!order || isPaying || order.status !== 'CREATED') {
      return;
    }

    setError('');
    setIsPaying(true);

    try {
      const result = await orderApi.processPayment(
        orderId,
        selectedMethod,
        simulationMode,
      );

      const finalStatus = result?.status;
      const succeeded = finalStatus === 'CONFIRMED';

      let cartCleanupFailed = false;

      if (succeeded) {
        try {
          await cartApi.clearCart();

          try {
            await refreshCartCount?.();
          } catch (refreshError) {
            console.error(
              'Payment succeeded and cart was cleared, but the cart badge refresh failed.',
              refreshError,
            );
          }
        } catch (cleanupError) {
          cartCleanupFailed = true;
          console.error(
            'Payment succeeded, but clearing the cart failed.',
            cleanupError,
          );
        }
      }

      const params = new URLSearchParams({
        orderId: String(orderId),
        status: succeeded ? 'SUCCESS' : 'FAILURE',
        method: selectedMethod,
      });

      if (cartCleanupFailed) {
        params.set('cartCleanup', 'failed');
      }

      navigate(`/payment/result?${params.toString()}`, {
        replace: true,
      });
    } catch (requestError) {
      setError(
        requestError?.data?.message ||
          requestError?.message ||
          'Payment could not be processed. Check your order status before retrying.',
      );
    } finally {
      setIsPaying(false);
    }
  };

  if (isLoading) {
    return (
      <main className="payment-page">
        <section className="payment-state">
          <Spinner />
          <p>Loading order...</p>
        </section>
      </main>
    );
  }

  return (
    <main className="payment-page">
      <header className="payment-header">
        <p className="payment-eyebrow">Secure Checkout</p>
        <h1>Choose a payment method</h1>
        <p>
          This is a dummy payment experience. No real payment will be taken.
        </p>
      </header>

      {error && (
        <Alert className="payment-error" variant="error">
          {error}
        </Alert>
      )}

      {!order ? (
        <section className="payment-card">
          <h2>Order unavailable</h2>
          <p>We could not load this order for the current account.</p>
          <Button type="button" onClick={() => navigate('/orders')}>
            View Orders
          </Button>
        </section>
      ) : (
        <div className="payment-layout">
          <form className="payment-card" onSubmit={handlePayment}>
            <h2>Payment method</h2>

            <div className="payment-method-list">
              {PAYMENT_METHODS.map((method) => (
                <label
                  className={`payment-method ${
                    selectedMethod === method.value ? 'is-selected' : ''
                  }`}
                  key={method.value}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value={method.value}
                    checked={selectedMethod === method.value}
                    onChange={() => setSelectedMethod(method.value)}
                    disabled={isPaying || order.status !== 'CREATED'}
                  />
                  <span className="payment-method-icon" aria-hidden="true">
                    {method.icon}
                  </span>
                  <span className="payment-method-copy">
                    <strong>{method.title}</strong>
                    <small>{method.description}</small>
                  </span>
                </label>
              ))}
            </div>

            <fieldset
              className="payment-simulation"
              disabled={isPaying || order.status !== 'CREATED'}
            >
              <legend>Simulation result</legend>
              <label>
                <input
                  type="radio"
                  name="simulationMode"
                  value="SUCCESS"
                  checked={simulationMode === 'SUCCESS'}
                  onChange={() => setSimulationMode('SUCCESS')}
                />
                Simulate successful payment
              </label>
              <label>
                <input
                  type="radio"
                  name="simulationMode"
                  value="FAILURE"
                  checked={simulationMode === 'FAILURE'}
                  onChange={() => setSimulationMode('FAILURE')}
                />
                Simulate failed payment
              </label>
              <p>
                Choose the outcome for testing. Do not enter real card, UPI,
                or wallet credentials.
              </p>
            </fieldset>

            <Button
              type="submit"
              variant="primary"
              disabled={
                isPaying ||
                order.status !== 'CREATED' ||
                !selectedMethod
              }
            >
              {isPaying ? 'Processing payment...' : 'Pay Now (Simulation)'}
            </Button>

            <button
              className="payment-back-button"
              type="button"
              disabled={isPaying}
              onClick={() => navigate('/orders')}
            >
              Cancel and view orders
            </button>
          </form>

          <aside className="payment-card payment-order-summary">
            <p className="payment-eyebrow">Order summary</p>
            <h2>Order #{order.id ?? orderId}</h2>
            <div className="payment-summary-row">
              <span>Items</span>
              <strong>{order.items?.length ?? 0}</strong>
            </div>
            <div className="payment-summary-row">
              <span>Status</span>
              <strong>{order.status || 'Unknown'}</strong>
            </div>
            <div className="payment-summary-total">
              <span>Total</span>
              <strong>{formatPrice(order.totalAmount)}</strong>
            </div>
            <p className="payment-disclaimer">
              Demo only. This screen does not connect to real payment
              providers.
            </p>
          </aside>
        </div>
      )}
    </main>
  );
}

export default PaymentPage;