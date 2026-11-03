function SortSelect({
  value = '',
  onChange,
}) {
  return (
    <div className="sort-select">
      <label
        className="sort-select__label"
        htmlFor="product-sort"
      >
        Sort by
      </label>

      <select
        id="product-sort"
        className="sort-select__control"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        <option value="">
          Default
        </option>

        <option value="price,asc">
          Price: Low to High
        </option>

        <option value="price,desc">
          Price: High to Low
        </option>

        <option value="name,asc">
          Name: A to Z
        </option>

        <option value="name,desc">
          Name: Z to A
        </option>
      </select>
    </div>
  );
}

export default SortSelect;