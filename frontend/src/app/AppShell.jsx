import {
  Outlet,
} from 'react-router-dom';

import AppHeader from '../components/layout/AppHeader';
import AppFooter from '../components/layout/AppFooter';
import PageContainer from '../components/layout/PageContainer';

function AppShell() {
  return (
    <div className="app-shell">
      <AppHeader />

      <main className="app-main">
        <PageContainer>
          <Outlet />
        </PageContainer>
      </main>

      <AppFooter />
    </div>
  );
}

export default AppShell;