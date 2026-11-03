import { useSearchParams, useNavigate } from 'react-router-dom';

function HeaderSearch() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const currentSearch = searchParams.get('search') || '';

  function handleSubmit(event) {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);
    const searchTerm = formData.get('search')?.trim() || '';

    if (!searchTerm) {
      navigate('/products');
      return;
    }

    navigate(
      `/products?search=${encodeURIComponent(searchTerm)}`,
    );
  }

  return (
    <form
      className="header-search"
      role="search"
      onSubmit={handleSubmit}
    >
      <label
        className="header-search-label"
        htmlFor="header-search-input"
      >
        Search products
      </label>

      <input
        id="header-search-input"
        className="header-search-input"
        type="search"
        name="search"
        placeholder="Search products..."
        autoComplete="off"
        defaultValue={currentSearch}
        key={currentSearch}
        aria-label="Search products"
      />
    </form>
  );
}

export default HeaderSearch;