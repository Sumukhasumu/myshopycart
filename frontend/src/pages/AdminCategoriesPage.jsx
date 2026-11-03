import { useCallback, useEffect, useState } from 'react';
import PageContainer from '../components/layout/PageContainer';
import categoryApi from '../api/categoryApi';
import './AdminCategoriesPage.css';

const EMPTY_FORM = {
  name: '',
  description: '',
  parentCategoryId: '',
};

function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadCategories = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const result = await categoryApi.getCategories();
      setCategories(Array.isArray(result) ? result : []);
    } catch (err) {
      setError(
        err?.message || 'Unable to load categories. Please try again.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timerId = setTimeout(() => {
      loadCategories();
    }, 0);

    return () => clearTimeout(timerId);
  }, [loadCategories]);

  function resetForm() {
    setForm(EMPTY_FORM);
    setEditingId(null);
    setError('');
    setMessage('');
  }

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function handleEdit(category) {
    setEditingId(category.id);
    setForm({
      name: category.name || '',
      description: category.description || '',
      parentCategoryId: category.parentCategoryId || '',
    });
    setError('');
    setMessage('');
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    const name = form.name.trim();
    const description = form.description.trim();

    if (!name) {
      setError('Category name is required.');
      return;
    }

    const payload = {
      name,
      description,
      parentCategoryId: form.parentCategoryId.trim() || null,
    };

    setSaving(true);

    try {
      if (editingId) {
        await categoryApi.updateCategory(editingId, payload);
        setMessage('Category updated successfully.');
      } else {
        await categoryApi.createCategory(payload);
        setMessage('Category created successfully.');
      }

      setForm(EMPTY_FORM);
      setEditingId(null);
      await loadCategories();
    } catch (err) {
      setError(
        err?.message || 'Unable to save the category. Please try again.',
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(category) {
    const confirmed = window.confirm(
      `Delete the category "${category.name}"? This operation cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(category.id);
    setError('');
    setMessage('');

    try {
      await categoryApi.deleteCategory(category.id);

      if (editingId === category.id) {
        setForm(EMPTY_FORM);
        setEditingId(null);
      }

      setMessage('Category deleted successfully.');
      await loadCategories();
    } catch (err) {
      setError(
        err?.message ||
          'Unable to delete the category. It may still be in use.',
      );
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <PageContainer>
      <section className="admin-categories">
        <header className="admin-categories__header">
          <p className="admin-categories__eyebrow">
            MYSHOPYCART ADMINISTRATION
          </p>
          <h1>Category Management</h1>
          <p>
            Create and maintain the categories used to organize
            products in the catalogue.
          </p>
        </header>

        {error && (
          <div
            className="admin-categories__alert admin-categories__alert--error"
            role="alert"
          >
            {error}
          </div>
        )}

        {message && (
          <div
            className="admin-categories__alert admin-categories__alert--success"
            role="status"
          >
            {message}
          </div>
        )}

        <section className="admin-categories__panel">
          <h2>{editingId ? 'Edit Category' : 'Add Category'}</h2>

          <form onSubmit={handleSubmit} className="admin-categories__form">
            <div className="admin-categories__field">
              <label htmlFor="category-name">Category Name *</label>
              <input
                id="category-name"
                name="name"
                value={form.name}
                onChange={handleChange}
                maxLength={150}
                required
                disabled={saving}
                placeholder="Enter category name"
              />
            </div>

            <div className="admin-categories__field">
              <label htmlFor="category-description">Description</label>
              <textarea
                id="category-description"
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={3}
                maxLength={1000}
                disabled={saving}
                placeholder="Enter an optional description"
              />
            </div>

            <div className="admin-categories__field">
              <label htmlFor="category-parent">
                Parent Category ID (optional)
              </label>
              <input
                id="category-parent"
                name="parentCategoryId"
                value={form.parentCategoryId}
                onChange={handleChange}
                disabled={saving}
                placeholder="Enter parent category ID, if applicable"
              />
            </div>

            <div className="admin-categories__actions">
              <button type="submit" disabled={saving}>
                {saving
                  ? 'Saving...'
                  : editingId
                    ? 'Update Category'
                    : 'Create Category'}
              </button>

              {editingId && (
                <button
                  type="button"
                  className="admin-categories__button--secondary"
                  onClick={resetForm}
                  disabled={saving}
                >
                  Cancel Edit
                </button>
              )}
            </div>
          </form>
        </section>

        <section className="admin-categories__panel">
          <h2>Existing Categories</h2>

          {loading ? (
            <p role="status">Loading categories...</p>
          ) : categories.length === 0 ? (
            <p>No categories found.</p>
          ) : (
            <div className="admin-categories__table-wrapper">
              <table className="admin-categories__table">
                <thead>
                  <tr>
                    <th scope="col">Name</th>
                    <th scope="col">Description</th>
                    <th scope="col">Parent Category</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {categories.map((category) => (
                    <tr key={category.id}>
                      <td>{category.name}</td>
                      <td>{category.description || '—'}</td>
                      <td>{category.parentCategoryId || '—'}</td>
                      <td>
                        <div className="admin-categories__row-actions">
                          <button
                            type="button"
                            onClick={() => handleEdit(category)}
                            disabled={saving || deletingId !== null}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="admin-categories__button--danger"
                            onClick={() => handleDelete(category)}
                            disabled={saving || deletingId !== null}
                          >
                            {deletingId === category.id
                              ? 'Deleting...'
                              : 'Delete'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </PageContainer>
  );
}

export default AdminCategoriesPage;