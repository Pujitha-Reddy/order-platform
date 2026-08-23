import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrder } from '../api/orders';
import { WS_URL } from '../api/client';
import './OrderTrackingPage.css';

const STAGES = [
  { key: 'CREATED', label: 'Order received' },
  { key: 'INVENTORY_RESERVED', label: 'Stock reserved' },
  { key: 'PAYMENT_COMPLETED', label: 'Payment approved' },
];

const FAILURE_STATES = new Set(['INVENTORY_FAILED', 'PAYMENT_FAILED', 'CANCELLED']);

function stageIndex(status) {
  return STAGES.findIndex((s) => s.key === status);
}

export default function OrderTrackingPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [log, setLog] = useState([]);
  const [connected, setConnected] = useState(false);
  const wsRef = useRef(null);

  useEffect(() => {
    getOrder(orderId)
      .then(setOrder)
      .catch(() => {});
  }, [orderId]);

  useEffect(() => {
    function connect() {
      const ws = new WebSocket(WS_URL);
      wsRef.current = ws;

      ws.onopen = () => setConnected(true);
      ws.onclose = () => {
        setConnected(false);
        setTimeout(connect, 3000);
      };
      ws.onmessage = (event) => {
        const update = JSON.parse(event.data);
        if (update.orderId !== orderId) return;

        setOrder(update);
        setLog((prev) => [
          ...prev,
          { status: update.status, detail: update.detail, at: update.updatedAt },
        ]);
      };
    }
    connect();
    return () => wsRef.current?.close();
  }, [orderId]);

  const failed = order && FAILURE_STATES.has(order.status);
  const currentStage = order ? stageIndex(order.status) : -1;

  return (
    <div className="tracker">
      <Link to="/" className="tracker-back">&larr; Back to catalog</Link>

      <div className="tracker-head">
        <span className={`ws-dot ${connected ? 'is-live' : ''}`} />
        <span className="tracker-ws-label">{connected ? 'live feed connected' : 'reconnecting…'}</span>
      </div>

      <h1 className="tracker-order-id">order {orderId.slice(0, 8)}</h1>

      <ol className="stage-list">
        {STAGES.map((stage, i) => {
          const reached = !failed && currentStage >= i;
          const active = !failed && currentStage === i;
          return (
            <li key={stage.key} className={`stage ${reached ? 'is-reached' : ''} ${active ? 'is-active' : ''}`}>
              <span className="stage-marker" />
              <span className="stage-label">{stage.label}</span>
            </li>
          );
        })}
        {failed && (
          <li className="stage is-failed is-reached is-active">
            <span className="stage-marker" />
            <span className="stage-label">{order.detail || 'Order cancelled'}</span>
          </li>
        )}
      </ol>

      <div className="terminal">
        <div className="terminal-header">saga event log</div>
        <div className="terminal-body">
          {log.length === 0 && <div className="terminal-line terminal-line--dim">waiting for events…</div>}
          {log.map((entry, i) => (
            <div key={i} className="terminal-line">
              <span className="terminal-time">{new Date(entry.at).toLocaleTimeString()}</span>
              <span className="terminal-status">{entry.status}</span>
              {entry.detail && <span className="terminal-detail">{entry.detail}</span>}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}