import {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
} from 'react';
import { api, tokenStore, isExpired, decodeToken } from './api.js';

const Ctx = createContext(null);
export const useAuth = () => useContext(Ctx); // anywhere: const { user, token, isAuthed, login, register, logout } = useAuth();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => {
    const t = tokenStore.get();
    if (t && isExpired(t)) {
      tokenStore.clear();
      return null;
    }
    return t;
  });
  const [user, setUser] = useState(() => (token ? tokenStore.user() : null));

  const logout = useCallback(() => {
    tokenStore.clear();
    setToken(null);
    setUser(null);
  }, []);

  // 401 from any api() call -> logout
  useEffect(() => {
    window.addEventListener('iiq:logout', logout);
    return () => window.removeEventListener('iiq:logout', logout);
  }, [logout]);

  // keep all open tabs in sync (login/logout in one tab)
  useEffect(() => {
    const f = (e) => {
      if (e.key !== 'iiq_token') return;
      const t = tokenStore.get();
      setToken(t);
      setUser(t ? tokenStore.user() : null);
    };
    addEventListener('storage', f);
    return () => removeEventListener('storage', f);
  }, []);

  // auto logout when the 24h token expires
  useEffect(() => {
    const exp = token && decodeToken(token)?.exp;
    if (!exp) return;
    const id = setTimeout(logout, Math.max(exp * 1000 - Date.now(), 0));
    return () => clearTimeout(id);
  }, [token, logout]);

  const login = async (email, password) => {
    const r = await api('/auth/login', {
      method: 'POST',
      body: { email, password },
      auth: false,
    });
    tokenStore.set(r.token, r.user);
    setToken(r.token);
    setUser(r.user);
    return r.user;
  };
  // backend register returns only the user (no token), so we log in right after
  const register = async (name, email, password) => {
    await api('/auth/register', {
      method: 'POST',
      body: { name, email, password },
      auth: false,
    });
    return login(email, password);
  };

  return (
    <Ctx.Provider
      value={{ user, token, isAuthed: !!token, login, register, logout }}
    >
      {children}
    </Ctx.Provider>
  );
}
