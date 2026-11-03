
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import orderApi from '../api/orderApi';

import Alert from '../components/ui/Alert';
import Spinner from '../components/ui/Spinner';

import './OrderDetailsPage.css';

function OrderDetailsPage() {
  const { orderId } = useParams();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [invoiceLoading, setInvoiceLoading] = useState(false);
  const [invoiceError, setInvoiceError] = useState('');
  const [invoiceMessage, setInvoiceMessage] = useState('');

  const [invoice, setInvoice] = useState(null);
  const [invoiceDetailsLoading, setInvoiceDetailsLoading] = useState(false);
  const [invoiceDetailsError, setInvoiceDetailsError] = useState('');
  const [invoiceDetailsUnavailable, setInvoiceDetailsUnavailable] =
    useState(false);

  useEffect(() => {
    let cancelled = false;

    const loadOrder = async () => {
      setLoading(true);
      setError('');
      setOrder(null);
      setInvoiceError('');
      setInvoiceMessage('');

      try {
        const response = await orderApi.getOrderById(orderId);

        if (cancelled) {
          return;
        }

        if (!response || String(response.id) !== String(orderId)) {
          setError('The requested order could not be found.');
          return;
        }

        setOrder(response);
      } catch (requestError) {
        if (cancelled) {
          return;
        }

        if (requestError?.status === 404) {
          setError('The requested order could not be found.');
        } else if (requestError?.status === 401) {
          setError('Please log in to view your order.');
        } else if (requestError?.status === 403) {
          setError('You are not allowed to view this order.');
        } else {
          setError(
            requestError?.data?.message ||
              requestError?.message ||
              'Unable to load order details. Please try again.',
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadOrder();

    return () => {
      cancelled = true;
    };
  }, [orderId]);

  useEffect(() => {
    let cancelled = false;

    const loadInvoiceDetails = async () => {
      setInvoice(null);
      setInvoiceDetailsError('');
      setInvoiceDetailsUnavailable(false);
      setInvoiceDetailsLoading(true);

      try {
        const response = await orderApi.getInvoiceDetails(orderId);

        if (!cancelled) {
          setInvoice(response || null);

          if (!response) {
            setInvoiceDetailsUnavailable(true);
          }
        }
      } catch (requestError) {
        if (cancelled) {
          return;
        }

        if (requestError?.status === 404) {
          setInvoiceDetailsUnavailable(true);
        } else if (requestError?.status === 401) {
          setInvoiceDetailsError(
            'Please log in to view invoice details.',
          );
        } else if (requestError?.status === 403) {
          setInvoiceDetailsError(
            'You are not allowed to view this invoice.',
          );
        } else {
          setInvoiceDetailsError(
            requestError?.data?.message ||
              requestError?.message ||
              'Unable to load invoice details. Please try again.',
          );
        }
      } finally {
        if (!cancelled) {
          setInvoiceDetailsLoading(false);
        }
      }
    };

    loadInvoiceDetails();

    return () => {
      cancelled = true;
    };
  }, [orderId]);

  const handleDownloadInvoice = async () => {
    setInvoiceLoading(true);
    setInvoiceError('');
    setInvoiceMessage('');

    let pdfUrl;

    try {
      const pdfBlob = await orderApi.downloadInvoice(orderId);

      if (!(pdfBlob instanceof Blob) || pdfBlob.size === 0) {
        throw new Error('The invoice PDF is empty or unavailable.');
      }

      pdfUrl = URL.createObjectURL(pdfBlob);

      const link = document.createElement('a');
      link.href = pdfUrl;
      link.download = `MYSHOPYCART-Order-${orderId}-Invoice.pdf`;

      document.body.appendChild(link);
      link.click();
      link.remove();

      setInvoiceMessage('Invoice download started.');
    } catch (requestError) {
      if (requestError?.status === 401) {
        setInvoiceError('Please log in to download this invoice.');
      } else if (requestError?.status === 403) {
        setInvoiceError(
          'You are not allowed to download this invoice.',
        );
      } else if (requestError?.status === 404) {
        setInvoiceError(
          'An invoice is not available for this order yet.',
        );
      } else {
        setInvoiceError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to download the invoice. Please try again.',
        );
      }
    } finally {
      if (pdfUrl) {
        URL.revokeObjectURL(pdfUrl);
      }

      setInvoiceLoading(false);
    }
  };

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

  const renderAddress = (address) => {
    if (!address) {
      return <p className="order-details-muted">Address not provided.</p>;
    }

    const addressLines = [
      address.address,
      address.city,
      address.state,
      address.postalCode,
      address.country,
    ].filter(Boolean);

    if (addressLines.length === 0) {
      return <p className="order-details-muted">Address not provided.</p>;
    }

    return (
      <p>
        {addressLines.map((line, index) => (
          <span key={`${line}-${index}`}>
            {line}
            {index < addressLines.length - 1 && <br />}
          </span>
        ))}
      </p>
    );
  };

  if (loading) {
    return (
      <section className="order-details-page">
        <div className="page-container">
          <nav
            className="order-details-breadcrumb"
            aria-label="Breadcrumb"
          >
            <Link to="/orders">Your Orders</Link>
            <span aria-hidden="true">/</span>
            <span>Loading order...</span>
          </nav>

          <div className="order-details-loading">
            <Spinner />
            <p>Loading order details...</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="order-details-page">
      <div className="page-container">
        <nav
          className="order-details-breadcrumb"
          aria-label="Breadcrumb"
        >
          <Link to="/orders">Your Orders</Link>
          <span aria-hidden="true">/</span>
          <span>Order #{orderId}</span>
        </nav>

        <header className="order-details-header">
          <div>
            <p className="order-details-eyebrow">MYSHOPYCART</p>
            <h1>Order Details</h1>
            <p className="order-details-description">
              Review your order, items, invoice, and order status.
            </p>
          </div>

          <Link className="order-details-back-link" to="/orders">
            Back to Orders
          </Link>
        </header>

        {error && (
          <div className="order-details-error">
            <Alert
              variant="error"
              title="Order unavailable"
              message={error}
            />
            <Link className="order-details-back-link" to="/orders">
              Back to Orders
            </Link>
          </div>
        )}

        {!error && order && (
          <>
            <article className="order-details-card">
              <div className="order-details-card-header">
                <div>
                  <p className="order-details-label">Order Number</p>
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

              <div className="order-details-metadata">
                <div>
                  <span className="order-details-label">Order Date</span>
                  <strong>{formatDate(order.createdAt)}</strong>
                </div>

                <div>
                  <span className="order-details-label">Last Updated</span>
                  <strong>{formatDate(order.updatedAt)}</strong>
                </div>
              </div>

              <div className="order-details-invoice-action">
                <button
                  type="button"
                  className="order-details-invoice-button"
                  onClick={handleDownloadInvoice}
                  disabled={invoiceLoading}
                >
                  {invoiceLoading
                    ? 'Preparing Invoice...'
                    : 'Download Invoice (PDF)'}
                </button>

                {invoiceMessage && (
                  <p
                    className="order-details-invoice-message"
                    role="status"
                  >
                    {invoiceMessage}
                  </p>
                )}

                {invoiceError && (
                  <div
                    className="order-details-invoice-error"
                    role="alert"
                  >
                    <Alert
                      variant="error"
                      title="Invoice download unavailable"
                      message={invoiceError}
                    />
                  </div>
                )}
              </div>
            </article>

            <article className="order-details-card">
              <div className="order-details-section-header">
                <h2>Invoice Details</h2>
                {invoice?.invoiceNumber && (
                  <span>{invoice.invoiceNumber}</span>
                )}
              </div>

              {invoiceDetailsLoading && (
                <div className="order-details-loading">
                  <Spinner />
                  <p>Loading invoice details...</p>
                </div>
              )}

              {!invoiceDetailsLoading && invoiceDetailsError && (
                <Alert
                  variant="error"
                  title="Invoice details unavailable"
                  message={invoiceDetailsError}
                />
              )}

              {!invoiceDetailsLoading &&
                !invoiceDetailsError &&
                invoiceDetailsUnavailable && (
                  <p className="order-details-muted">
                    An invoice is not available for this order yet.
                    It may be generated after successful payment.
                  </p>
                )}

              {!invoiceDetailsLoading && invoice && (
                <>
                  <div className="order-details-metadata">
                    <div>
                      <span className="order-details-label">
                        Invoice Number
                      </span>
                      <strong>
                        {invoice.invoiceNumber || 'Not available'}
                      </strong>
                    </div>

                    <div>
                      <span className="order-details-label">
                        Issue Date
                      </span>
                      <strong>
                        {formatDate(invoice.issueDate)}
                      </strong>
                    </div>

                    <div>
                      <span className="order-details-label">
                        Payment Method
                      </span>
                      <strong>
                        {invoice.paymentMethod || 'Not available'}
                      </strong>
                    </div>
                  </div>

                  <div className="order-details-layout">
                    <div className="order-details-card">
                      <h3>Billing Address</h3>
                      {renderAddress(invoice.billingAddress)}
                    </div>

                    <div className="order-details-card">
                      <h3>Shipping Address</h3>
                      {renderAddress(invoice.shippingAddress)}
                    </div>
                  </div>

                  <div className="order-details-customer">
                    <h3>Invoice Customer Information</h3>

                    <div className="order-details-customer-grid">
                      <div>
                        <span className="order-details-label">
                          Customer Name
                        </span>
                        <strong>
                          {invoice.customerName || 'Not provided'}
                        </strong>
                      </div>

                      <div>
                        <span className="order-details-label">
                          Email
                        </span>
                        <strong>
                          {invoice.customerEmail || 'Not provided'}
                        </strong>
                      </div>

                      <div>
                        <span className="order-details-label">
                          Phone
                        </span>
                        <strong>
                          {invoice.customerPhone || 'Not provided'}
                        </strong>
                      </div>

                      {invoice.customerGstin && (
                        <div>
                          <span className="order-details-label">
                            GSTIN
                          </span>
                          <strong>{invoice.customerGstin}</strong>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="order-details-items">
                    <h3>Invoice Items</h3>

                    {!invoice.items?.length ? (
                      <p className="order-details-muted">
                        No invoice line items are available.
                      </p>
                    ) : (
                      invoice.items.map((item, index) => (
                        <div
                          className="order-details-item"
                          key={`${item.productId || 'item'}-${index}`}
                        >
                          <div className="order-details-item-info">
                            <h3>
                              {item.productName ||
                                item.productId ||
                                'Product'}
                            </h3>
                            {item.productId && (
                              <p>Product ID: {item.productId}</p>
                            )}
                            <p>
                              Unit price: {formatPrice(item.unitPrice)}
                            </p>
                          </div>

                          <div className="order-details-item-total">
                            <span>Qty: {item.quantity ?? 0}</span>
                            <strong>
                              {formatPrice(item.lineTotal)}
                            </strong>
                          </div>
                        </div>
                      ))
                    )}
                  </div>

                  <div className="order-details-summary">
                    <div className="order-details-summary-row">
                      <span>Subtotal</span>
                      <strong>{formatPrice(invoice.subtotal)}</strong>
                    </div>

                    <div className="order-details-summary-row">
                      <span>Tax</span>
                      <strong>{formatPrice(invoice.taxAmount)}</strong>
                    </div>

                    <div className="order-details-summary-total">
                      <span>Invoice Total</span>
                      <strong>{formatPrice(invoice.totalAmount)}</strong>
                    </div>
                  </div>
                </>
              )}
            </article>

            <div className="order-details-layout">
              <article className="order-details-card">
                <div className="order-details-section-header">
                  <h2>Items in This Order</h2>
                  <span>
                    {order.items?.length || 0}{' '}
                    {(order.items?.length || 0) === 1 ? 'item' : 'items'}
                  </span>
                </div>

                {!order.items?.length ? (
                  <p className="order-details-muted">
                    No item details are available for this order.
                  </p>
                ) : (
                  <div className="order-details-items">
                    {order.items.map((item, index) => {
                      const quantity = Number(item.quantity || 0);
                      const unitPrice = Number(item.price || 0);

                      return (
                        <div
                          className="order-details-item"
                          key={item.id ?? `${item.productId}-${index}`}
                        >
                          <div className="order-details-item-info">
                            <h3>
                              {item.productName || item.productId}
                            </h3>
                            <p>Product ID: {item.productId}</p>
                            <p>
                              Unit price: {formatPrice(unitPrice)}
                            </p>
                          </div>

                          <div className="order-details-item-total">
                            <span>Qty: {quantity}</span>
                            <strong>
                              {formatPrice(unitPrice * quantity)}
                            </strong>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </article>

              <aside className="order-details-card order-details-summary">
                <h2>Order Summary</h2>

                <div className="order-details-summary-row">
                  <span>Items</span>
                  <strong>{order.items?.length || 0}</strong>
                </div>

                <div className="order-details-summary-row">
                  <span>Total quantity</span>
                  <strong>
                    {(order.items || []).reduce(
                      (total, item) =>
                        total + Number(item.quantity || 0),
                      0,
                    )}
                  </strong>
                </div>

                <div className="order-details-summary-total">
                  <span>Order Total</span>
                  <strong>{formatPrice(order.totalAmount)}</strong>
                </div>

                <p className="order-details-summary-note">
                  This total is provided by the Order Service.
                </p>
              </aside>
            </div>

            <article className="order-details-card order-details-customer">
              <h2>Customer Information</h2>

              <div className="order-details-customer-grid">
                <div>
                  <span className="order-details-label">
                    Customer Name
                  </span>
                  <strong>
                    {order.customerName || 'Not provided'}
                  </strong>
                </div>

                <div>
                  <span className="order-details-label">Email</span>
                  <strong>
                    {order.customerEmail || 'Not provided'}
                  </strong>
                </div>

                <div>
                  <span className="order-details-label">Mobile</span>
                  <strong>
                    {order.customerMobile || 'Not provided'}
                  </strong>
                </div>
              </div>
            </article>

            <div className="order-details-footer">
              <Link to="/orders">Back to Order History</Link>
            </div>
          </>
        )}
      </div>
    </section>
  );
}

export default OrderDetailsPage;