import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { listProducts } from '../api/products';
import { useCart } from '../context/CartContext';
import StarRating from '../components/StarRating';
import HeroBanner from '../components/HeroBanner';
import CategoryShelf from '../components/CategoryShelf';
import './ShopPage.css';

function groupByCategory(products) {
  const groups = {};
  for (const p of products) {
    const key = p.category || 'more';
    if (!groups[key]) groups[key] = [];
    groups[key].push(p);
  }
  return groups;
}

function titleCase(s) {
  return s.replace(/\b\w/g, (c) => c.toUpperCase());
}

export default function ShopPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [addedId, setAddedId] = useState(null);
  const { addItem } = useCart();
  const [searchParams] = useSearchParams();
  const query = (searchParams.get('q') || '').toLowerCase().trim();

  useEffect(() => {
    listProducts()
      .then(setProducts)
      .catch(() => setError('Could not reach the catalog service.'))
      .finally(() => setLoading(false));
  }, []);

  function handleAdd(product) {
    addItem(product, 1);
    setAddedId(product.productId);
    setTimeout(() => setAddedId(null), 1200);
  }

  if (loading) return <p className="shop-status">Loading catalog…</p>;
  if (error) return <p className="shop-status shop-status--error">{error}</p>;

  // Search mode: show a dense results grid, guaranteed at least a few
  // relevant items by widening the match (name -> description -> category)
  // if an exact name match comes up too thin.
  if (query) {
    let results = products.filter((p) => p.name.toLowerCase().includes(query));
    if (results.length < 5) {
      const wider = products.filter(
        (p) =>
          p.description.toLowerCase().includes(query) ||
          (p.category || '').toLowerCase().includes(query)
      );
      const ids = new Set(results.map((p) => p.productId));
      results = [...results, ...wider.filter((p) => !ids.has(p.productId))];
    }

    return (
      <div className="shop">
        <p className="shop-search-result">
          {results.length} result{results.length !== 1 ? 's' : ''} for "<strong>{query}</strong>"
        </p>
        {results.length === 0 ? (
          <p className="shop-status">No products found. Try browsing the catalog instead.</p>
        ) : (
          <div className="product-grid">
            {results.map((p) => (
              <ProductCard key={p.productId} p={p} addedId={addedId} onAdd={handleAdd} />
            ))}
          </div>
        )}
      </div>
    );
  }

  // Browse mode: hero banner + category shelves.
  const grouped = groupByCategory(products);

  return (
    <div>
      <HeroBanner />
      {Object.entries(grouped).map(([category, items]) => (
        <CategoryShelf key={category} title={titleCase(category)} products={items} />
      ))}
    </div>
  );
}

function ProductCard({ p, addedId, onAdd }) {
  const outOfStock = p.availableQuantity === 0;
  return (
    <article className="product-card">
      <Link to={`/products/${p.productId}`} className="product-card__image-link">
        <img src={p.imageUrl} alt={p.name} className="product-card__image" />
      </Link>
      <Link to={`/products/${p.productId}`} className="product-card__name">{p.name}</Link>
      <StarRating rating={p.rating} reviewCount={p.reviewCount} />
      <div className="product-card__price">
        <span className="product-card__price-symbol">$</span>
        <span className="product-card__price-whole">{Math.floor(p.price)}</span>
        <span className="product-card__price-cents">{(p.price % 1).toFixed(2).slice(2)}</span>
      </div>
      <span className="product-card__delivery">FREE delivery <strong>tomorrow</strong></span>
      {outOfStock ? (
        <span className="product-card__oos">Currently unavailable</span>
      ) : (
        <button className="product-card__buy" onClick={() => onAdd(p)}>
          {addedId === p.productId ? 'Added ✓' : 'Add to Cart'}
        </button>
      )}
    </article>
  );
}