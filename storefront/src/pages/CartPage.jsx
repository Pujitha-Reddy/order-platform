import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { createOrder } from '../api/orders';
import './CartPage.css';

export default function CartPage() {
  const { items, updateQuantity, removeItem, clearCart, totalPrice } = useCart();
  const { user } = useAuth();
  const [error, setError] = useState(null);
  const [placing, setPlacing] = useState(false);
  const navigate = useNavigate();

  async function handleCheckout() {
    setError(null);
    setPlacing(true);
    try {
      const order = await createOrder({
        customerId: user ? user.email : `guest-${Date.now()}@example.com`,
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
        })),
      });
      clearCart();
      navigate(`/orders/${order.id}`);
    } catch {
      setError('Could not place your order. Please try again.');
      setPlacing(false);
    }
  }

  if (items.length === 0) {
    return (
      <div className="cart-page">
        <p className="cart-empty">Your cart is empty.</p>
        <Link to="/" className="cart-empty-link">Browse the catalog &rarr;</Link>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <h1>Your cart</h1>

      <div className="cart-lines">
        {items.map((item) => (
          <div className="cart-line" key={item.productId}>
            <img src={item.imageUrl} alt={item.name} />
            <div className="cart-line__info">
              <span className="cart-line__name">{item.name}</span>
              <span className="cart-line__price">${item.unitPrice.toFixed(2)} each</span>
            </div>
            <input
              type="number"
              min={1}
              value={item.quantity}
              onChange={(e) => updateQuantity(item.productId, Number(e.target.value))}
            />
            <span className="cart-line__subtotal">
              ${(item.unitPrice * item.quantity).toFixed(2)}
            </span>
            <button className="cart-line__remove" onClick={() => removeItem(item.productId)}>
              Remove
            </button>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <span>Total</span>
        <span className="cart-summary__total">${totalPrice.toFixed(2)}</span>
      </div>

      {error && <p className="cart-error">{error}</p>}

      <button className="cart-checkout" onClick={handleCheckout} disabled={placing}>
        {placing ? 'Placing order…' : 'Checkout'}
      </button>
    </div>
  );
}