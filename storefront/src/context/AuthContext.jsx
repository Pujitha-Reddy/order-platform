import { createContext, useContext, useState, useCallback } from 'react';
import { loginUser, registerUser } from '../api/orders';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const persist = useCallback((authResponse) => {
    const { token, ...userInfo } = authResponse;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userInfo));
    setUser(userInfo);
  }, []);

  const login = useCallback(async (credentials) => {
    const res = await loginUser(credentials);
    persist(res);
  }, [persist]);

  const register = useCallback(async (details) => {
    const res = await registerUser(details);
    persist(res);
  }, [persist]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}