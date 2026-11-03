import { Link, useSearchParams } from 'react-router-dom';
import './PaymentResultPage.css';

function PaymentResultPage() {
  const [searchParams] = useSearchParams();

  const orderId = searchParams.get('orderId');
  const status = searchParams.get('status')?.toUpperCase();
  const method = searchParams.get('method')?.toUpperCase();
  const cartCleanupFailed =
    searchParams.get('cartCleanup') === 'failed';

  const succeeded = status === 'SUCCESS';

  const paymentMethods = {
    UPI: 'UPI',
    CARD: 'Credit or Debit Card',
    DIGITAL_WALLET: 'Digital Wallet',
  };

  return (
    <main className="payment-result-page">
      <section
        className={`payment-result-card ${
          succeeded ? 'payment-result-success' : 'payment-result-failure'
        }`}
        aria-live="polite"
      >
        <div className="payment-result-icon" aria-hidden="true">
          {succeeded ? '✓' : '×'}
        </div>

        <p className="payment-result-eyebrow">MYSHOPYCART</p>

        <h1>
          {succeeded ? 'Payment Successful' : 'Payment Failed'}
        </h1>

        <p className="payment-result-message">
          {succeeded
            ? 'Your simulated payment was successful. Thank you for shopping with us!'
            : 'Your simulated payment failed and the order was cancelled. Your cart has been preserved so you can review your items and start a new order.'}
        </p>

        {succeeded && cartCleanupFailed && (
          <p className="payment-result-message" role="alert">
            Your payment succeeded, but we could not clear your cart.
            Please open your cart and remove any items that you have
            already purchased.
          </p>
        )}

        {orderId && (
          <div className="payment-result-details">
            <div>
              <span>Order number</span>
              <strong>#{orderId}</strong>
            </div>

            <div>
              <span>Payment method</span>
              <strong>
                {paymentMethods[method] || method || 'Not available'}
              </strong>
            </div>

            <div>
              <span>Result</span>
              <strong>{succeeded ? 'SUCCESS' : status || 'UNKNOWN'}</strong>
            </div>
          </div>
        )}

        <p className="payment-result-demo-note">
          Demo payment only. No real money was charged.
        </p>

        <div className="payment-result-actions">
          {orderId && (
            <Link
              className="payment-result-primary"
              to={`/orders/${encodeURIComponent(orderId)}`}
            >
              View Order
            </Link>
          )}

          {!succeeded && (
            <Link className="payment-result-secondary" to="/cart">
              Review Cart
            </Link>
          )}

          <Link className="payment-result-secondary" to="/orders">
            My Orders
          </Link>

          <Link className="payment-result-secondary" to="/products">
            Continue Shopping
          </Link>
        </div>
      </section>
    </main>
  );
}

export default PaymentResultPage;