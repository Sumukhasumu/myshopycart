import { Link, useNavigate } from 'react-router-dom';

import useAuth from '../auth/useAuth';
import Button from '../components/ui/Button';
import './ProfilePage.css';

function ProfilePage() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  if (!isAuthenticated) {
    return (
      <section
        className="profile-page"
        aria-labelledby="profile-title"
      >
        <header className="profile-page-header">
          <p className="profile-page-eyebrow">
            MYSHOPYCART ACCOUNT
          </p>

          <h1 id="profile-title">Your Profile</h1>

          <p className="profile-page-description">
            Sign in to view your account information.
          </p>
        </header>

        <div className="profile-card">
          <p role="status">
            You are not currently signed in.
          </p>

          <Link className="ui-button ui-button-primary ui-button-medium" to="/login">
            Sign in
          </Link>
        </div>
      </section>
    );
  }

  const profileFields = [
    { label: 'Username', value: user?.username },
    { label: 'Email', value: user?.email },
    { label: 'Account ID', value: user?.id },
    { label: 'Role', value: user?.role },
  ];

  return (
    <section
      className="profile-page"
      aria-labelledby="profile-title"
    >
      <header className="profile-page-header">
        <p className="profile-page-eyebrow">
          MYSHOPYCART ACCOUNT
        </p>

        <h1 id="profile-title">Your Profile</h1>

        <p className="profile-page-description">
          View the account information available from your current sign-in.
        </p>
      </header>

      <section
        className="profile-card"
        aria-labelledby="profile-information-title"
      >
        <div className="profile-card-header">
          <div>
            <h2 id="profile-information-title">
              Profile information
            </h2>

            <p>
              These details were provided by the authentication service.
            </p>
          </div>
        </div>

        {!user && (
          <p className="profile-notice" role="status">
            Your session is authenticated, but profile details are
            unavailable. Sign out and sign in again to refresh your session.
          </p>
        )}

        <dl className="profile-details">
          {profileFields.map(({ label, value }) => (
            <div className="profile-detail-row" key={label}>
              <dt>{label}</dt>
              <dd>
                {value === null || value === undefined || value === ''
                  ? 'Not provided'
                  : String(value)}
              </dd>
            </div>
          ))}
        </dl>

        <p className="profile-page-note">
          Profile editing is not available because a profile-update API
          has not been configured in the frontend.
        </p>
      </section>

      <nav className="profile-actions" aria-label="Account navigation">
        <Link
          className="ui-button ui-button-secondary ui-button-medium"
          to="/orders"
        >
          View orders
        </Link>

        <Link
          className="ui-button ui-button-secondary ui-button-medium"
          to="/settings"
        >
          Settings
        </Link>

        <Button
          type="button"
          variant="secondary"
          onClick={handleLogout}
        >
          Logout
        </Button>
      </nav>
    </section>
  );
}

export default ProfilePage;