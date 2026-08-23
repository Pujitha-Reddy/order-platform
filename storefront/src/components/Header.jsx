import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Header.css';

export default function Header() {
  const { user, logout } = useAuth();
  const { totalItems } = useCart();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');

  function handleLogout() {
    logout();
    navigate('/');
  }

  function handleSearch(e) {
    e.preventDefault();
    navigate(query ? `/?q=${encodeURIComponent(query)}` : '/');
  }

  return (
    <header className="site-header">
      <div className="site-header__top">
        <Link to="/" className="site-header__logo">
          order<span className="site-header__logo-accent">platform</span>
        </Link>

        <form className="site-header__search" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search products…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button type="submit" aria-label="Search">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <circle cx="11" cy="11" r="7" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </button>
        </form>

        <div className="site-header__actions">
          {user ? (
            <div className="site-header__account">
              <span className="site-header__account-greeting">Hello, {user.displayName}</span>
              <button onClick={handleLogout} className="site-header__account-link">Log out</button>
            </div>
          ) : (
            <Link to="/login" className="site-header__account">
              <span className="site-header__account-greeting">Hello, sign in</span>
              <span className="site-header__account-link">Account &amp; Lists</span>
            </Link>
          )}

          <Link to="/orders" className="site-header__orders">
            <span className="site-header__account-greeting">Returns</span>
            <span className="site-header__account-link">&amp; Orders</span>
          </Link>

          <Link to="/cart" className="site-header__cart">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" />
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
            </svg>
            {totalItems > 0 && <span className="site-header__cart-badge">{totalItems}</span>}
            <span className="site-header__cart-label">Cart</span>
          </Link>
        </div>
      </div>

      <div className="site-header__bottom">
        <nav className="site-header__nav">
          <Link to="/">All Products</Link>
          {user && <Link to="/orders">My Orders</Link>}
        </nav>
      </div>
    </header>
  );
}