import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { getErrorMsg } from '../lib/utils.jsx';
import { Package, Lock, Mail, ArrowRight, ShieldCheck, User, Truck } from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e, customCredentials) => {
    if (e) e.preventDefault();
    setError('');
    setLoading(true);
    const payload = customCredentials || form;
    try {
      const res = await api.post('/auth/login', payload);
      login(res.data.user, res.data.token);
      const role = res.data.user.role;
      if (role === 'ADMIN') navigate('/admin');
      else if (role === 'AGENT') navigate('/agent');
      else navigate('/dashboard');
    } catch (err) {
      setError(getErrorMsg(err));
    } finally {
      setLoading(false);
    }
  };

  const handleDemoClick = (email, password) => {
    setForm({ email, password });
    handleLogin(null, { email, password });
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-box">
            <Package size={22} strokeWidth={2.4} />
          </div>
          <h1>Welcome Back</h1>
          <p>Sign in to manage your shipments and dispatches</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={(e) => handleLogin(e)}>
          <div className="form-group">
            <label className="form-label">
              <Mail size={14} className="text-slate-400" />
              <span>Email Address</span>
            </label>
            <input
              id="login-email"
              type="email"
              className="form-input"
              placeholder="name@company.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              <Lock size={14} className="text-slate-400" />
              <span>Password</span>
            </label>
            <input
              id="login-password"
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>

          <button
            id="login-submit"
            type="submit"
            className="btn btn-primary w-full"
            disabled={loading}
            style={{ marginTop: 6, width: '100%' }}
          >
            {loading ? (
              <>
                <span className="spinner" /> Signing in...
              </>
            ) : (
              <>
                <span>Sign In</span>
                <ArrowRight size={15} />
              </>
            )}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--text-secondary)', fontSize: 14 }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ fontWeight: 600 }}>
            Sign Up
          </Link>
        </p>

        {/* ── Demo Accounts Quick Switcher ─────────────────────── */}
        <div className="auth-demo-grid">
          <div className="auth-demo-title">
            <span>Quick Demo Logins</span>
            <span style={{ fontSize: 11, fontWeight: 500, color: 'var(--text-muted)' }}>1-Click</span>
          </div>
          <div className="demo-btn-group">
            <button
              type="button"
              className="demo-pill-btn"
              onClick={() => handleDemoClick('admin@lastmile.com', 'admin123')}
            >
              Admin
            </button>
            <button
              type="button"
              className="demo-pill-btn"
              onClick={() => handleDemoClick('customer@test.com', 'customer123')}
            >
              Customer
            </button>
            <button
              type="button"
              className="demo-pill-btn"
              onClick={() => handleDemoClick('agent1@lastmile.com', 'agent123')}
            >
              Agent
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
