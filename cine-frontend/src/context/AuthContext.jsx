import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Inicializar desde localStorage
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const email = localStorage.getItem('userEmail');
    const roles = JSON.parse(localStorage.getItem('userRoles') || '[]');

    if (token && email) {
      setUser({ email, roles, accessToken: token });
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password) => {
    const { data } = await api.post('/api/v1/auth/login', { email, password });

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userRoles', JSON.stringify([...data.roles]));

    const userData = {
      email: data.email,
      roles: [...data.roles],
      accessToken: data.accessToken,
    };
    setUser(userData);
    return userData;
  }, []);

  const register = useCallback(async (email, password) => {
    const { data } = await api.post('/api/v1/auth/register', { email, password });

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userRoles', JSON.stringify([...data.roles]));

    const userData = {
      email: data.email,
      roles: [...data.roles],
      accessToken: data.accessToken,
    };
    setUser(userData);
    return userData;
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    try {
      if (refreshToken) {
        await api.post('/api/v1/auth/logout', { refreshToken });
      }
    } catch (e) {
      // Ignoramos errores de logout
    }

    localStorage.clear();
    setUser(null);
  }, []);

  const isAdmin = user?.roles?.includes('ADMINISTRADOR');
  const isUser = user?.roles?.includes('USUARIO');
  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider
      value={{ user, login, register, logout, isAdmin, isUser, isAuthenticated, loading }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return context;
}
