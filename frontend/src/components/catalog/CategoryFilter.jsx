function CategoryFilter({
  categories = [],
  value = '',
  onChange,
}) {
  return (
    <div className="category-filter">
      <label
        className="category-filter__label"
        htmlFor="category-filter"
      >
        Filter by category
      </label>

      <select
        id="category-filter"
        className="category-filter__select"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        <option value="">All categories</option>

        {categories.map((category) => (
          <option
            key={category.id}
            value={category.id}
          >
            {category.name}
          </option>
        ))}
      </select>
    </div>
  );
}

export default CategoryFilter;