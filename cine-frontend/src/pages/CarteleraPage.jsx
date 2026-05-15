import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const GENEROS = ['Todos', 'Ciencia Ficción', 'Acción', 'Drama', 'Thriller', 'Aventura', 'Fantasía', 'Romance', 'Animación'];

// Helper for 100% reliable local SVG placeholders (fixes broken images instantly without external services)
const getFallbackImage = (title) => {
  const shortTitle = title.length > 20 ? title.substring(0, 17) + '...' : title;
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="600" viewBox="0 0 400 600">
    <rect width="100%" height="100%" fill="#111118"/>
    <rect width="100%" height="100%" fill="none" stroke="#d4af37" stroke-width="8" opacity="0.5"/>
    <text x="50%" y="45%" dominant-baseline="middle" text-anchor="middle" fill="#d4af37" font-family="sans-serif" font-size="60px">🎬</text>
    <text x="50%" y="55%" dominant-baseline="middle" text-anchor="middle" fill="#f4f4f8" font-family="sans-serif" font-size="22px" font-weight="bold">${shortTitle}</text>
  </svg>`;
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
};

export default function CarteleraPage() {
  const [peliculas, setPeliculas] = useState([]);
  const [funciones, setFunciones] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [generoFilter, setGeneroFilter] = useState('Todos');
  const [loading, setLoading] = useState(true);

  // Classic Quotes Logic with verified TMDB backdrops
  const [quoteIndex, setQuoteIndex] = useState(0);
  const quotes = [
    { 
      text: "Le haré una oferta que no podrá rechazar.", 
      author: "El Padrino (Francis Ford Coppola)",
      image: "https://image.tmdb.org/t/p/w1280/tmU7GeKVybMWFButWEGl2M4GeiP.jpg"
    },
    { 
      text: "Anda, alégrame el día.", 
      author: "Harry el Sucio (Clint Eastwood)",
      image: "https://image.tmdb.org/t/p/w1280/mJjOZSaTTqG3an5MkkOX3x18lYX.jpg"
    },
    { 
      text: "¿Me estás hablando a mí?", 
      author: "Taxi Driver (Martin Scorsese)",
      image: "https://image.tmdb.org/t/p/w1280/5MVSXJieOhbyZudCnV1H4YJpfPV.jpg"
    },
    { 
      text: "Que la Fuerza te acompañe.", 
      author: "Star Wars (George Lucas)",
      image: "https://image.tmdb.org/t/p/w1280/zqkmTXzjkAgXmEWLRsY4UpTWCeo.jpg"
    },
    { 
      text: "Me encanta el olor a napalm por la mañana.", 
      author: "Apocalypse Now (Francis Ford Coppola)",
      image: "https://image.tmdb.org/t/p/w1280/49fzLedFVKgGgFChLMuulDJBY1c.jpg"
    },
  ];

  useEffect(() => {
    const fetchDatos = async () => {
      try {
        const [pelRes, funRes] = await Promise.all([
          api.get('/api/v1/peliculas'),
          api.get('/api/v1/funciones'),
        ]);
        setPeliculas(pelRes.data);
        setFunciones(funRes.data);
      } catch (err) {
        console.error('Error cargando cartelera:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDatos();
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setQuoteIndex((prev) => (prev + 1) % quotes.length);
    }, 5000);
    return () => clearInterval(interval);
  }, [quotes.length]);

  const getFuncionCount = (peliculaId) =>
    funciones.filter((f) => f.peliculaId === peliculaId).length;

  const filteredMovies = peliculas.filter((p) => {
    const matchSearch = p.titulo.toLowerCase().includes(searchTerm.toLowerCase());
    const matchGenero = generoFilter === 'Todos' || p.genero === generoFilter;
    return matchSearch && matchGenero;
  });

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="elegant-reel"></div>
        <p className="loading-text">Preparando la sala...</p>
      </div>
    );
  }

  return (
    <div className="cartelera-page">
      <section 
        className="hero-section" 
        style={{ 
          backgroundImage: `linear-gradient(to bottom, rgba(7, 7, 10, 0.5), rgba(7, 7, 10, 1)), url(${quotes[quoteIndex].image})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center 20%'
        }}
      >
        <div className="hero-content">
          <div className="classic-quotes-container">
            {quotes.map((quote, idx) => (
              <h1 key={idx} className={`classic-quote ${idx === quoteIndex ? 'active' : ''}`}>
                "{quote.text}"
                <span className="classic-quote-author">— {quote.author}</span>
              </h1>
            ))}
          </div>
        </div>
        <div className="hero-particles">
          {[...Array(6)].map((_, i) => <div key={i} className="particle" style={{'--i': i}}></div>)}
        </div>
      </section>

      <div className="layout-container">
        <section className="filters-section">
          <div className="genre-filters">
            {GENEROS.map((gen) => (
              <button
                key={gen}
                className={`genre-btn ${generoFilter === gen ? 'active' : ''}`}
                onClick={() => setGeneroFilter(gen)}
              >
                {gen}
              </button>
            ))}
          </div>
          <div className="search-bar">
            <input
              type="text"
              placeholder="Buscar película..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </section>

        {filteredMovies.length === 0 ? (
          <div className="no-results">
            <h3>No se encontraron películas</h3>
            <p>Prueba con otros términos o filtros de género</p>
          </div>
        ) : (
          <div className="movies-grid">
            {filteredMovies.map((p) => (
              <div key={p.id} className="movie-card">
                <div className="movie-poster-wrapper">
                  <img 
                    src={p.posterUrl || getFallbackImage(p.titulo)} 
                    alt={p.titulo} 
                    className="movie-poster" 
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = getFallbackImage(p.titulo);
                    }}
                  />
                  <div className="movie-overlay">
                    <Link to={`/pelicula/${p.id}`} className="btn-comprar">
                      Comprar Entradas
                    </Link>
                  </div>
                </div>
                <div className="movie-info">
                  <h3 className="movie-title" title={p.titulo}>{p.titulo}</h3>
                  <div className="movie-meta">
                    <span className="meta-item">⏱️ {p.duracion} min</span>
                    <span className="meta-item age-rating">+{p.edadMinima}</span>
                  </div>
                  <div className="movie-functions-badge">
                    {getFuncionCount(p.id)} funciones disponibles
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
