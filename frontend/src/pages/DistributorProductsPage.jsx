
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import productApi from '../api/productApi';
import categoryApi from '../api/categoryApi';
import useAuth from '../auth/useAuth';
import Alert from '../components/ui/Alert';
import Spinner from '../components/ui/Spinner';
import './DistributorProductsPage.css';

const EMPTY_FORM = {
  name: '',
  description: '',
  price: '',
  categoryId: '',
  attributesJson: '{}',
  imageUrlsText: '',
};

function formatPrice(price) {
  const amount = Number(price);

  if (!Number.isFinite(amount)) {
    return 'Price unavailable';
  }

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount);
}

function getErrorMessage(error, fallback) {
  return (
    error?.response?.data?.message ||
    error?.data?.message ||
    error?.message ||
    fallback
  );
}

function DistributorProductsPage() {
  const { user } = useAuth();

  const distributorId = user?.id;

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  const [error, setError] = useState('');
  const [categoryError, setCategoryError] = useState('');
  const [formError, setFormError] = useState('');
  const [success, setSuccess] = useState('');

  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState('');
  const [editingId, setEditingId] = useState('');

  const [retryCount, setRetryCount] = useState(0);
  const [form, setForm] = useState(EMPTY_FORM);

  // Load products belonging to the authenticated distributor.
  useEffect(() => {
    let active = true;

    async function fetchProducts() {
      try {
        const response = await productApi.getMyProducts({
          page,
          size: 12,
        });

        const result = response?.data ?? response;
        const content = Array.isArray(result)
          ? result
          : result?.content;

        if (!Array.isArray(content)) {
          throw new Error(
            'The product service returned an unexpected response.',
          );
        }

        if (active) {
          setProducts(content);
          setTotalPages(
            Number.isFinite(result?.totalPages)
              ? result.totalPages
              : 0,
          );
          setTotalElements(
            Number.isFinite(result?.totalElements)
              ? result.totalElements
              : content.length,
          );
          setError('');
        }
      } catch (requestError) {
        if (active) {
          setProducts([]);
          setTotalPages(0);
          setTotalElements(0);
          setError(
            getErrorMessage(
              requestError,
              'Unable to load your products. Please try again.',
            ),
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    fetchProducts();

    return () => {
      active = false;
    };
  }, [page, retryCount]);

  // Load categories for the product form.
  useEffect(() => {
    let active = true;

    async function fetchCategories() {
      try {
        const response = await categoryApi.getCategories();
        const result = response?.data ?? response;

        const items = Array.isArray(result)
          ? result
          : result?.content;

        if (!Array.isArray(items)) {
          throw new Error(
            'The category service returned an unexpected response.',
          );
        }

        if (active) {
          setCategories(items);
          setCategoryError('');
        }
      } catch (requestError) {
        if (active) {
          setCategories([]);
          setCategoryError(
            getErrorMessage(
              requestError,
              'Unable to load categories.',
            ),
          );
        }
      } finally {
        if (active) {
          setCategoriesLoading(false);
        }
      }
    }

    fetchCategories();

    return () => {
      active = false;
    };
  }, []);

  function updateField(event) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));

    setFormError('');
    setSuccess('');
  }

  function resetForm() {
    setForm(EMPTY_FORM);
    setEditingId('');
    setFormError('');
  }

  function startEditing(product) {
    setEditingId(product.id);

    setForm({
      name: product.name || '',
      description: product.description || '',
      price: product.price == null
        ? ''
        : String(product.price),
      categoryId: product.categoryId || '',
      attributesJson: JSON.stringify(
        product.attributes || {},
        null,
        2,
      ),
      imageUrlsText: Array.isArray(product.imageUrls)
        ? product.imageUrls.join('\n')
        : '',
    });

    setFormError('');
    setSuccess('');

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFormError('');
    setSuccess('');

    if (!distributorId) {
      setFormError(
        'Your distributor account ID is unavailable. Please log in again.',
      );
      return;
    }

    if (!form.name.trim()) {
      setFormError('Product name is required.');
      return;
    }

    if (!form.description.trim()) {
      setFormError('Product description is required.');
      return;
    }

    const price = Number(form.price);

    if (!Number.isFinite(price) || price < 0.01) {
      setFormError('Enter a valid price greater than 0.');
      return;
    }

    if (!form.categoryId.trim()) {
      setFormError('Category is required.');
      return;
    }

    let attributes;

    try {
      attributes = JSON.parse(form.attributesJson || '{}');

      if (
        attributes === null ||
        Array.isArray(attributes) ||
        typeof attributes !== 'object'
      ) {
        throw new Error('Attributes must be a JSON object.');
      }
    } catch {
      setFormError(
        'Attributes must contain valid JSON object data.',
      );
      return;
    }

    const imageUrls = form.imageUrlsText
      .split(/\r?\n/)
      .map((url) => url.trim())
      .filter(Boolean);

    const payload = {
      name: form.name.trim(),
      description: form.description.trim(),
      price,
      categoryId: form.categoryId.trim(),
      attributes,
      imageUrls,
    };

    // Ownership is assigned from the authenticated session.
    // It is never taken from the editable product form.
    if (!editingId) {
      payload.distributorId = String(distributorId);
    }

    setSaving(true);

    try {
      if (editingId) {
        await productApi.updateProduct(editingId, payload);
        setSuccess('Product updated successfully.');
      } else {
        await productApi.createProduct(payload);
        setSuccess('Product created successfully.');
      }

      resetForm();
      setPage(0);
      setLoading(true);
      setRetryCount((count) => count + 1);
    } catch (requestError) {
      setFormError(
        getErrorMessage(
          requestError,
          'Unable to save the product.',
        ),
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(product) {
    const confirmed = window.confirm(
      `Delete "${product.name || 'this product'}"? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(product.id);
    setError('');
    setSuccess('');

    try {
      await productApi.deleteProduct(product.id);

      setSuccess('Product deleted successfully.');

      if (products.length === 1 && page > 0) {
        setPage((current) => current - 1);
        setLoading(true);
      } else {
        setLoading(true);
        setRetryCount((count) => count + 1);
      }

      if (editingId === product.id) {
        resetForm();
      }
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Unable to delete the product.',
        ),
      );
    } finally {
      setDeletingId('');
    }
  }

  function retryProducts() {
    setLoading(true);
    setRetryCount((count) => count + 1);
  }

  function changePage(nextPage) {
    setLoading(true);
    setPage(nextPage);
  }

  return (
    <main className="distributor-products">
      <header className="distributor-products__header">
        <div>
          <p className="distributor-products__eyebrow">
            DISTRIBUTOR PORTAL
          </p>

          <h1>Product Management</h1>

          <p>
            Add, view, update and manage products associated
            with your distributor account.
          </p>
        </div>

        <Link
          className="distributor-products__back"
          to="/distributor/dashboard"
        >
          Back to Dashboard
        </Link>
      </header>

      {/* Add and Edit Product Form */}
      <section className="distributor-products__form-section">
        <h2>{editingId ? 'Edit Product' : 'Add New Product'}</h2>

        <p className="distributor-products__form-hint">
          Products created here will be associated with your
          distributor account.
        </p>

        {formError && <Alert>{formError}</Alert>}

        {success && (
          <p className="distributor-products__success" role="status">
            {success}
          </p>
        )}

        {categoryError && <Alert>{categoryError}</Alert>}

        <form
          className="distributor-products__form"
          onSubmit={handleSubmit}
        >
          <label>
            Product name
            <input
              name="name"
              value={form.name}
              onChange={updateField}
              required
              maxLength={200}
            />
          </label>

          <label>
            Description
            <textarea
              name="description"
              value={form.description}
              onChange={updateField}
              required
              rows={3}
            />
          </label>

          <div className="distributor-products__form-row">
            <label>
              Price (INR)
              <input
                name="price"
                type="number"
                min="0.01"
                step="0.01"
                value={form.price}
                onChange={updateField}
                required
              />
            </label>

            <label>
              Category
              <select
                name="categoryId"
                value={form.categoryId}
                onChange={updateField}
                required
                disabled={
                  categoriesLoading || categories.length === 0
                }
              >
                <option value="">
                  {categoriesLoading
                    ? 'Loading categories...'
                    : 'Select a category'}
                </option>

                {categories.map((category) => (
                  <option
                    key={category.id}
                    value={category.id}
                  >
                    {category.name || category.id}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <label>
            Attributes (JSON object)
            <textarea
              name="attributesJson"
              value={form.attributesJson}
              onChange={updateField}
              rows={4}
              spellCheck="false"
            />
          </label>

          <label>
            Image URLs (one URL per line)
            <textarea
              name="imageUrlsText"
              value={form.imageUrlsText}
              onChange={updateField}
              rows={3}
              placeholder="https://example.com/image.jpg"
            />
          </label>

          <div className="distributor-products__form-actions">
            <button
              type="submit"
              disabled={saving || categoriesLoading || categories.length === 0}
            >
              {saving
                ? 'Saving...'
                : editingId
                  ? 'Save Changes'
                  : 'Add Product'}
            </button>

            {editingId && (
              <button
                type="button"
                className="distributor-products__cancel-button"
                onClick={resetForm}
                disabled={saving}
              >
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      </section>

      {/* Distributor Product Listing */}
      <section
        className="distributor-products__content"
        aria-labelledby="distributor-products-title"
      >
        <div className="distributor-products__summary">
          <h2 id="distributor-products-title">My Products</h2>

          {!loading && !error && (
            <p>
              {totalElements}{' '}
              {totalElements === 1 ? 'product' : 'products'}
            </p>
          )}
        </div>

        {loading && (
          <div
            className="distributor-products__feedback"
            role="status"
          >
            <Spinner />
            <p>Loading your products...</p>
          </div>
        )}

        {!loading && error && (
          <div className="distributor-products__error">
            <Alert>{error}</Alert>

            <button
              type="button"
              className="distributor-products__button"
              onClick={retryProducts}
            >
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && products.length === 0 && (
          <div className="distributor-products__empty">
            <h3>No products found</h3>
            <p>
              You haven't added any products yet.
              Use the form above to add your first product.
            </p>
          </div>
        )}

        {!loading && !error && products.length > 0 && (
          <>
            <div className="distributor-products__grid">
              {products.map((product) => (
                <article
                  className="distributor-products__card"
                  key={product.id}
                >
                  {product.imageUrls?.[0] ? (
                    <img
                      className="distributor-products__image"
                      src={product.imageUrls[0]}
                      alt={product.name || 'Product'}
                      loading="lazy"
                    />
                  ) : (
                    <div
                      className="distributor-products__image-placeholder"
                      aria-label="No product image"
                    >
                      No image
                    </div>
                  )}

                  <div className="distributor-products__card-body">
                    <h3>{product.name || 'Unnamed product'}</h3>

                    <p className="distributor-products__price">
                      {formatPrice(product.price)}
                    </p>

                    {product.categoryId && (
                      <p className="distributor-products__meta">
                        Category: {product.categoryId}
                      </p>
                    )}

                    {product.description && (
                      <p className="distributor-products__description">
                        {product.description}
                      </p>
                    )}

                    <div className="distributor-products__card-actions">
                      <Link
                        to={`/products/${encodeURIComponent(product.id)}`}
                      >
                        View
                      </Link>

                      <button
                        type="button"
                        onClick={() => startEditing(product)}
                        disabled={saving || Boolean(deletingId)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        className="distributor-products__delete-button"
                        onClick={() => handleDelete(product)}
                        disabled={
                          Boolean(deletingId) || saving
                        }
                      >
                        {deletingId === product.id
                          ? 'Deleting...'
                          : 'Delete'}
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            <nav
              className="distributor-products__pagination"
              aria-label="Product pages"
            >
              <button
                type="button"
                disabled={page <= 0 || loading}
                onClick={() => changePage(page - 1)}
              >
                Previous
              </button>

              <span>
                Page {page + 1}
                {totalPages > 0 ? ` of ${totalPages}` : ''}
              </span>

              <button
                type="button"
                disabled={
                  loading ||
                  totalPages === 0 ||
                  page + 1 >= totalPages
                }
                onClick={() => changePage(page + 1)}
              >
                Next
              </button>
            </nav>
          </>
        )}
      </section>
    </main>
  );
}

export default DistributorProductsPage;
