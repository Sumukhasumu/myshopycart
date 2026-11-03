
import { useState } from 'react';
import { Link } from 'react-router-dom';

import './SettingsPage.css';

function SettingsPage() {
  const [preferences, setPreferences] = useState({
    orderUpdates: true,
    promotionalEmails: false,
  });

  const handlePreferenceChange = (event) => {
    const { name, checked } = event.target;

    setPreferences((currentPreferences) => ({
      ...currentPreferences,
      [name]: checked,
    }));
  };

  return (
    <section
      className="settings-page"
      aria-labelledby="settings-title"
    >
      <header className="settings-page-header">
        <p className="settings-page-eyebrow">
          MYSHOPYCART ACCOUNT
        </p>

        <h1 id="settings-title">Settings</h1>

        <p className="settings-page-description">
          Manage your notification preferences.
        </p>
      </header>

      <section
        className="settings-card"
        aria-labelledby="notification-settings-title"
      >
        <h2 id="notification-settings-title">
          Notification preferences
        </h2>

        <p className="settings-card-description">
          Choose which types of updates you would like to enable
          in this page.
        </p>

        <div className="settings-option">
          <div className="settings-option-copy">
            <label htmlFor="orderUpdates">
              Order updates
            </label>

            <p id="orderUpdates-description">
              Enable preferences for order-related notifications.
            </p>
          </div>

          <input
            id="orderUpdates"
            name="orderUpdates"
            type="checkbox"
            checked={preferences.orderUpdates}
            onChange={handlePreferenceChange}
            aria-describedby="orderUpdates-description"
          />
        </div>

        <div className="settings-option">
          <div className="settings-option-copy">
            <label htmlFor="promotionalEmails">
              Promotional emails
            </label>

            <p id="promotionalEmails-description">
              Enable preferences for offers and promotional messages.
            </p>
          </div>

          <input
            id="promotionalEmails"
            name="promotionalEmails"
            type="checkbox"
            checked={preferences.promotionalEmails}
            onChange={handlePreferenceChange}
            aria-describedby="promotionalEmails-description"
          />
        </div>

        <p className="settings-page-note" role="status">
          These are frontend-only preferences. They are not saved to
          your account or retained after this page is reloaded.
        </p>
      </section>

      <nav className="settings-page-actions" aria-label="Account navigation">
        <Link to="/profile">Back to profile</Link>
        <Link to="/orders">View orders</Link>
      </nav>
    </section>
  );
}

export default SettingsPage;