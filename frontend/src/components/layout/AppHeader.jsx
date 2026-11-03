
import { useState } from 'react';
import BrandLogo from '../branding/BrandLogo';
import PageContainer from './PageContainer';
import AppNavigation from './AppNavigation';
import HeaderSearch from './HeaderSearch';
import HeaderWishlist from './HeaderWishlist';
import HeaderCart from './HeaderCart';
import HeaderAccount from './HeaderAccount';
import MobileMenuButton from './MobileMenuButton';

function AppHeader() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleMobileMenuToggle = () => {
    setIsMobileMenuOpen((isOpen) => !isOpen);
  };

  const handleNavigation = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <header className="app-header">
      <PageContainer>
        <div className="app-header-content">
          <div className="app-header-brand">
            <BrandLogo className="app-logo" />
          </div>

          <AppNavigation
            isMobileMenuOpen={isMobileMenuOpen}
            onNavigate={handleNavigation}
          />

          <HeaderSearch />

          <div className="app-header-actions">
            <HeaderWishlist />
            <HeaderCart />
            <HeaderAccount />

            <MobileMenuButton
              isOpen={isMobileMenuOpen}
              onClick={handleMobileMenuToggle}
            />
          </div>
        </div>
      </PageContainer>
    </header>
  );
}

export default AppHeader;
