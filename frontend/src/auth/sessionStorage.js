const AUTH_SESSION_KEY =
  'myshopycart.auth.session';

function getSession() {
  const storedSession =
    sessionStorage.getItem(
      AUTH_SESSION_KEY,
    );

  if (!storedSession) {
    return null;
  }

  try {
    return JSON.parse(storedSession);
  } catch {
    sessionStorage.removeItem(
      AUTH_SESSION_KEY,
    );

    return null;
  }
}

function saveSession(
  accessToken,
  user = null,
) {
  const session = {
    accessToken,
    user,
  };

  sessionStorage.setItem(
    AUTH_SESSION_KEY,
    JSON.stringify(session),
  );
}

function clearSession() {
  sessionStorage.removeItem(
    AUTH_SESSION_KEY,
  );
}

export {
  getSession,
  saveSession,
  clearSession,
};