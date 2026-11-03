import BrandLogo from '../branding/BrandLogo';
import PageContainer from './PageContainer';

function AppFooter() {
  return (
    <footer className="app-footer">
      <PageContainer>
        <div className="app-footer-content">
          <div className="app-footer-brand">
            <BrandLogo
              className="app-footer-logo"
            />

            <p className="app-footer-tagline">
              SHOP SMART • LIVE BETTER
            </p>
          </div>

          <div className="app-footer-copyright">
            <p>
              MYSHOPYCART | SHOP SMART • LIVE BETTER
            </p>

            <p>
              © 2026 Sumukha S
            </p>
          </div>
        </div>
      </PageContainer>
    </footer>
  );
}

export default AppFooter;