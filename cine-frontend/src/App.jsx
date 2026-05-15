import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CarteleraPage from './pages/CarteleraPage';
import PeliculaDetailPage from './pages/PeliculaDetailPage';
import CarritoPage from './pages/CarritoPage';
import MisEntradasPage from './pages/MisEntradasPage';
import MiCuentaPage from './pages/MiCuentaPage';
import AdminDashboard from './pages/AdminDashboard';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <div className="app">
            <Navbar />
            <main className="main-content">
              <Routes>
                {/* Rutas Públicas */}
                <Route path="/" element={<CarteleraPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/pelicula/:id" element={<PeliculaDetailPage />} />

                {/* Rutas Protegidas — USUARIO */}
                <Route
                  path="/carrito"
                  element={
                    <ProtectedRoute>
                      <CarritoPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/mis-entradas"
                  element={
                    <ProtectedRoute>
                      <MisEntradasPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/mi-cuenta"
                  element={
                    <ProtectedRoute>
                      <MiCuentaPage />
                    </ProtectedRoute>
                  }
                />

                {/* Rutas Protegidas — ADMIN */}
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute requiredRole="ADMINISTRADOR">
                      <AdminDashboard />
                    </ProtectedRoute>
                  }
                />
              </Routes>
            </main>

            <footer className="footer">
              <p>© 2026 CineMax — Proyecto JWT · Todos los derechos reservados</p>
            </footer>
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
