import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import api from '../api/axios';
import { useState } from 'react';

export default function CarritoPage() {
  const { items, removeItem, clearCart, total } = useCart();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleComprar = async () => {
    if (items.length === 0) return;

    setLoading(true);
    setError('');

    try {
      const entradas = items.map((item) => ({
        funcionId: item.funcionId,
        fila: item.fila,
        asiento: item.asiento,
      }));

      await api.post('/api/v1/ventas', {
        metodoPago: 'TARJETA',
        entradas,
      });

      setSuccess('🎉 ¡Compra realizada con éxito! Tus entradas están listas.');
      clearCart();

      setTimeout(() => navigate('/mis-entradas'), 2500);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al procesar la compra. Inténtalo de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="cart-page">
      <div className="cart-header">
        <h1>Tu Carrito</h1>
        <p className="cart-subtitle">Revisa tus entradas antes de proceder al pago</p>
      </div>

      {success && <div className="alert-box success-box">{success}</div>}
      {error && <div className="alert-box error-box">{error}</div>}

      {items.length === 0 && !success ? (
        <div className="empty-cart-state">
          <div className="empty-cart-icon">🍿</div>
          <h2>Tu carrito está vacío</h2>
          <p>Explora la cartelera y elige tu próxima película</p>
          <button className="btn-primary btn-explore" onClick={() => navigate('/')}>
            Ver Cartelera
          </button>
        </div>
      ) : (
        <div className="cart-layout">
          <div className="cart-items-section">
            <h2 className="section-title">Entradas ({items.length})</h2>
            <div className="cart-items-list">
              {items.map((item) => (
                <div key={item.id} className="cart-item-card">
                  <div className="cart-item-visual">
                    <div className="ticket-cutout-top"></div>
                    <span className="ticket-icon">🎟️</span>
                    <div className="ticket-cutout-bottom"></div>
                  </div>
                  <div className="cart-item-details">
                    <h3>{item.movieTitle}</h3>
                    <div className="ticket-info">
                      <span className="info-date">📅 {formatDate(item.fechaHora)}</span>
                      <span className="info-seat">🏢 {item.salaName} — Fila {item.fila}, Asiento {item.asiento}</span>
                    </div>
                  </div>
                  <div className="cart-item-price-actions">
                    <span className="item-price">{item.precio?.toFixed(2) || '0.00'}€</span>
                    <button className="btn-icon-remove" onClick={() => removeItem(item.id)} title="Eliminar entrada">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18"></path><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="cart-summary-section">
            <div className="summary-card">
              <h2>Resumen del Pedido</h2>
              <div className="summary-row">
                <span>Subtotal ({items.length} entradas)</span>
                <span>{total.toFixed(2)}€</span>
              </div>
              <div className="summary-row">
                <span>Gastos de gestión</span>
                <span>0.00€</span>
              </div>
              <div className="summary-divider"></div>
              <div className="summary-total">
                <span>Total a pagar</span>
                <span className="total-highlight">{total.toFixed(2)}€</span>
              </div>
              
              <button 
                className="btn-pay-now" 
                onClick={handleComprar}
                disabled={loading}
              >
                {loading ? (
                  <span className="processing">Procesando...</span>
                ) : (
                  <>💳 Pagar {total.toFixed(2)}€</>
                )}
              </button>

              <button className="btn-clear-cart" onClick={clearCart}>
                Vaciar carrito
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
