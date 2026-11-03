import { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import PageContainer from '../components/layout/PageContainer';
import Spinner from '../components/ui/Spinner';
import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import Pagination from '../components/ui/Pagination';
import ProductGrid from '../components/catalog/ProductGrid';
import categoryApi from '../api/categoryApi';
import productApi from '../api/productApi';
import './CategoryPage.css';
import '../components/catalog/ProductCard.css';
import '../components/catalog/ProductGrid.css';
import '../components/ui/Pagination.css';

const PAGE_SIZE = 2;

function CategoryPage() {
  const { categoryId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();

  const pageParam = Number.parseInt(
    searchParams.get('page') || '0',
    10,
  );

  const currentPage = Number.isNaN(pageParam) || pageParam < 0
    ? 0
    : pageParam;

  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;

    async function loadCategory() {
      setLoading(true);
      setError(null);

      try {
        const [categoryResponse, productResponse] =
          await Promise.all([
            categoryApi.getCategoryById(categoryId),
            productApi.getProductsByCategory(
              categoryId,
              {
                page: currentPage,
                size: PAGE_SIZE,
              },
            ),
          ]);

        if (!isMounted) {
          return;
        }

        setCategory(categoryResponse);
        setProducts(productResponse?.content || []);
        setPageData(productResponse || null);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        setProducts([]);
        setPageData(null);

        setError(
          requestError?.data?.message ||
            requestError?.message ||
            'Unable to load this category.',
        );
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    if (categoryId) {
      loadCategory();
    }

    return () => {
      isMounted = false;
    };
  }, [categoryId, currentPage]);

  function handlePageChange(nextPage) {
    const nextParams = new URLSearchParams(searchParams);

    if (nextPage === 0) {
      nextParams.delete('page');
    } else {
      nextParams.set('page', String(nextPage));
    }

    setSearchParams(nextParams);
  }

  return (
    <PageContainer>
      <section className="category-page">
        {loading && (
          <div className="category-page__status">
            <Spinner />
            <p>Loading category...</p>
          </div>
        )}

        {!loading && error && (
          <div className="category-page__status">
            <Alert variant="error">
              {error}
            </Alert>
          </div>
        )}

        {!loading && !error && category && (
          <>
            <header className="category-page__header">
              <p className="category-page__eyebrow">
                CATEGORY
              </p>

              <h1 className="category-page__title">
                {category.name}
              </h1>

              {category.description && (
                <p className="category-page__description">
                  {category.description}
                </p>
              )}
            </header>

            {products.length === 0 ? (
              <EmptyState
                title="No products in this category"
                description="There are currently no products available in this category."
              />
            ) : (
              <>
                <ProductGrid products={products} />

                <Pagination
                  page={pageData?.number ?? currentPage}
                  totalPages={pageData?.totalPages ?? 0}
                  onPageChange={handlePageChange}
                  disabled={loading}
                />
              </>
            )}
          </>
        )}
      </section>
    </PageContainer>
  );
}

export default CategoryPage;