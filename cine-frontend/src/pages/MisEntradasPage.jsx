import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useNavigate } from 'react-router-dom';

export default function MisEntradasPage() {
  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const fetchVentas = async () => {
    try {
      const { data } = await api.get('/api/v1/ventas/mis-ventas');
      // Sort by date descending (newest first)
      const sorted = data.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
      setVentas(sorted);
    } catch (err) {
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVentas();
  }, []);

  const handleCancelVenta = async (id) => {
    if (!window.confirm('¿Seguro que quieres cancelar esta compra? Se liberarán todas las butacas asociadas.')) return;

    try {
      await api.delete(`/api/v1/ventas/${id}`);
      setMessage('✅ Compra cancelada y reembolsada correctamente');
      fetchVentas();
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage('❌ Error al cancelar: ' + (err.response?.data?.message || 'Error desconocido'));
      setTimeout(() => setMessage(''), 3000);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="vintage-loader-container">
          <div className="vintage-countdown">
            <span className="countdown-number">3</span>
          </div>
          <div className="dust-line"></div>
          <div className="dust-line" style={{ left: '80%', animationDelay: '0.2s' }}></div>
        </div>
        <p className="loading-text">Buscando en el archivo...</p>
      </div>
    );
  }

  return (
    <div className="tickets-page">
      <div className="tickets-header">
        <h1>🎟️ Mis Entradas</h1>
        <p className="tickets-subtitle">Tu historial de compras y reservas activas</p>
      </div>

      {message && (
        <div className={`alert-box ${message.includes('✅') ? 'success-box' : 'error-box'}`}>
          {message}
        </div>
      )}

      {ventas.length === 0 ? (
        <div className="empty-cart-state">
          <div className="empty-cart-icon">🎬</div>
          <h2>Aún no tienes compras</h2>
          <p>Explora nuestra cartelera y consigue tus mejores asientos</p>
          <button className="btn-explore" onClick={() => navigate('/')}>
            Ver Cartelera
          </button>
        </div>
      ) : (
        <div className="purchases-grid">
          {ventas.map((venta) => (
            <div key={venta.id} className="purchase-card vintage-ticket">
              <div className="purchase-header">
                <div className="purchase-id-badge">
                  <span className="badge-icon">🧾</span>
                  Pedido #{venta.id}
                </div>
                <span className={`status-badge ${venta.estado?.toLowerCase()}`}>
                  {venta.estado || 'COMPLETADA'}
                </span>
              </div>

              <div className="purchase-body">
                <div className="purchase-info-row">
                  <div className="info-block">
                    <span className="info-label">Fecha de Compra</span>
                    <span className="info-value">{formatDate(venta.fecha)}</span>
                  </div>
                  <div className="info-block">
                    <span className="info-label">Importe Total</span>
                    <span className="info-value highlight">{venta.importeTotal?.toFixed(2)}€</span>
                  </div>
                  <div className="info-block">
                    <span className="info-label">Método de Pago</span>
                    <span className="info-value">💳 {venta.metodoPago}</span>
                  </div>
                </div>

                {venta.entradas && venta.entradas.length > 0 && (
                  <div className="tickets-breakdown">
                    <h4 className="breakdown-title">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path><line x1="4" y1="22" x2="4" y2="15"></line></svg>
                      Asientos Reservados ({venta.entradas.length})
                    </h4>
                    <div className="seats-list">
                      {[...venta.entradas].sort((a, b) => a.fila - b.fila || a.asiento - b.asiento).map((entrada) => (
                        <div key={entrada.id} className="seat-pill">
                          <div className="seat-pill-icon">💺</div>
                          <div className="seat-pill-details">
                            <span className="seat-loc">Fila {entrada.fila} · Asiento {entrada.asiento}</span>
                            <span className="seat-code">Código: {entrada.codigo || 'TKT-' + entrada.id}</span>
                          </div>
                          <div className={`seat-status-dot ${entrada.estado?.toLowerCase()}`} title={entrada.estado}></div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="purchase-footer">
                <button
                  className="btn-cancel-purchase"
                  onClick={() => handleCancelVenta(venta.id)}
                >
                  Cancelar Compra y Reembolsar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
