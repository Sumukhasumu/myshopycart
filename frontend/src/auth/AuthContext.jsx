import { useMemo, useState } from 'react';
import AuthContext from './AuthContext';
import {
  clearSession,
  getSession,
  saveSession,
} from './sessionStorage';

function AuthProvider({ children }) {
  const [session, setSession] = useState(() =>
    getSession(),
  );

  const accessToken = session?.accessToken ?? null;
  const user = session?.user ?? null;
  const isAuthenticated = Boolean(accessToken);

  const login = (token, authenticatedUser = null) => {
    saveSession(token, authenticatedUser);

    setSession({
      accessToken: token,
      user: authenticatedUser,
    });
  };

  const logout = () => {
    clearSession();
    setSession(null);
  };

  const value = useMemo(
    () => ({
      accessToken,
      user,
      isAuthenticated,
      login,
      logout,
    }),
    [accessToken, user, isAuthenticated],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthProvider;