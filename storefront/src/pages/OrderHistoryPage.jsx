import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyOrders } from '../api/orders';
import './OrderHistoryPage.css';

const STATUS_CLASS = {
  CREATED: 'is-pending',
  INVENTORY_RESERVED: 'is-pending',
  PAYMENT_COMPLETED: 'is-pending',
  COMPLETED: 'is-complete',
  CANCELLED: 'is-cancelled',
};

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getMyOrders()
      .then(setOrders)
      .catch(() => setError('Could not load your orders.'));
  }, []);

  if (error) return <div className="oh-status">{error}</div>;
  if (orders === null) return <div className="oh-status">Loading…</div>;
  if (orders.length === 0) return <div className="oh-status">You haven't placed any orders yet.</div>;

  return (
    <div className="oh">
      <h1>My orders</h1>
      <div className="oh-list">
        {orders.map((order) => (
          <Link to={`/orders/${order.id}`} className="oh-row" key={order.id}>
            <span className="oh-row__id">{order.id.slice(0, 8)}</span>
            <span className="oh-row__items">
              {order.items.length} item{order.items.length !== 1 ? 's' : ''}
            </span>
            <span className="oh-row__date">
              {new Date(order.createdAt).toLocaleDateString()}
            </span>
            <span className={`oh-row__status ${STATUS_CLASS[order.status] || ''}`}>
              {order.status.replace(/_/g, ' ')}
            </span>
            <span className="oh-row__total">${order.totalAmount.toFixed(2)}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}