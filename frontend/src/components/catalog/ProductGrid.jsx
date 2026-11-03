import ProductCard from './ProductCard';

function ProductGrid({
  products = [],
  wishlistProductIds = new Set(),
  onWishlistChange,
}) {
  return (
    <div className="product-grid">
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          isInWishlist={wishlistProductIds.has(
            product.id,
          )}
          onWishlistChange={onWishlistChange}
        />
      ))}
    </div>
  );
}

export default ProductGrid;