import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function AdminDashboard() {
  const [stats, setStats] = useState({ peliculas: 0, funciones: 0, salas: 0, ventas: 0, usuarios: 0 });
  const [ventas, setVentas] = useState([]);
  const [peliculas, setPeliculas] = useState([]);
  const [funciones, setFunciones] = useState([]);
  const [salas, setSalas] = useState([]);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });

  // Formularios
  const [newPelicula, setNewPelicula] = useState({ 
    titulo: '', duracion: '', edadMinima: '', genero: 'Acción', sinopsis: '', posterUrl: '', trailerUrl: '' 
  });
  const [newSala, setNewSala] = useState({ nombre: '', capacidad: '' });
  const [newFuncion, setNewFuncion] = useState({ 
    fechaHora: '', precio: '', peliculaId: '', salaId: '' 
  });

  useEffect(() => {
    fetchAll();
  }, []);

  const fetchAll = async () => {
    try {
      const [pelRes, funRes, salRes, venRes, usrRes] = await Promise.all([
        api.get('/api/v1/peliculas'),
        api.get('/api/v1/funciones'),
        api.get('/api/v1/salas'),
        api.get('/api/v1/ventas'),
        api.get('/api/v1/usuarios'),
      ]);

      setPeliculas(pelRes.data);
      setFunciones(funRes.data);
      setSalas(salRes.data);
      setVentas(venRes.data);
      setStats({
        peliculas: pelRes.data.length,
        funciones: funRes.data.length,
        salas: salRes.data.length,
        ventas: venRes.data.length,
        usuarios: usrRes.data.length,
      });

      // Set defaults for dropdowns if available
      if (pelRes.data.length > 0 && salRes.data.length > 0) {
        setNewFuncion(prev => ({
          ...prev, 
          peliculaId: pelRes.data[0].id, 
          salaId: salRes.data[0].id 
        }));
      }

    } catch (err) {
      console.error('Error cargando datos admin:', err);
      showMsg('❌ Error al cargar datos del servidor', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showMsg = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 4000);
  };

  // --- Películas ---
  const handleCreatePelicula = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/v1/peliculas', {
        titulo: newPelicula.titulo,
        duracion: parseInt(newPelicula.duracion),
        edadMinima: parseInt(newPelicula.edadMinima),
        genero: newPelicula.genero,
        sinopsis: newPelicula.sinopsis,
        posterUrl: newPelicula.posterUrl,
        trailerUrl: newPelicula.trailerUrl,
      });
      showMsg('✅ Película creada correctamente');
      setNewPelicula({ titulo: '', duracion: '', edadMinima: '', genero: 'Acción', sinopsis: '', posterUrl: '', trailerUrl: '' });
      fetchAll();
    } catch (err) {
      showMsg('❌ Error: ' + (err.response?.data?.message || 'Revisa los datos'), 'error');
    }
  };

  const handleDeletePelicula = async (id) => {
    if (!window.confirm('¿Estás seguro de eliminar esta película? Se borrarán sus funciones.')) return;
    try {
      await api.delete(`/api/v1/peliculas/${id}`);
      showMsg('✅ Película eliminada');
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al eliminar', 'error');
    }
  };

  // --- Funciones ---
  const handleCreateFuncion = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/v1/funciones', {
        fechaHora: newFuncion.fechaHora,
        precio: parseFloat(newFuncion.precio),
        peliculaId: parseInt(newFuncion.peliculaId),
        salaId: parseInt(newFuncion.salaId),
      });
      showMsg('✅ Función creada correctamente');
      setNewFuncion(prev => ({ ...prev, fechaHora: '', precio: '' }));
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al crear función: ' + (err.response?.data?.message || ''), 'error');
    }
  };

  const handleDeleteFuncion = async (id) => {
    if (!window.confirm('¿Eliminar esta función? Las entradas vendidas se cancelarán.')) return;
    try {
      await api.delete(`/api/v1/funciones/${id}`);
      showMsg('✅ Función eliminada');
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al eliminar función', 'error');
    }
  };

  // --- Salas ---
  const handleCreateSala = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/v1/salas', {
        nombre: newSala.nombre,
        capacidad: parseInt(newSala.capacidad),
      });
      showMsg('✅ Sala creada');
      setNewSala({ nombre: '', capacidad: '' });
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al crear sala', 'error');
    }
  };

  const handleDeleteSala = async (id) => {
    if (!window.confirm('¿Eliminar esta sala?')) return;
    try {
      await api.delete(`/api/v1/salas/${id}`);
      showMsg('✅ Sala eliminada');
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al eliminar', 'error');
    }
  };

  // --- Ventas ---
  const handleCancelVenta = async (id) => {
    if (!window.confirm(`¿Estás seguro de cancelar la venta #${id}?`)) return;
    try {
      await api.delete(`/api/v1/ventas/${id}`);
      showMsg('✅ Venta cancelada correctamente');
      fetchAll();
    } catch (err) {
      showMsg('❌ Error al cancelar venta', 'error');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('es-ES', {
      day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  };

  const getPeliculaNombre = (id) => peliculas.find(p => p.id === id)?.titulo || 'Desconocida';
  const getSalaNombre = (id) => salas.find(s => s.id === id)?.nombre || 'Desconocida';

  const totalIngresos = ventas
    .filter(v => v.estado !== 'CANCELADA')
    .reduce((sum, v) => sum + (v.importeTotal || 0), 0);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="elegant-reel"></div>
        <p className="loading-text">Cargando datos del panel...</p>
      </div>
    );
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>⚙️ Panel de Administración</h1>
        <p className="admin-subtitle">Gestión centralizada del sistema CineMax</p>
      </div>

      {message.text && (
        <div className={`alert-box ${message.type === 'success' ? 'success-box' : 'error-box'}`}>
          {message.text}
        </div>
      )}

      {/* TABS */}
      <div className="admin-tabs">
        {['dashboard', 'peliculas', 'funciones', 'salas', 'ventas'].map((tab) => (
          <button
            key={tab}
            className={`admin-tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'dashboard' && '📊 Dashboard'}
            {tab === 'peliculas' && '🎬 Películas'}
            {tab === 'funciones' && '🕐 Horarios'}
            {tab === 'salas' && '🏢 Salas'}
            {tab === 'ventas' && '🛒 Ventas'}
          </button>
        ))}
      </div>

      <div className="admin-content">
        {/* DASHBOARD */}
        {activeTab === 'dashboard' && (
          <div className="stats-grid">
            <div className="stat-card">
              <span className="stat-icon">🎬</span>
              <span className="stat-value">{stats.peliculas}</span>
              <span className="stat-label">Películas</span>
            </div>
            <div className="stat-card">
              <span className="stat-icon">🕐</span>
              <span className="stat-value">{stats.funciones}</span>
              <span className="stat-label">Funciones Activas</span>
            </div>
            <div className="stat-card">
              <span className="stat-icon">🏢</span>
              <span className="stat-value">{stats.salas}</span>
              <span className="stat-label">Salas Configuradas</span>
            </div>
            <div className="stat-card">
              <span className="stat-icon">🛒</span>
              <span className="stat-value">{stats.ventas}</span>
              <span className="stat-label">Tickets Vendidos</span>
            </div>
            <div className="stat-card">
              <span className="stat-icon">👤</span>
              <span className="stat-value">{stats.usuarios}</span>
              <span className="stat-label">Usuarios Registrados</span>
            </div>
            <div className="stat-card stat-highlight">
              <span className="stat-icon">💰</span>
              <span className="stat-value">{totalIngresos.toFixed(2)}€</span>
              <span className="stat-label">Ingresos Netos Totales</span>
            </div>
          </div>
        )}

        {/* PELÍCULAS */}
        {activeTab === 'peliculas' && (
          <div className="admin-section">
            <div className="section-header">
              <h2>🎬 Añadir Nueva Película</h2>
            </div>
            
            <form onSubmit={handleCreatePelicula} className="admin-form glass-panel">
              <div className="form-row">
                <div className="form-group">
                  <label>Título</label>
                  <input type="text" value={newPelicula.titulo} onChange={(e) => setNewPelicula({ ...newPelicula, titulo: e.target.value })} required className="glass-input" />
                </div>
                <div className="form-group">
                  <label>Género</label>
                  <select value={newPelicula.genero} onChange={(e) => setNewPelicula({ ...newPelicula, genero: e.target.value })} className="glass-input">
                    {['Ciencia Ficción', 'Acción', 'Drama', 'Thriller', 'Aventura', 'Fantasía', 'Romance', 'Animación'].map(g => <option key={g} value={g}>{g}</option>)}
                  </select>
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Duración (min)</label>
                  <input type="number" min="1" value={newPelicula.duracion} onChange={(e) => setNewPelicula({ ...newPelicula, duracion: e.target.value })} required className="glass-input" />
                </div>
                <div className="form-group">
                  <label>Edad Mínima</label>
                  <input type="number" min="0" value={newPelicula.edadMinima} onChange={(e) => setNewPelicula({ ...newPelicula, edadMinima: e.target.value })} required className="glass-input" />
                </div>
              </div>

              <div className="form-group">
                <label>URL del Póster</label>
                <input type="url" value={newPelicula.posterUrl} onChange={(e) => setNewPelicula({ ...newPelicula, posterUrl: e.target.value })} required className="glass-input" />
              </div>

              <div className="form-group">
                <label>URL del Tráiler (YouTube Embed)</label>
                <input type="url" value={newPelicula.trailerUrl} onChange={(e) => setNewPelicula({ ...newPelicula, trailerUrl: e.target.value })} className="glass-input" placeholder="Ej: https://www.youtube.com/embed/..." />
              </div>
              
              <div className="form-group">
                <label>Sinopsis</label>
                <textarea rows="3" value={newPelicula.sinopsis} onChange={(e) => setNewPelicula({ ...newPelicula, sinopsis: e.target.value })} required className="glass-input"></textarea>
              </div>
              
              <button type="submit" className="btn-primary btn-full">+ Guardar Película</button>
            </form>

            <h3 className="section-subtitle">Películas en el sistema</h3>
            <div className="admin-table-wrapper glass-panel">
              <table className="admin-table">
                <thead>
                  <tr><th>ID</th><th>Póster</th><th>Título</th><th>Género</th><th>Duración</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                  {peliculas.map((p) => (
                    <tr key={p.id}>
                      <td>#{p.id}</td>
                      <td><img src={p.posterUrl} alt="poster" className="table-poster-thumb"/></td>
                      <td className="fw-bold">{p.titulo}</td>
                      <td>{p.genero}</td>
                      <td>{p.duracion} min</td>
                      <td>
                        <button className="btn-danger-icon" onClick={() => handleDeletePelicula(p.id)} title="Eliminar">🗑️</button>
                      </td>
                    </tr>
                  ))}
                  {peliculas.length === 0 && <tr><td colSpan="6" className="text-center">No hay películas registradas</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* FUNCIONES (HORARIOS) */}
        {activeTab === 'funciones' && (
          <div className="admin-section">
            <div className="section-header">
              <h2>🕐 Programar Nueva Función</h2>
            </div>
            
            <form onSubmit={handleCreateFuncion} className="admin-form glass-panel">
              <div className="form-row">
                <div className="form-group">
                  <label>Película</label>
                  <select value={newFuncion.peliculaId} onChange={(e) => setNewFuncion({ ...newFuncion, peliculaId: e.target.value })} required className="glass-input">
                    {peliculas.map(p => <option key={p.id} value={p.id}>{p.titulo}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Sala</label>
                  <select value={newFuncion.salaId} onChange={(e) => setNewFuncion({ ...newFuncion, salaId: e.target.value })} required className="glass-input">
                    {salas.map(s => <option key={s.id} value={s.id}>{s.nombre} (Cap: {s.capacidad})</option>)}
                  </select>
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Fecha y Hora</label>
                  <input type="datetime-local" value={newFuncion.fechaHora} onChange={(e) => setNewFuncion({ ...newFuncion, fechaHora: e.target.value })} required className="glass-input" />
                </div>
                <div className="form-group">
                  <label>Precio de la entrada (€)</label>
                  <input type="number" step="0.5" min="0" value={newFuncion.precio} onChange={(e) => setNewFuncion({ ...newFuncion, precio: e.target.value })} required className="glass-input" />
                </div>
              </div>
              
              <button type="submit" className="btn-primary btn-full">+ Programar Función</button>
            </form>

            <h3 className="section-subtitle">Horarios activos</h3>
            <div className="admin-table-wrapper glass-panel">
              <table className="admin-table">
                <thead>
                  <tr><th>ID</th><th>Película</th><th>Sala</th><th>Fecha / Hora</th><th>Precio</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                  {funciones.map((f) => (
                    <tr key={f.id}>
                      <td>#{f.id}</td>
                      <td className="fw-bold">{getPeliculaNombre(f.peliculaId)}</td>
                      <td>{getSalaNombre(f.salaId)}</td>
                      <td>{formatDate(f.fechaHora)}</td>
                      <td>{f.precio?.toFixed(2)}€</td>
                      <td>
                        <button className="btn-danger-icon" onClick={() => handleDeleteFuncion(f.id)} title="Eliminar">🗑️</button>
                      </td>
                    </tr>
                  ))}
                  {funciones.length === 0 && <tr><td colSpan="6" className="text-center">No hay funciones programadas</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* SALAS */}
        {activeTab === 'salas' && (
          <div className="admin-section">
            <div className="section-header">
              <h2>🏢 Gestión de Salas</h2>
            </div>
            
            <form onSubmit={handleCreateSala} className="admin-form glass-panel">
              <div className="form-row">
                <div className="form-group">
                  <label>Nombre de la Sala</label>
                  <input type="text" placeholder="Ej: Sala IMAX" value={newSala.nombre} onChange={(e) => setNewSala({ ...newSala, nombre: e.target.value })} required className="glass-input" />
                </div>
                <div className="form-group">
                  <label>Capacidad (Butacas)</label>
                  <input type="number" min="1" value={newSala.capacidad} onChange={(e) => setNewSala({ ...newSala, capacidad: e.target.value })} required className="glass-input" />
                </div>
              </div>
              <button type="submit" className="btn-primary btn-full">+ Registrar Sala</button>
            </form>

            <div className="admin-table-wrapper glass-panel">
              <table className="admin-table">
                <thead>
                  <tr><th>ID</th><th>Nombre</th><th>Capacidad Total</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                  {salas.map((s) => (
                    <tr key={s.id}>
                      <td>#{s.id}</td>
                      <td className="fw-bold">{s.nombre}</td>
                      <td>{s.capacidad} butacas</td>
                      <td>
                        <button className="btn-danger-icon" onClick={() => handleDeleteSala(s.id)}>🗑️</button>
                      </td>
                    </tr>
                  ))}
                  {salas.length === 0 && <tr><td colSpan="4" className="text-center">No hay salas registradas</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* VENTAS */}
        {activeTab === 'ventas' && (
          <div className="admin-section">
            <div className="section-header">
              <h2>🛒 Supervisión de Entradas y Pagos</h2>
            </div>

            <div className="admin-table-wrapper glass-panel">
              <table className="admin-table">
                <thead>
                  <tr><th>ID Venta</th><th>Fecha</th><th>Importe</th><th>Método</th><th>Usuario</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                  {ventas.map((v) => (
                    <tr key={v.id}>
                      <td className="fw-bold">#{v.id}</td>
                      <td>{formatDate(v.fecha)}</td>
                      <td>{v.importeTotal?.toFixed(2)}€</td>
                      <td><span className="badge-method">{v.metodoPago}</span></td>
                      <td>{v.usuarioId ? `ID: ${v.usuarioId}` : 'Invitado'}</td>
                      <td>
                        <span className={`status-badge ${v.estado?.toLowerCase() || 'completada'}`}>
                          {v.estado || 'COMPLETADA'}
                        </span>
                      </td>
                      <td>
                        {v.estado !== 'CANCELADA' && (
                          <button className="btn-cancel-admin" onClick={() => handleCancelVenta(v.id)}>
                            Anular Venta
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {ventas.length === 0 && <tr><td colSpan="7" className="text-center">No hay ventas registradas</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
