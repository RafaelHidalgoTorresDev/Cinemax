const https = require('https');
const movies = [
  { name: 'Dune Part Two', path: '/movie/693134' },
  { name: 'The Dark Knight', path: '/movie/155' },
  { name: 'Everything Everywhere', path: '/movie/545611' },
  { name: 'LOTR Return of the King', path: '/movie/122' },
  { name: 'Whiplash', path: '/movie/239566' }
];

movies.forEach(m => {
  https.get({
    hostname: 'www.themoviedb.org',
    path: m.path,
    headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' }
  }, res => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      const match = data.match(/<meta property="og:image" content="(.*?)"/);
      if (match) console.log(m.name + ' -> ' + match[1]);
      else console.log(m.name + ' -> NOT FOUND');
    });
  });
});
