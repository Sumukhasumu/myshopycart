function Pagination({
  page = 0,
  totalPages = 0,
  onPageChange,
  disabled = false,
}) {
  if (totalPages <= 1) {
    return null;
  }

  const isFirstPage = page === 0;
  const isLastPage = page >= totalPages - 1;

  function handlePrevious() {
    if (!isFirstPage && !disabled) {
      onPageChange(page - 1);
    }
  }

  function handleNext() {
    if (!isLastPage && !disabled) {
      onPageChange(page + 1);
    }
  }

  return (
    <nav
      className="pagination"
      aria-label="Product pagination"
    >
      <button
        type="button"
        className="pagination__button"
        onClick={handlePrevious}
        disabled={disabled || isFirstPage}
        aria-label="Go to previous page"
      >
        Previous
      </button>

      <span
        className="pagination__status"
        aria-current="page"
      >
        Page {page + 1} of {totalPages}
      </span>

      <button
        type="button"
        className="pagination__button"
        onClick={handleNext}
        disabled={disabled || isLastPage}
        aria-label="Go to next page"
      >
        Next
      </button>
    </nav>
  );
}

export default Pagination;