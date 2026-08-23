import { Link } from 'react-router-dom';
import StarRating from './StarRating';
import './CategoryShelf.css';

export default function CategoryShelf({ title, products }) {
  if (products.length === 0) return null;

  return (
    <section className="shelf">
      <div className="shelf__header">
        <h2>{title}</h2>
      </div>
      <div className="shelf__row">
        {products.map((p) => (
          <Link to={`/products/${p.productId}`} className="shelf-card" key={p.productId}>
            <img src={p.imageUrl} alt={p.name} />
            <span className="shelf-card__name">{p.name}</span>
            <StarRating rating={p.rating} reviewCount={p.reviewCount} />
            <span className="shelf-card__price">${p.price.toFixed(2)}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}