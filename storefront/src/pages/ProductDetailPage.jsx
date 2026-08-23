import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getProduct } from '../api/products';
import { useCart } from '../context/CartContext';
import StarRating from '../components/StarRating';
import './ProductDetailPage.css';

export default function ProductDetailPage() {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const { addItem } = useCart();
  const navigate = useNavigate();

  useEffect(() => {
    getProduct(productId).then(setProduct).catch(() => setProduct(false));
  }, [productId]);

  if (product === null) return <div className="pdp-status">Loading…</div>;
  if (product === false) return <div className="pdp-status">Product not found.</div>;

  const outOfStock = product.availableQuantity === 0;

  function handleAdd() {
    addItem(product, quantity);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  }

  return (
    <div className="pdp">
      <Link to="/" className="pdp-back">&larr; Back to catalog</Link>

      <div className="pdp-grid">
        <img src={product.imageUrl} alt={product.name} className="pdp-image" />

        <div className="pdp-info">
          <span className="pdp-sku">{product.productId}</span>
          <h1>{product.name}</h1>
          <div className="pdp-rating">
            <StarRating rating={product.rating} reviewCount={product.reviewCount} size="lg" />
          </div>
          <p className="pdp-description">{product.description}</p>
        </div>

        <div className="pdp-buybox">
          <div className="pdp-price">
            <span className="pdp-price-symbol">$</span>{product.price.toFixed(2)}
          </div>

          <span className={`pdp-stock ${outOfStock ? 'is-out' : 'is-live'}`}>
            {outOfStock ? 'Out of stock' : `In stock (${product.availableQuantity} available)`}
          </span>

          <span className="pdp-delivery">FREE delivery <strong>tomorrow</strong>. Order within 4 hrs.</span>

          {!outOfStock && (
            <>
              <label className="pdp-qty-label">
                Quantity:
                <select value={quantity} onChange={(e) => setQuantity(Number(e.target.value))}>
                  {Array.from({ length: Math.min(10, product.availableQuantity) }, (_, i) => i + 1).map((n) => (
                    <option key={n} value={n}>{n}</option>
                  ))}
                </select>
              </label>
              <button onClick={handleAdd} className="pdp-add">
                {added ? 'Added ✓' : 'Add to Cart'}
              </button>
              <button className="pdp-buy-now" onClick={() => { addItem(product, quantity); navigate('/cart'); }}>
                Buy Now
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}