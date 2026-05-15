import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAdmin, isAuthenticated, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon">
            <svg viewBox="0 0 40 40" width="32" height="32" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="goldGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#fff1ba" />
                  <stop offset="50%" stopColor="#d4af37" />
                  <stop offset="100%" stopColor="#aa8620" />
                </linearGradient>
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="2" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
              </defs>
              <path d="M20 4C11.16 4 4 11.16 4 20c0 8.84 7.16 16 16 16 3.63 0 6.97-1.2 9.68-3.21l-3.32-3.32C24.58 30.73 22.42 31.5 20 31.5c-6.35 0-11.5-5.15-11.5-11.5S13.65 8.5 20 8.5c2.42 0 4.58.77 6.36 2.03l3.32-3.32C26.97 5.2 23.63 4 20 4z" fill="url(#goldGradient)" filter="url(#glow)" />
              <polygon points="23,14 36,20 23,26" fill="url(#goldGradient)" filter="url(#glow)" />
            </svg>
          </span>
          <span className="brand-text">CINEMAX</span>
        </Link>

        <div className="navbar-links">
          <Link to="/" className="nav-link">Cartelera</Link>

          {isAuthenticated && (
            <>
              <Link to="/mis-entradas" className="nav-link">Mis Entradas</Link>
              <Link to="/mi-cuenta" className="nav-link">👤 Mi Cuenta</Link>
              {isAdmin && (
                <Link to="/admin" className="nav-link nav-admin">
                  ⚙️ Panel Admin
                </Link>
              )}
            </>
          )}
        </div>

        <div className="navbar-actions">
          <Link to="/carrito" className="cart-btn">
            <span className="cart-icon">🎟️</span>
            {count > 0 && <span className="cart-badge">{count}</span>}
          </Link>

          {isAuthenticated ? (
            <div className="user-menu">
              <span className="user-email">{user?.email}</span>
              <span className="user-role-badge">{isAdmin ? 'ADMIN' : 'USUARIO'}</span>
              <button onClick={handleLogout} className="btn-logout">Cerrar Sesión</button>
            </div>
          ) : (
            <div className="auth-buttons">
              <Link to="/login" className="btn-login">Iniciar Sesión</Link>
              <Link to="/registro" className="btn-register">Registrarse</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
