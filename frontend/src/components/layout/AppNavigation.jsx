
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import categoryApi from '../../api/categoryApi';

function AppNavigation({ isMobileMenuOpen, onNavigate }) {
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    let isMounted = true;

    async function loadCategories() {
      try {
        const response = await categoryApi.getCategories();

        if (isMounted) {
          setCategories(Array.isArray(response) ? response : []);
        }
      } catch {
        if (isMounted) {
          setCategories([]);
        }
      }
    }

    loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  const navigationItems = [
    { label: 'Home', path: '/' },
    { label: 'Shop', path: '/products' },
    {
      label: 'Categories',
      path:
        categories.length > 0
          ? `/categories/${encodeURIComponent(categories[0].id)}`
          : '/products',
    },
    { label: 'Orders', path: '/orders' },
  ];

  return (
    <nav
      id="mobile-navigation"
      className={`app-navigation${isMobileMenuOpen ? ' app-navigation--open' : ''}`}
      aria-label="Primary navigation"
    >
      <ul className="app-navigation-list">
        {navigationItems.map((item) => (
          <li key={item.path}>
            <Link
              to={item.path}
              className="app-navigation-link"
              onClick={onNavigate}
            >
              {item.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}

export default AppNavigation;
