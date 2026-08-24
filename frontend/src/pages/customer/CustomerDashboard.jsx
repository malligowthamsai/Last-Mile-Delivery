import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../lib/api';
import { StatusBadge, formatCurrency, formatDate, shortId } from '../../lib/utils.jsx';
import Navbar from '../../components/Navbar.jsx';
import { 
  Package, 
  Plus, 
  Truck, 
  CheckCircle2, 
  AlertCircle, 
  Search, 
  ExternalLink,
  ArrowRight,
  Clock
} from 'lucide-react';

export default function CustomerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    api.get('/orders')
      .then((res) => setOrders(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const stats = {
    total: orders.length,
    active: orders.filter((o) => !['DELIVERED', 'CANCELLED', 'FAILED'].includes(o.status)).length,
    delivered: orders.filter((o) => o.status === 'DELIVERED').length,
    failed: orders.filter((o) => o.status === 'FAILED').length
  };

  const filteredOrders = orders.filter((o) => {
    if (!searchTerm) return true;
    const s = searchTerm.toLowerCase();
    return (
      o.id.toLowerCase().includes(s) ||
      o.pickupAddress?.toLowerCase().includes(s) ||
      o.dropAddress?.toLowerCase().includes(s) ||
      o.status.toLowerCase().includes(s)
    );
  });

  return (
    <>
      <Navbar />
      <div className="page-wrapper">
        {/* ── Page Header ─────────────────────────────────────── */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28, flexWrap: 'wrap', gap: 16 }}>
          <div>
            <h1 className="page-title">Welcome back, {user?.name}!</h1>
            <p className="page-subtitle">Track your active deliveries and book new shipments</p>
          </div>
          <button
            id="place-order-btn"
            className="btn btn-primary"
            onClick={() => navigate('/place-order')}
          >
            <Plus size={16} />
            <span>Place New Order</span>
          </button>
        </div>

        {/* ── Stats Overview ──────────────────────────────────── */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-header">
              <span className="stat-label">Total Shipments</span>
              <div className="stat-icon">
                <Package size={16} />
              </div>
            </div>
            <div className="stat-value">{stats.total}</div>
            <div className="stat-subtext">All-time bookings</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <span className="stat-label">Active / In Transit</span>
              <div className="stat-icon" style={{ color: 'var(--primary)', background: 'var(--primary-light)' }}>
                <Truck size={16} />
              </div>
            </div>
            <div className="stat-value" style={{ color: 'var(--primary)' }}>{stats.active}</div>
            <div className="stat-subtext">On route or assigned</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <span className="stat-label">Delivered</span>
              <div className="stat-icon" style={{ color: 'var(--success)', background: 'var(--success-bg)' }}>
                <CheckCircle2 size={16} />
              </div>
            </div>
            <div className="stat-value" style={{ color: 'var(--success)' }}>{stats.delivered}</div>
            <div className="stat-subtext">Successfully fulfilled</div>
          </div>

          <div className="stat-card">
            <div className="stat-header">
              <span className="stat-label">Failed / Issues</span>
              <div className="stat-icon" style={{ color: 'var(--danger)', background: 'var(--danger-bg)' }}>
                <AlertCircle size={16} />
              </div>
            </div>
            <div className="stat-value" style={{ color: stats.failed > 0 ? 'var(--danger)' : 'var(--text-primary)' }}>
              {stats.failed}
            </div>
            <div className="stat-subtext">Requires rescheduling</div>
          </div>
        </div>

        {/* ── Orders Table Section ────────────────────────────── */}
        <div className="card">
          <div className="card-header" style={{ flexWrap: 'wrap', gap: 12 }}>
            <div>
              <h2 className="card-title">Recent Orders</h2>
              <p className="card-subtitle">Manage and track your delivery history</p>
            </div>
            <div style={{ position: 'relative', width: 260 }}>
              <input
                type="text"
                className="form-input"
                placeholder="Search orders..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ paddingLeft: 34, fontSize: 13 }}
              />
              <Search size={14} style={{ position: 'absolute', left: 12, top: 12, color: 'var(--text-muted)' }} />
            </div>
          </div>

          {loading ? (
            <div className="loading-page" style={{ minHeight: 200 }}>
              <span className="spinner" style={{ width: 28, height: 28 }} />
              <span>Loading orders...</span>
            </div>
          ) : filteredOrders.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon-wrap">
                <Package size={24} />
              </div>
              <h3>No shipments found</h3>
              <p>{orders.length === 0 ? 'Place your first order to get started with fast doorstep delivery.' : 'No orders matched your search criteria.'}</p>
              {orders.length === 0 && (
                <button className="btn btn-primary" onClick={() => navigate('/place-order')} style={{ marginTop: 16 }}>
                  <Plus size={16} /> Place First Order
                </button>
              )}
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Order ID</th>
                    <th>Pickup → Drop Route</th>
                    <th>Type</th>
                    <th>Payment</th>
                    <th>Total Charge</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredOrders.map((o) => (
                    <tr key={o.id}>
                      <td>
                        <Link to={`/orders/${o.id}`} style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                          {shortId(o.id)}
                        </Link>
                      </td>
                      <td>
                        <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-primary)' }}>
                          {o.pickupAddress ? o.pickupAddress.slice(0, 24) : '—'}...
                        </div>
                        <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                          → {o.dropAddress ? o.dropAddress.slice(0, 24) : '—'}...
                        </div>
                      </td>
                      <td>
                        <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>
                          {o.orderType}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                          {o.paymentType}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                          {formatCurrency(o.totalCharge)}
                        </span>
                      </td>
                      <td>
                        <StatusBadge status={o.status} />
                      </td>
                      <td>
                        <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                          {formatDate(o.createdAt)}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                          <Link to={`/orders/${o.id}`} className="btn btn-secondary btn-sm" title="View Details">
                            Details
                          </Link>
                          <Link to={`/track/${o.id}`} className="btn btn-ghost btn-sm" title="Public Tracking View">
                            <ExternalLink size={14} />
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
