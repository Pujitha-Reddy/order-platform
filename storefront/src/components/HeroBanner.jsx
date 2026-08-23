import './HeroBanner.css';

const TILES = [
  { title: 'Gear up for back to school', sub: 'Laptops, tablets & more', color: '#dde9e8' },
  { title: 'Get free delivery on your faves', sub: 'Fast shipping on millions of items', color: '#1e5fb0', dark: true },
  { title: 'Skincare essentials starting at $15', sub: 'Shop top-rated beauty', color: '#e8f27a' },
  { title: 'New season, new fits', sub: 'Apparel for everyone', color: '#f3ded1' },
  { title: 'Level up your home', sub: 'Kitchen & home upgrades', color: '#cdeadb' },
];

export default function HeroBanner() {
  return (
    <div className="hero-banner">
      {TILES.map((tile, i) => (
        <div className={`hero-tile ${tile.dark ? 'hero-tile--dark' : ''}`} style={{ background: tile.color }} key={i}>
          <span className="hero-tile__sub">{tile.sub}</span>
          <h2>{tile.title}</h2>
        </div>
      ))}
    </div>
  );
}