import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function PeliculaDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { items, addItem } = useCart();

  const [pelicula, setPelicula] = useState(null);
  const [funciones, setFunciones] = useState([]);
  const [selectedFuncion, setSelectedFuncion] = useState(null);
  const [isTrailerOpen, setIsTrailerOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [occupiedSeats, setOccupiedSeats] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [pelRes, funRes] = await Promise.all([
          api.get(`/api/v1/peliculas/${id}`),
          api.get('/api/v1/funciones'),
        ]);
        setPelicula(pelRes.data);
        setFunciones(funRes.data.filter((f) => f.peliculaId === parseInt(id)));
      } catch (err) {
        console.error('Error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  useEffect(() => {
    if (!selectedFuncion) {
      setOccupiedSeats([]);
      return;
    }
    const fetchOccupied = async () => {
      try {
        const res = await api.get(`/api/v1/funciones/${selectedFuncion.id}/asientos-ocupados`);
        setOccupiedSeats(res.data);
      } catch (err) {
        console.error('Error fetching occupied seats:', err);
      }
    };
    fetchOccupied();
  }, [selectedFuncion]);

  const isSeatOccupied = (fila, asiento) => {
    return occupiedSeats.some(s => s.fila === fila && s.asiento === asiento);
  };

  const handleSelectSeat = (fila, asiento) => {
    if (!isAuthenticated) { navigate('/login'); return; }
    if (!selectedFuncion) { setMessage('⚠️ Selecciona primero una sesión'); setTimeout(() => setMessage(''), 3000); return; }
    if (isSeatOccupied(fila, asiento)) { setMessage('⛔ Este asiento ya está ocupado'); setTimeout(() => setMessage(''), 3000); return; }
    const added = addItem(selectedFuncion.id, fila, asiento, pelicula.titulo, selectedFuncion.fechaHora, selectedFuncion.precio, `Sala ${selectedFuncion.salaId}`);
    setMessage(added ? `✅ Fila ${fila} · Asiento ${asiento} añadido` : '⚠️ Ya está en tu carrito');
    setTimeout(() => setMessage(''), 3000);
  };

  const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit' });
  };

  const renderStars = (r) => '★'.repeat(Math.round(r / 2)) + '☆'.repeat(5 - Math.round(r / 2));

  if (loading) return <div className="loading-screen"><div className="elegant-reel"></div></div>;
  if (!pelicula) return <div className="empty-state"><h2>Película no encontrada</h2></div>;

  return (
    <div className="detail-page">
      <div className="detail-backdrop" style={{ backgroundImage: pelicula.posterUrl ? `url(${pelicula.posterUrl})` : 'none' }}></div>

      <div className="detail-content">
        <div className="detail-header">
          <div className="detail-poster">
            {pelicula.posterUrl ? <img src={pelicula.posterUrl} alt={pelicula.titulo} onError={(e) => { e.target.onerror = null; e.target.src = `https://placehold.co/400x600/1a1a2e/d4af37?text=${encodeURIComponent(pelicula.titulo)}`; }} /> : <img src={`https://placehold.co/400x600/1a1a2e/d4af37?text=${encodeURIComponent(pelicula.titulo)}`} alt={pelicula.titulo} />}
          </div>
          <div className="detail-info">
            <h1>{pelicula.titulo}</h1>
            <div className="detail-rating" style={{ marginBottom: '1.5rem' }}>
              <span className="stars">{renderStars(pelicula.puntuacion)}</span>
              <span className="rating-number">{pelicula.puntuacion}/10</span>
            </div>

            <div className="clapperboard-panel">
              <div className="clapperboard-top"></div>
              <div className="clapperboard-content">
                <div className="clap-item">
                  <span className="clap-label">Género</span>
                  <span className="clap-value">{pelicula.genero || 'Drama'}</span>
                </div>
                <div className="clap-item">
                  <span className="clap-label">Duración</span>
                  <span className="clap-value">{pelicula.duracion} min</span>
                </div>
                <div className="clap-item">
                  <span className="clap-label">Edad Mínima</span>
                  <span className="clap-value">+{pelicula.edadMinima}</span>
                </div>
              </div>
            </div>

            {pelicula.sinopsis && <p className="detail-sinopsis">{pelicula.sinopsis}</p>}
            {pelicula.trailerUrl && (
              <button className="btn-trailer-premium" onClick={() => setIsTrailerOpen(true)}>
                <span className="play-icon-wrapper">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                </span>
                Ver Tráiler
              </button>
            )}
          </div>
        </div>

        <section className="sessions-section">
          <h2>Sesiones Disponibles</h2>
          {funciones.length === 0 ? (
            <p className="no-sessions">No hay sesiones programadas para esta película.</p>
          ) : (
            <div className="sessions-grid">
              {funciones.map((f) => (
                <button key={f.id} className={`session-card ${selectedFuncion?.id === f.id ? 'selected' : ''}`} onClick={() => setSelectedFuncion(f)}>
                  <div className="session-top">
                    <span className="session-day">{new Date(f.fechaHora).toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' })}</span>
                    <span className="session-time">{new Date(f.fechaHora).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                  <div className="session-bottom">
                    <span className="session-price">{f.precio.toFixed(2)}€</span>
                    <span className="session-sala">Sala {f.salaId}</span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </section>

        {selectedFuncion && (
          <section className="seats-section">
            <h2>Elige tus Asientos</h2>
            <p className="seats-subtitle">{formatDate(selectedFuncion.fechaHora)} — <strong>{selectedFuncion.precio.toFixed(2)}€</strong>/entrada</p>
            {message && <div className="seat-message">{message}</div>}

            <div className="cinema-screen-wrapper">
              <div className="cinema-screen"><span>PANTALLA</span></div>
              <div className="seats-grid">
                {Array.from({ length: 8 }, (_, r) => (
                  <div key={r} className="seat-row">
                    <span className="row-label">{r + 1}</span>
                    {Array.from({ length: 12 }, (_, s) => {
                      const isSelected = items.some(item => selectedFuncion && item.funcionId === selectedFuncion.id && item.fila === r + 1 && item.asiento === s + 1);
                      const isOccupied = isSeatOccupied(r + 1, s + 1);
                      let seatClass = 'seat';
                      if (isOccupied) seatClass += ' occupied';
                      else if (isSelected) seatClass += ' selected';
                      
                      return (
                        <button 
                          key={s} 
                          className={seatClass} 
                          onClick={() => handleSelectSeat(r + 1, s + 1)} 
                          title={`F${r+1} A${s+1}`}
                          disabled={isOccupied}
                        />
                      );
                    })}
                    <span className="row-label">{r + 1}</span>
                  </div>
                ))}
              </div>
              <div className="seats-legend">
                <div className="legend-item"><span className="leg-dot available"></span>Disponible</div>
                <div className="legend-item"><span className="leg-dot vip"></span>VIP</div>
                <div className="legend-item"><span className="leg-dot occupied"></span>Ocupado</div>
              </div>
            </div>

            <button className="btn-primary btn-go-cart" onClick={() => navigate('/carrito')}>🛒 Ver carrito y pagar</button>
          </section>
        )}
      </div>

      {isTrailerOpen && pelicula.trailerUrl && (
        <div className="trailer-modal-overlay" onClick={() => setIsTrailerOpen(false)}>
          <div className="trailer-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="trailer-close-btn" onClick={() => setIsTrailerOpen(false)}>✕</button>
            <iframe 
              width="100%" 
              height="100%" 
              src={pelicula.trailerUrl} 
              title="YouTube video player" 
              frameBorder="0" 
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
              allowFullScreen
            ></iframe>
          </div>
        </div>
      )}
    </div>
  );
}
