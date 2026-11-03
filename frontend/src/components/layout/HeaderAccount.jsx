
import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../auth/useAuth';

function HeaderAccount() {
  const { isAuthenticated, user, logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const username = user?.username?.trim() || 'Account';
  const role = String(user?.role ?? '').toUpperCase();

  const accountItems =
    role === 'DISTRIBUTOR'
      ? [
          { label: 'Distributor Dashboard', path: '/distributor/dashboard' },
          { label: 'My Profile', path: '/profile' },
          { label: 'Manage My Products', path: '/distributor/products' },
          { label: 'Manage My Inventory', path: '/distributor/inventory' },
        ]
      : role === 'ADMIN'
        ? [
            { label: 'Admin Dashboard', path: '/admin/dashboard' },
            { label: 'Manage Categories', path: '/admin/categories' },
            { label: 'Manage Products', path: '/admin/products' },
            { label: 'My Profile', path: '/profile' },
          ]
        : [
            { label: 'My Profile', path: '/profile' },
          ];

  // Close the dropdown when clicking or touching outside it,
  // or when pressing the Escape key.
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    function handleOutsideClick(event) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    }

    function handleEscape(event) {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    }

    document.addEventListener('pointerdown', handleOutsideClick);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('pointerdown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen]);

  const handleLogout = () => {
    setIsOpen(false);
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) {
    return (
      <Link className="header-account" to="/login">
        <span className="header-account-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24">
            <circle
              cx="12"
              cy="8"
              r="3.2"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
            />
            <path
              d="M5.5 20a6.5 6.5 0 0 1 13 0"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </span>
        <span className="header-account-label">Login</span>
      </Link>
    );
  }

  return (
    <div
      className="header-account-dropdown"
      ref={dropdownRef}
    >
      <button
        type="button"
        className="header-account"
        onClick={() => setIsOpen((open) => !open)}
        aria-label={`Account menu for ${username}`}
        aria-expanded={isOpen}
        aria-haspopup="true"
      >
        <span className="header-account-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24">
            <circle
              cx="12"
              cy="8"
              r="3.2"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
            />
            <path
              d="M5.5 20a6.5 6.5 0 0 1 13 0"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </span>

        <span className="header-account-label">{username}</span>
        <span className="header-account-chevron" aria-hidden="true">
          ▾
        </span>
      </button>

      {isOpen && (
        <div className="header-account-menu">
          {accountItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className="header-account-menu-link"
              onClick={() => setIsOpen(false)}
            >
              {item.label}
            </Link>
          ))}

          <div className="header-account-menu-divider" />

          <button
            type="button"
            className="header-account-menu-logout"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}

export default HeaderAccount;
