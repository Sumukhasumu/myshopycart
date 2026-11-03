import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

import PageContainer from '../components/layout/PageContainer';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import Pagination from '../components/ui/Pagination';
import ProductGrid from '../components/catalog/ProductGrid';
import CategoryFilter from '../components/catalog/CategoryFilter';
import SortSelect from '../components/catalog/SortSelect';

import productApi from '../api/productApi';
import categoryApi from '../api/categoryApi';
import wishlistApi from '../api/wishlistApi';

import './ProductsPage.css';
import '../components/catalog/ProductCard.css';
import '../components/catalog/ProductGrid.css';
import '../components/catalog/CategoryFilter.css';
import '../components/catalog/SortSelect.css';
import '../components/ui/Pagination.css';

const PAGE_SIZE = 2;

function ProductsPage() {
  const [searchParams, setSearchParams] =
    useSearchParams();

  const searchKeyword =
    searchParams.get('search')?.trim() || '';

  const selectedCategoryId =
    searchParams.get('category')?.trim() || '';

  const selectedSort =
    searchParams.get('sort')?.trim() || '';

  const pageParam = Number.parseInt(
    searchParams.get('page') || '0',
    10,
  );

  const currentPage =
    Number.isNaN(pageParam) || pageParam < 0
      ? 0
      : pageParam;

  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] =
    useState(true);
  const [categoriesError, setCategoriesError] =
    useState(null);

  const [products, setProducts] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [wishlistProductIds, setWishlistProductIds] =
    useState(new Set());

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      setCategoriesLoading(true);
      setCategoriesError(null);

      try {
        const response =
          await categoryApi.getCategories();

        if (!isMounted) {
          return;
        }

        setCategories(response || []);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setCategoriesError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load categories.',
        );
      } finally {
        if (isMounted) {
          setCategoriesLoading(false);
        }
      }
    }

    loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function loadWishlist() {
      try {
        const response =
          await wishlistApi.getWishlist();

        if (!isMounted) {
          return;
        }

        const productIds = new Set(
          (response?.items || [])
            .map((item) => item.productId)
            .filter(Boolean),
        );

        setWishlistProductIds(productIds);
      } catch {
        if (isMounted) {
          setWishlistProductIds(new Set());
        }
      }
    }

    loadWishlist();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function loadProducts() {
      setLoading(true);
      setError(null);

      try {
        let response;

        const productOptions = {
          page: currentPage,
          size: PAGE_SIZE,
          sort: selectedSort || undefined,
        };

        if (selectedCategoryId) {
          response =
            await productApi.getProductsByCategory(
              selectedCategoryId,
              productOptions,
            );
        } else if (searchKeyword) {
          response = await productApi.searchProducts({
            keyword: searchKeyword,
            ...productOptions,
          });
        } else {
          response =
            await productApi.getProducts(
              productOptions,
            );
        }

        if (!isMounted) {
          return;
        }

        setProducts(response?.content || []);
        setPageData(response || null);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setProducts([]);
        setPageData(null);

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load products.',
        );
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    loadProducts();

    return () => {
      isMounted = false;
    };
  }, [
    searchKeyword,
    selectedCategoryId,
    selectedSort,
    currentPage,
  ]);

  function handleWishlistChange(
    productId,
    isInWishlist,
  ) {
    setWishlistProductIds((currentIds) => {
      const nextIds = new Set(currentIds);

      if (isInWishlist) {
        nextIds.add(productId);
      } else {
        nextIds.delete(productId);
      }

      return nextIds;
    });
  }

  function handleCategoryChange(categoryId) {
    const nextParams = new URLSearchParams(
      searchParams,
    );

    nextParams.delete('page');
    nextParams.delete('search');

    if (categoryId) {
      nextParams.set('category', categoryId);
    } else {
      nextParams.delete('category');
    }

    setSearchParams(nextParams);
  }

  function handleSortChange(sort) {
    const nextParams = new URLSearchParams(
      searchParams,
    );

    nextParams.delete('page');

    if (sort) {
      nextParams.set('sort', sort);
    } else {
      nextParams.delete('sort');
    }

    setSearchParams(nextParams);
  }

  function handlePageChange(nextPage) {
    const nextParams = new URLSearchParams(
      searchParams,
    );

    if (nextPage === 0) {
      nextParams.delete('page');
    } else {
      nextParams.set('page', String(nextPage));
    }

    setSearchParams(nextParams);
  }

  const selectedCategory = categories.find(
    (category) => category.id === selectedCategoryId,
  );

  const pageTitle = selectedCategory
    ? `${selectedCategory.name} Products`
    : searchKeyword
      ? `Search results for "${searchKeyword}"`
      : 'Products';

  const pageDescription = selectedCategory
    ? `Explore products available in ${selectedCategory.name}.`
    : searchKeyword
      ? 'Products matching your search.'
      : 'Explore our latest products and find something that fits your needs.';

  return (
    <PageContainer>
      <section className="products-page">
        <header className="products-page__header">
          <div>
            <p className="products-page__eyebrow">
              MYSHOPYCART
            </p>

            <h1 className="products-page__title">
              {pageTitle}
            </h1>

            <p className="products-page__description">
              {pageDescription}
            </p>
          </div>
        </header>

        <div className="products-page__controls">
          {!categoriesLoading && (
            <CategoryFilter
              categories={categories}
              value={selectedCategoryId}
              onChange={handleCategoryChange}
            />
          )}

          <SortSelect
            value={selectedSort}
            onChange={handleSortChange}
          />
        </div>

        {!categoriesLoading && categoriesError && (
          <Alert variant="error">
            {categoriesError}
          </Alert>
        )}

        {loading && (
          <div className="products-page__status">
            <Spinner />
            <p>
              {selectedCategory
                ? 'Loading category products...'
                : searchKeyword
                  ? 'Searching products...'
                  : 'Loading products...'}
            </p>
          </div>
        )}

        {!loading && error && (
          <div className="products-page__status">
            <Alert variant="error">
              {error}
            </Alert>
          </div>
        )}

        {!loading &&
          !error &&
          products.length === 0 && (
            <EmptyState
              title={
                selectedCategory
                  ? 'No products in this category'
                  : searchKeyword
                    ? 'No products found'
                    : 'No products available'
              }
              description={
                selectedCategory
                  ? `There are currently no products available in ${selectedCategory.name}.`
                  : searchKeyword
                    ? `No products matched "${searchKeyword}". Try a different search term.`
                    : 'There are currently no products available.'
              }
            />
          )}

        {!loading &&
          !error &&
          products.length > 0 && (
            <>
              <ProductGrid
                products={products}
                wishlistProductIds={
                  wishlistProductIds
                }
                onWishlistChange={
                  handleWishlistChange
                }
              />

              <Pagination
                page={
                  pageData?.number ??
                  currentPage
                }
                totalPages={
                  pageData?.totalPages ?? 0
                }
                onPageChange={handlePageChange}
                disabled={loading}
              />
            </>
          )}
      </section>
    </PageContainer>
  );
}

export default ProductsPage;