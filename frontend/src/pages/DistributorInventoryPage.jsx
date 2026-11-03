import { useCallback, useEffect, useState } from 'react';

import inventoryApi from '../api/inventoryApi';
import productApi from '../api/productApi';
import Alert from '../components/ui/Alert';
import Spinner from '../components/ui/Spinner';

import './DistributorInventoryPage.css';

function DistributorInventoryPage() {
  const [inventory, setInventory] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const [selectedProductId, setSelectedProductId] = useState('');
  const [initialStock, setInitialStock] = useState('');

  const [stockProductId, setStockProductId] = useState('');
  const [additionalStock, setAdditionalStock] = useState('');

  const [adjustProductId, setAdjustProductId] = useState('');
  const [adjustmentOperation, setAdjustmentOperation] =
    useState('INCREASE');
  const [adjustmentQuantity, setAdjustmentQuantity] = useState('');
  const [adjustmentReason, setAdjustmentReason] = useState('');

  const [historyProductId, setHistoryProductId] = useState('');
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [inventoryResponse, productsResponse] =
        await Promise.all([
          inventoryApi.getMyInventory(),
          productApi.getMyProducts({ page: 0, size: 100 }),
        ]);

      setInventory(
        Array.isArray(inventoryResponse)
          ? inventoryResponse
          : [],
      );

      const pageContent = Array.isArray(productsResponse)
        ? productsResponse
        : productsResponse?.content;

      setProducts(
        Array.isArray(pageContent) ? pageContent : [],
      );
    } catch (requestError) {
      setError(
        requestError.message ||
          'Unable to load distributor inventory.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
  Promise.resolve().then(loadData);
  }, [loadData]);

  const inventoryProductIds = new Set(
    inventory.map((item) => String(item.productId)),
  );

  const productsWithoutInventory = products.filter(
    (product) =>
      !inventoryProductIds.has(String(product.id)),
  );

  async function handleCreateInventory(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!selectedProductId || initialStock === '') {
      setError('Select a product and enter initial stock.');
      return;
    }

    const quantity = Number(initialStock);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      setError('Initial stock must be a positive whole number.');
      return;
    }

    setSubmitting(true);

    try {
      await inventoryApi.createInventory({
        productId: selectedProductId,
        availableStock: quantity,
      });

      setMessage('Inventory initialized successfully.');
      setSelectedProductId('');
      setInitialStock('');

      await loadData();
    } catch (requestError) {
      setError(
        requestError.message ||
          'Unable to initialize inventory.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAddStock(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!stockProductId || additionalStock === '') {
      setError('Select a product and enter the stock quantity.');
      return;
    }

    const quantity = Number(additionalStock);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      setError('Additional stock must be a positive whole number.');
      return;
    }

    setSubmitting(true);

    try {
      await inventoryApi.addStock(stockProductId, quantity);

      setMessage('Stock added successfully.');
      setStockProductId('');
      setAdditionalStock('');

      await loadData();
    } catch (requestError) {
      setError(
        requestError.message || 'Unable to add stock.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAdjustStock(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (
      !adjustProductId ||
      adjustmentQuantity === '' ||
      !adjustmentReason.trim()
    ) {
      setError(
        'Select a product, enter a quantity, and provide a reason.',
      );
      return;
    }

    const quantity = Number(adjustmentQuantity);

    if (!Number.isInteger(quantity) || quantity <= 0) {
      setError('Adjustment quantity must be a positive whole number.');
      return;
    }

    setSubmitting(true);

    try {
      await inventoryApi.adjustStock(adjustProductId, {
        quantity,
        operation: adjustmentOperation,
        reason: adjustmentReason,
      });

      setMessage('Inventory adjusted successfully.');

      setAdjustmentQuantity('');
      setAdjustmentReason('');

      await loadData();
    } catch (requestError) {
      setError(
        requestError.message ||
          'Unable to adjust inventory.',
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleViewHistory(productId) {
    setError('');
    setMessage('');

    setHistoryProductId(productId);
    setShowHistory(true);
    setHistoryLoading(true);
    setHistory([]);

    try {
      const response =
        await inventoryApi.getTransactionHistory(productId);

      setHistory(
        Array.isArray(response) ? response : [],
      );
    } catch (requestError) {
      setError(
        requestError.message ||
          'Unable to load inventory history.',
      );
      setShowHistory(false);
    } finally {
      setHistoryLoading(false);
    }
  }

  function closeHistory() {
    setShowHistory(false);
    setHistory([]);
    setHistoryProductId('');
  }

  return (
    <main className="distributor-inventory">
      <header className="distributor-inventory__header">
        <p className="distributor-inventory__eyebrow">
          MYSHOPYCART PARTNER PORTAL
        </p>

        <h1>Inventory Operations</h1>

        <p>
          Manage your product inventory, adjust stock levels,
          and review inventory transactions.
        </p>
      </header>

      {error && <Alert variant="error">{error}</Alert>}
      {message && <Alert variant="success">{message}</Alert>}

      {loading ? (
        <div className="distributor-inventory__loading">
          <Spinner />
          <p>Loading inventory...</p>
        </div>
      ) : (
        <>
          <section
            className="distributor-inventory__section"
            aria-labelledby="inventory-list-title"
          >
            <h2 id="inventory-list-title">My Inventory</h2>

            {inventory.length === 0 ? (
              <p className="distributor-inventory__empty">
                No inventory records were returned for your account.
              </p>
            ) : (
              <div className="distributor-inventory__table-wrap">
                <table className="distributor-inventory__table">
                  <thead>
                    <tr>
                      <th>Product ID</th>
                      <th>Available Stock</th>
                      <th>Reserved Stock</th>
                      <th>Total Stock</th>
                      <th>Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {inventory.map((item) => {
                      const available = item.availableStock ?? 0;
                      const reserved = item.reservedStock ?? 0;
                      const total = available + reserved;

                      return (
                        <tr key={item.id ?? item.productId}>
                          <td>{item.productId}</td>
                          <td>{available}</td>
                          <td>{reserved}</td>
                          <td>{total}</td>
                          <td>
                            <button
                              type="button"
                              className="distributor-inventory__action"
                              onClick={() =>
                                handleViewHistory(item.productId)
                              }
                              disabled={historyLoading}
                            >
                              View History
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}

            <p className="distributor-inventory__note">
              Total Stock = Available Stock + Reserved Stock.
              Reserved Stock is managed automatically by the order
              reservation process and cannot be edited manually.
            </p>
          </section>

          <div className="distributor-inventory__forms">
            <section className="distributor-inventory__section">
              <h2>Initialize Inventory</h2>

              <p>
                Create an inventory record for a product
                associated with your account.
              </p>

              {productsWithoutInventory.length === 0 ? (
                <p className="distributor-inventory__empty">
                  No products without inventory were found.
                </p>
              ) : (
                <form onSubmit={handleCreateInventory}>
                  <label htmlFor="initial-product">
                    Product
                  </label>

                  <select
                    id="initial-product"
                    value={selectedProductId}
                    onChange={(event) =>
                      setSelectedProductId(event.target.value)
                    }
                    required
                  >
                    <option value="">Select a product</option>

                    {productsWithoutInventory.map((product) => (
                      <option key={product.id} value={product.id}>
                        {product.name || product.title || product.id}
                        {' — ID: '}
                        {product.id}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="initial-stock">
                    Initial Available Stock
                  </label>

                  <input
                    id="initial-stock"
                    type="number"
                    min="1"
                    step="1"
                    value={initialStock}
                    onChange={(event) =>
                      setInitialStock(event.target.value)
                    }
                    required
                  />

                  <button type="submit" disabled={submitting}>
                    {submitting
                      ? 'Please wait...'
                      : 'Initialize Inventory'}
                  </button>
                </form>
              )}
            </section>

            <section className="distributor-inventory__section">
              <h2>Add Stock</h2>

              <p>
                Increase available stock for an existing
                inventory record.
              </p>

              {inventory.length === 0 ? (
                <p className="distributor-inventory__empty">
                  Initialize inventory before adding stock.
                </p>
              ) : (
                <form onSubmit={handleAddStock}>
                  <label htmlFor="stock-product">
                    Inventory Product
                  </label>

                  <select
                    id="stock-product"
                    value={stockProductId}
                    onChange={(event) =>
                      setStockProductId(event.target.value)
                    }
                    required
                  >
                    <option value="">Select a product</option>

                    {inventory.map((item) => (
                      <option
                        key={item.id ?? item.productId}
                        value={item.productId}
                      >
                        Product {item.productId}
                        {' — Available: '}
                        {item.availableStock ?? 0}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="additional-stock">
                    Quantity to Add
                  </label>

                  <input
                    id="additional-stock"
                    type="number"
                    min="1"
                    step="1"
                    value={additionalStock}
                    onChange={(event) =>
                      setAdditionalStock(event.target.value)
                    }
                    required
                  />

                  <button type="submit" disabled={submitting}>
                    {submitting ? 'Please wait...' : 'Add Stock'}
                  </button>
                </form>
              )}
            </section>

            <section className="distributor-inventory__section">
              <h2>Adjust Stock</h2>

              <p>
                Manually increase or decrease available stock.
                A reason is required for every adjustment.
              </p>

              {inventory.length === 0 ? (
                <p className="distributor-inventory__empty">
                  Initialize inventory before adjusting stock.
                </p>
              ) : (
                <form onSubmit={handleAdjustStock}>
                  <label htmlFor="adjust-product">
                    Inventory Product
                  </label>

                  <select
                    id="adjust-product"
                    value={adjustProductId}
                    onChange={(event) =>
                      setAdjustProductId(event.target.value)
                    }
                    required
                  >
                    <option value="">Select a product</option>

                    {inventory.map((item) => (
                      <option
                        key={item.id ?? item.productId}
                        value={item.productId}
                      >
                        Product {item.productId}
                        {' — Available: '}
                        {item.availableStock ?? 0}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="adjust-operation">
                    Adjustment Type
                  </label>

                  <select
                    id="adjust-operation"
                    value={adjustmentOperation}
                    onChange={(event) =>
                      setAdjustmentOperation(event.target.value)
                    }
                    required
                  >
                    <option value="INCREASE">Increase Stock</option>
                    <option value="DECREASE">Decrease Stock</option>
                  </select>

                  <label htmlFor="adjust-quantity">
                    Quantity
                  </label>

                  <input
                    id="adjust-quantity"
                    type="number"
                    min="1"
                    step="1"
                    value={adjustmentQuantity}
                    onChange={(event) =>
                      setAdjustmentQuantity(event.target.value)
                    }
                    required
                  />

                  <label htmlFor="adjust-reason">
                    Adjustment Reason
                  </label>

                  <textarea
                    id="adjust-reason"
                    rows="3"
                    maxLength="500"
                    value={adjustmentReason}
                    onChange={(event) =>
                      setAdjustmentReason(event.target.value)
                    }
                    placeholder="Enter the reason for this adjustment"
                    required
                  />

                  <button type="submit" disabled={submitting}>
                    {submitting
                      ? 'Please wait...'
                      : 'Adjust Stock'}
                  </button>
                </form>
              )}
            </section>
          </div>

          {showHistory && (
            <section
              className="distributor-inventory__section distributor-inventory__history"
              aria-labelledby="inventory-history-title"
            >
              <div className="distributor-inventory__history-header">
                <div>
                  <h2 id="inventory-history-title">
                    Inventory History
                  </h2>

                  <p>
                    Product ID: {historyProductId}
                  </p>
                </div>

                <button
                  type="button"
                  className="distributor-inventory__close"
                  onClick={closeHistory}
                >
                  Close History
                </button>
              </div>

              {historyLoading ? (
                <div className="distributor-inventory__loading">
                  <Spinner />
                  <p>Loading transaction history...</p>
                </div>
              ) : history.length === 0 ? (
                <p className="distributor-inventory__empty">
                  No transaction history was found for this product.
                </p>
              ) : (
                <div className="distributor-inventory__table-wrap">
                  <table className="distributor-inventory__table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Transaction Type</th>
                        <th>Quantity</th>
                        <th>Previous Available</th>
                        <th>New Available</th>
                        <th>Reason</th>
                      </tr>
                    </thead>

                    <tbody>
                      {history.map((transaction) => (
                        <tr key={transaction.id}>
                          <td>
                            {transaction.createdAt
                              ? new Date(
                                  transaction.createdAt,
                                ).toLocaleString()
                              : '—'}
                          </td>

                          <td>{transaction.transactionType}</td>
                          <td>{transaction.quantity}</td>
                          <td>
                            {transaction.previousAvailableStock}
                          </td>
                          <td>
                            {transaction.newAvailableStock}
                          </td>
                          <td>
                            {transaction.reason || '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          )}

          <button
            type="button"
            className="distributor-inventory__refresh"
            onClick={loadData}
            disabled={loading || submitting}
          >
            Refresh Inventory
          </button>
        </>
      )}
    </main>
  );
}

export default DistributorInventoryPage;