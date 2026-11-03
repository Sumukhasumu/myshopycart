import useAuth from '../../auth/useAuth';

function HeaderLogout() {
  const { isAuthenticated, logout } = useAuth();

  if (!isAuthenticated) {
    return null;
  }

  const handleLogout = () => {
    logout();
  };

  return (
    <button
      type="button"
      className="header-logout"
      onClick={handleLogout}
      aria-label="Logout"
      title="Logout"
    >
      <span className="header-logout-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" role="img">
          <path
            d="M10 4H5.5A1.5 1.5 0 0 0 4 5.5v13A1.5 1.5 0 0 0 5.5 20H10"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
          <path
            d="M13 8l4 4-4 4M8 12h9"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </span>
      <span className="header-logout-label">Logout</span>
    </button>
  );
}

export default HeaderLogout;