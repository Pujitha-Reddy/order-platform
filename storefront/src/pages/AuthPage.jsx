import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPage.css';

export default function AuthPage() {
  const [mode, setMode] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (mode === 'login') {
        await login({ email, password });
      } else {
        await register({ email, password, displayName });
      }
      navigate('/');
    } catch (err) {
      const msg = err.response?.data?.message || 'Something went wrong. Please try again.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <span className="auth-eyebrow">order-platform / account</span>
        <h1>{mode === 'login' ? 'Log in' : 'Create an account'}</h1>

        <form onSubmit={handleSubmit}>
          {mode === 'register' && (
            <label>
              Display name
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                required
              />
            </label>
          )}

          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
          </label>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" disabled={submitting}>
            {submitting ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>

        <button
          className="auth-toggle"
          onClick={() => setMode(mode === 'login' ? 'register' : 'login')}
        >
          {mode === 'login' ? "Don't have an account? Register" : 'Already have an account? Log in'}
        </button>
      </div>
    </div>
  );
}