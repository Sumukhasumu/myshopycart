import { useId } from 'react';

function SearchBox({
  value = '',
  onChange,
  placeholder = 'Search products...',
  onSubmit,
  className = '',
}) {
  const generatedId = useId();

  const inputId = `ui-search-input-${generatedId}`;

  const handleSubmit = (event) => {
    event.preventDefault();

    if (onSubmit) {
      onSubmit(value);
    }
  };

  return (
    <form
      className={`ui-search-box ${className}`.trim()}
      onSubmit={handleSubmit}
      role="search"
    >
      <label
        className="ui-search-box-label"
        htmlFor={inputId}
      >
        Search
      </label>

      <input
        id={inputId}
        className="ui-search-box-input"
        type="search"
        value={value}
        onChange={onChange}
        placeholder={placeholder}
      />

      <button
        type="submit"
        className="ui-search-box-button"
        aria-label="Search"
      >
        <svg
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <circle
            cx="11"
            cy="11"
            r="6.5"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
          />

          <path
            d="m16 16 5 5"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
        </svg>
      </button>
    </form>
  );
}

export default SearchBox;