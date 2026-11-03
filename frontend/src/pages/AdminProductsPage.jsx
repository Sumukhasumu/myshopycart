import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import productApi from '../api/productApi';
import categoryApi from '../api/categoryApi';
import Alert from '../components/ui/Alert';
import Spinner from '../components/ui/Spinner';
import './AdminProductsPage.css';

const EMPTY_FORM = {
  name: '',
  description: '',
  price: '',
  categoryId: '',
  distributorId: '',
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

function AdminProductsPage() {
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
  const [editingId, setEditingId] = useState('');
  const [deletingId, setDeletingId] = useState('');
  const [retryCount, setRetryCount] = useState(0);
  const [form, setForm] = useState(EMPTY_FORM);

  useEffect(() => {
    let cancelled = false;

    productApi.getProducts({ page, size: 12 })
      .then((response) => {
        if (cancelled) return;

        const result = response?.data ?? response;
        const content = Array.isArray(result)
          ? result
          : result?.content;

        if (!Array.isArray(content)) {
          throw new Error(
            'The product service returned an unexpected response.',
          );
        }

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
      })
      .catch((requestError) => {
        if (cancelled) return;

        setProducts([]);
        setTotalPages(0);
        setTotalElements(0);
        setError(getErrorMessage(
          requestError,
          'Unable to load products. Please try again.',
        ));
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [page, retryCount]);

  useEffect(() => {
    let cancelled = false;

    categoryApi.getCategories()
      .then((response) => {
        if (cancelled) return;

        const result = response?.data ?? response;
        const items = Array.isArray(result)
          ? result
          : result?.content;

        if (!Array.isArray(items)) {
          throw new Error(
            'The category service returned an unexpected response.',
          );
        }

        setCategories(items);
        setCategoryError('');
      })
      .catch((requestError) => {
        if (cancelled) return;

        setCategories([]);
        setCategoryError(getErrorMessage(
          requestError,
          'Unable to load categories.',
        ));
      })
      .finally(() => {
        if (!cancelled) {
          setCategoriesLoading(false);
        }
      });

    return () => {
      cancelled = true;
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
      price: product.price == null ? '' : String(product.price),
      categoryId: product.categoryId || '',
      distributorId: product.distributorId || '',
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
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError('');
    setSuccess('');

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

    if (!editingId && !form.distributorId.trim()) {
      setFormError(
        'Distributor ID is required when creating a product. ' +
        'Use the ID of an existing distributor account.',
      );
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
      setFormError('Attributes must contain valid JSON object data.');
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

    if (!editingId) {
      payload.distributorId = form.distributorId.trim();
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
      setLoading(true);
      setRetryCount((count) => count + 1);
    } catch (requestError) {
      setFormError(getErrorMessage(
        requestError,
        'Unable to save the product.',
      ));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(product) {
    const confirmed = window.confirm(
      `Delete "${product.name || 'this product'}"? This action cannot be undone.`,
    );

    if (!confirmed) return;

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
    } catch (requestError) {
      setError(getErrorMessage(
        requestError,
        'Unable to delete the product.',
      ));
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
    <main className="admin-products">
      <header className="admin-products__header">
        <div>
          <p className="admin-products__eyebrow">
            MYSHOPYCART ADMINISTRATION
          </p>
          <h1>Product Management</h1>
          <p>
            View, create, update, and remove catalogue products.
          </p>
        </div>

        <Link
          className="admin-products__link"
          to="/admin/dashboard"
        >
          Admin Dashboard
        </Link>
      </header>

      <section className="admin-products__form-section">
        <h2>{editingId ? 'Edit Product' : 'Create Product'}</h2>

        <p className="admin-products__hint">
          Product ownership is managed by the backend. When creating
          a product, enter the ID of an existing distributor account.
          Editing does not change the existing distributor ownership.
        </p>

        {formError && <Alert>{formError}</Alert>}

        {success && (
          <p className="admin-products__success" role="status">
            {success}
          </p>
        )}

        {categoryError && <Alert>{categoryError}</Alert>}

        <form
          className="admin-products__form"
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

          <div className="admin-products__form-row">
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

          {!editingId && (
            <label>
              Existing distributor ID
              <input
                name="distributorId"
                value={form.distributorId}
                onChange={updateField}
                required
                placeholder="ID of an existing distributor"
              />
            </label>
          )}

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

          <div className="admin-products__form-actions">
            <button type="submit" disabled={saving}>
              {saving
                ? 'Saving...'
                : editingId
                  ? 'Save Changes'
                  : 'Create Product'}
            </button>

            {editingId && (
              <button
                type="button"
                className="admin-products__secondary-button"
                onClick={resetForm}
                disabled={saving}
              >
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="admin-products__list-section">
        <div className="admin-products__list-heading">
          <h2>All Products</h2>

          {!loading && !error && (
            <p>
              {totalElements}{' '}
              {totalElements === 1 ? 'product' : 'products'}
            </p>
          )}
        </div>

        {loading && (
          <div className="admin-products__feedback" role="status">
            <Spinner />
            <p>Loading products...</p>
          </div>
        )}

        {!loading && error && (
          <div className="admin-products__error">
            <Alert>{error}</Alert>
            <button type="button" onClick={retryProducts}>
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && products.length === 0 && (
          <div className="admin-products__empty">
            <h3>No products found</h3>
            <p>The catalogue does not contain any products yet.</p>
          </div>
        )}

        {!loading && !error && products.length > 0 && (
          <>
            <div className="admin-products__grid">
              {products.map((product) => (
                <article
                  className="admin-products__card"
                  key={product.id}
                >
                  {product.imageUrls?.[0] ? (
                    <img
                      className="admin-products__image"
                      src={product.imageUrls[0]}
                      alt={product.name || 'Product'}
                      loading="lazy"
                    />
                  ) : (
                    <div className="admin-products__image-placeholder">
                      No image
                    </div>
                  )}

                  <div className="admin-products__card-body">
                    <h3>{product.name || 'Unnamed product'}</h3>

                    <p className="admin-products__price">
                      {formatPrice(product.price)}
                    </p>

                    <p className="admin-products__meta">
                      Category: {product.categoryId || 'Not assigned'}
                    </p>

                    <p className="admin-products__meta">
                      Distributor ID:{' '}
                      {product.distributorId || 'Not assigned'}
                    </p>

                    {product.description && (
                      <p className="admin-products__description">
                        {product.description}
                      </p>
                    )}

                    <div className="admin-products__card-actions">
                      <button
                        type="button"
                        onClick={() => startEditing(product)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        className="admin-products__delete-button"
                        onClick={() => handleDelete(product)}
                        disabled={deletingId === product.id}
                      >
                        {deletingId === product.id
                          ? 'Deleting...'
                          : 'Delete'}
                      </button>

                      <Link
                        to={`/products/${encodeURIComponent(product.id)}`}
                      >
                        View
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            <nav
              className="admin-products__pagination"
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

export default AdminProductsPage;