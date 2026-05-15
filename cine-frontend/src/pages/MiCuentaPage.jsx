import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function MiCuentaPage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState({ email: '', roles: [] });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [isEditing, setIsEditing] = useState(false);
  
  // Edit form state
  const [formData, setFormData] = useState({ email: '', password: '', confirmPassword: '' });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const { data } = await api.get('/api/v1/usuarios/me');
        setProfile(data);
        setFormData({ ...formData, email: data.email });
      } catch (err) {
        console.error('Error cargando perfil:', err);
        setMessage({ text: 'Error al cargar los datos de la cuenta.', type: 'error' });
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setMessage({ text: 'Las contraseñas no coinciden.', type: 'error' });
      return;
    }

    try {
      const payload = { email: formData.email, roles: profile.roles };
      if (formData.password) {
        payload.password = formData.password;
      }

      const { data } = await api.put('/api/v1/usuarios/me', payload);
      setProfile(data);
      setMessage({ text: '✅ Perfil actualizado correctamente.', type: 'success' });
      setIsEditing(false);
      setFormData({ ...formData, password: '', confirmPassword: '' });
    } catch (err) {
      console.error('Error al actualizar:', err);
      setMessage({ 
        text: '❌ Error: ' + (err.response?.data?.message || 'No se pudo actualizar el perfil.'), 
        type: 'error' 
      });
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="elegant-reel"></div>
        <p className="loading-text">Cargando tu perfil...</p>
      </div>
    );
  }

  return (
    <div className="account-page">
      <div className="account-header">
        <h1>👤 Mi Cuenta</h1>
        <p className="account-subtitle">Gestiona tu perfil y preferencias</p>
      </div>

      <div className="account-content">
        {message.text && (
          <div className={`alert-box ${message.type === 'success' ? 'success-box' : 'error-box'}`}>
            {message.text}
          </div>
        )}

        <div className="profile-card">
          <div className="profile-card-header">
            <div className="avatar-placeholder">
              {profile.email ? profile.email.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="profile-title-info">
              <h2>{profile.email}</h2>
              <div className="roles-badges">
                {profile.roles?.map(rol => (
                  <span key={rol} className={`role-badge ${rol.toLowerCase()}`}>
                    {rol === 'ADMINISTRADOR' ? '🛡️ Admin' : '🎟️ Usuario'}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <div className="profile-card-body">
            {!isEditing ? (
              <div className="profile-details-view">
                <div className="detail-group">
                  <span className="detail-label">Correo Electrónico</span>
                  <span className="detail-value">{profile.email}</span>
                </div>
                <div className="detail-group">
                  <span className="detail-label">Contraseña</span>
                  <span className="detail-value">••••••••</span>
                </div>
                
                <button className="btn-edit-profile" onClick={() => setIsEditing(true)}>
                  Editar Perfil
                </button>
              </div>
            ) : (
              <form className="profile-edit-form" onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="email">Correo Electrónico</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    className="glass-input"
                  />
                </div>
                
                <div className="form-group">
                  <label htmlFor="password">Nueva Contraseña <span className="optional-tag">(Opcional)</span></label>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Dejar en blanco para no cambiar"
                    value={formData.password}
                    onChange={handleChange}
                    className="glass-input"
                  />
                </div>

                {formData.password && (
                  <div className="form-group">
                    <label htmlFor="confirmPassword">Confirmar Contraseña</label>
                    <input
                      type="password"
                      id="confirmPassword"
                      name="confirmPassword"
                      placeholder="Repite la nueva contraseña"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      required
                      className="glass-input"
                    />
                  </div>
                )}

                <div className="form-actions">
                  <button type="button" className="btn-cancel" onClick={() => {
                    setIsEditing(false);
                    setFormData({ ...formData, email: profile.email, password: '', confirmPassword: '' });
                    setMessage({ text: '', type: '' });
                  }}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn-save">
                    Guardar Cambios
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
