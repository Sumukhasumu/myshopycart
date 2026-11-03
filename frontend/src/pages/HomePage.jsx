
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import categoryApi from '../api/categoryApi';
import PageBackground from '../components/background/PageBackground';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';

import './home.css';

function HomePage() {
  const [firstCategoryId, setFirstCategoryId] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      try {
        const categories = await categoryApi.getCategories();

        if (isMounted && Array.isArray(categories) && categories.length > 0) {
          setFirstCategoryId(categories[0].id || '');
        }
      } catch {
        if (isMounted) {
          setFirstCategoryId('');
        }
      }
    }

    loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <PageBackground variant="home">
      <div className="home-page">
        <section
          className="home-hero"
          aria-labelledby="home-title"
        >
          <div className="home-hero-content">
            <p className="home-eyebrow">MYSHOPYCART</p>

            <h1 id="home-title">Shop Smart. Live Better.</h1>

            <p className="home-hero-description">
              Discover products you love, manage your
              shopping easily, and enjoy a simple,
              secure online shopping experience.
            </p>

            <div className="home-hero-actions">
              <Link
                className="home-primary-action"
                to="/products"
              >
                <Button
                  type="button"
                  variant="primary"
                  size="large"
                >
                  Shop Now
                </Button>
              </Link>

              <Link
                className="home-secondary-action"
                to={firstCategoryId
                  ? `/categories/${encodeURIComponent(firstCategoryId)}`
                  : '/products'}
              >
                Browse Categories
              </Link>
            </div>
          </div>

          <div
            className="home-hero-visual"
            aria-hidden="true"
          >
            <div className="home-hero-orbit">
              <div className="home-hero-cart">
                <span className="home-hero-cart-handle" />
                <span className="home-hero-cart-body" />
                <span className="home-hero-cart-wheel home-hero-cart-wheel-left" />
                <span className="home-hero-cart-wheel home-hero-cart-wheel-right" />
              </div>
            </div>
          </div>
        </section>

        <section
          className="home-benefits"
          aria-labelledby="home-benefits-title"
        >
          <div className="home-section-heading">
            <p className="home-section-eyebrow">WHY MYSHOPYCART</p>

            <h2 id="home-benefits-title">
              Everything you need for a better
              shopping experience.
            </h2>
          </div>

          <div className="home-benefit-grid">
            <Card className="home-benefit-card">
              <div className="home-benefit-icon" aria-hidden="true">
                ✓
              </div>
              <h3>Wide Product Selection</h3>
              <p>
                Explore products across multiple
                categories from one convenient
                customer application.
              </p>
            </Card>

            <Card className="home-benefit-card">
              <div className="home-benefit-icon" aria-hidden="true">
                🔒
              </div>
              <h3>Secure Shopping</h3>
              <p>
                Your account and shopping experience
                are protected with secure
                authentication.
              </p>
            </Card>

            <Card className="home-benefit-card">
              <div className="home-benefit-icon" aria-hidden="true">
                ⚡
              </div>
              <h3>Simple Order Experience</h3>
              <p>
                Find products, manage your cart, and
                follow your orders through one
                connected experience.
              </p>
            </Card>
          </div>
        </section>

        <section
          className="home-callout"
          aria-labelledby="home-callout-title"
        >
          <div>
            <p className="home-callout-eyebrow">START SHOPPING</p>
            <h2 id="home-callout-title">
              Your next shopping experience starts here.
            </h2>
            <p>
              Browse the catalog and discover what
              MYSHOPYCART has to offer.
            </p>
          </div>

          <Link
            className="home-callout-action"
            to="/products"
          >
            Explore Products
          </Link>
        </section>
      </div>
    </PageBackground>
  );
}

export default HomePage;