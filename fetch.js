const https = require('https');

const fetchPoster = (movieId) => {
  return new Promise((resolve) => {
    https.get('https://www.themoviedb.org/movie/' + movieId, {
      headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' }
    }, res => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        const match = data.match(/https:\/\/image\.tmdb\.org\/t\/p\/w500(\/.*?\.jpg)/);
        resolve(match ? match[1] : 'NOT FOUND');
      });
    });
  });
};

(async () => {
  console.log('The Dark Knight: ' + await fetchPoster('155'));
  console.log('LOTR Return King: ' + await fetchPoster('122'));
  console.log('Whiplash: ' + await fetchPoster('244786'));
  console.log('Everything: ' + await fetchPoster('545611'));
})();
