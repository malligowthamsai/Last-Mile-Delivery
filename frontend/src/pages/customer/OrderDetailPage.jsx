import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../lib/api';
import { StatusBadge, STATUS_META, formatCurrency, formatDate, shortId, getErrorMsg } from '../../lib/utils.jsx';
import Navbar from '../../components/Navbar.jsx';
import {
  ArrowLeft,
  MapPin,
  Package,
  Truck,
  CheckCircle2,
  CalendarClock,
  CreditCard,
  User,
  Phone
} from 'lucide-react';

export default function OrderDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [rescheduleDate, setRescheduleDate] = useState('');
  const [rescheduleLoading, setRescheduleLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchOrder = () => {
    api.get(`/orders/${id}`)
      .then((res) => setOrder(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const handleReschedule = async () => {
    if (!rescheduleDate) return;
    setError('');
    setSuccess('');
    setRescheduleLoading(true);
    try {
      await api.post(`/orders/${id}/reschedule`, { newDate: rescheduleDate });
      setSuccess('Order rescheduled successfully. A new agent will be assigned shortly.');
      fetchOrder();
    } catch (err) {
      setError(getErrorMsg(err));
    } finally {
      setRescheduleLoading(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="loading-page">
          <span className="spinner" style={{ width: 36, height: 36 }} />
          <span>Loading order details...</span>
        </div>
      </>
    );
  }

  if (!order) {
    return (
      <>
        <Navbar />
        <div className="page-wrapper">
          <div className="alert alert-error">Order not found or you don't have access.</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="page-wrapper" style={{ maxWidth: 1080 }}>
        {/* ── Header ─────────────────────────────────────────── */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 28, flexWrap: 'wrap', gap: 16 }}>
          <div>
            <button
              className="btn btn-ghost btn-sm"
              onClick={() => navigate(-1)}
              style={{ marginBottom: 10 }}
            >
              <ArrowLeft size={14} /> Back
            </button>
            <h1 className="page-title" style={{ marginBottom: 8 }}>
              Order {shortId(order.id)}
            </h1>
            <StatusBadge status={order.status} />
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: 28, fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
              {formatCurrency(order.totalCharge)}
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 2 }}>Total Charge</div>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: '5fr 3fr', gap: 20 }}>
          {/* ── Left: Timeline + Reschedule ───────────────────── */}
          <div>
            <div className="card" style={{ marginBottom: 20 }}>
              <div className="card-header">
                <h3 className="card-title">Tracking Timeline</h3>
              </div>
              {order.trackingHistory && order.trackingHistory.length > 0 ? (
                <div className="timeline">
                  {order.trackingHistory.map((entry, i) => {
                    const isLast = i === order.trackingHistory.length - 1;
                    return (
                      <div className="timeline-item" key={entry.id}>
                        <div className={`timeline-dot ${isLast ? 'active' : 'completed'}`}>
                          <CheckCircle2 size={15} />
                        </div>
                        <div className="timeline-content">
                          <div className="timeline-status">
                            {STATUS_META[entry.status]?.label || entry.status}
                          </div>
                          <div className="timeline-time">{formatDate(entry.timestamp)}</div>
                          {entry.note && <div className="timeline-note">{entry.note}</div>}
                          {entry.changedByRole && (
                            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>
                              Updated by: {entry.changedByRole}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>No tracking updates yet.</div>
              )}
            </div>

            {/* Reschedule Section */}
            {order.status === 'FAILED' && (user?.role === 'CUSTOMER' || user?.role === 'ADMIN') && (
              <div className="card" style={{ borderColor: 'var(--danger-border)', background: 'var(--danger-bg)' }}>
                <div className="card-header" style={{ borderColor: 'var(--danger-border)' }}>
                  <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <CalendarClock size={16} /> Reschedule Delivery
                  </h3>
                </div>
                <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 16 }}>
                  The delivery attempt failed. Select a new date and we'll dispatch a courier again.
                </p>
                <div style={{ display: 'flex', gap: 12 }}>
                  <input
                    id="reschedule-date"
                    type="date"
                    className="form-input"
                    value={rescheduleDate}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={(e) => setRescheduleDate(e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <button
                    id="reschedule-btn"
                    className="btn btn-primary"
                    onClick={handleReschedule}
                    disabled={!rescheduleDate || rescheduleLoading}
                  >
                    {rescheduleLoading ? <span className="spinner" /> : 'Reschedule'}
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* ── Right: Order Info ─────────────────────────────── */}
          <div>
            {/* Addresses */}
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
                <MapPin size={13} /> Addresses
              </h4>
              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 3 }}>PICKUP</div>
                <div style={{ fontSize: 14, color: 'var(--text-primary)', fontWeight: 500 }}>{order.pickupAddress}</div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                  {order.pickupPincode} · Zone: {order.pickupZone?.name || '—'}
                </div>
              </div>
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 14 }}>
                <div style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600, marginBottom: 3 }}>DROP</div>
                <div style={{ fontSize: 14, color: 'var(--text-primary)', fontWeight: 500 }}>{order.dropAddress}</div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                  {order.dropPincode} · Zone: {order.dropZone?.name || '—'}
                </div>
              </div>
            </div>

            {/* Package Dimensions */}
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
                <Package size={13} /> Package Specs
              </h4>
              <div style={{ fontSize: 13, color: 'var(--text-secondary)', display: 'grid', gap: 8 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Dimensions</span>
                  <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{order.length} × {order.breadth} × {order.height} cm</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Actual Weight</span>
                  <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{order.actualWeight} kg</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Volumetric Weight</span>
                  <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{order.volumetricWeight} kg</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Billable Weight</span>
                  <span style={{ fontWeight: 700, color: 'var(--primary)' }}>{order.billableWeight} kg</span>
                </div>
              </div>
            </div>

            {/* Charges */}
            <div className="card" style={{ marginBottom: 16 }}>
              <h4 style={{ color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
                <CreditCard size={13} /> Pricing Breakdown
              </h4>
              <div style={{ display: 'grid', gap: 8, fontSize: 14 }}>
                <div className="charge-row" style={{ border: 'none', padding: '4px 0' }}>
                  <span>Base charge</span>
                  <span className="charge-value">{formatCurrency(order.baseCharge)}</span>
                </div>
                {order.codSurcharge > 0 && (
                  <div className="charge-row" style={{ border: 'none', padding: '4px 0' }}>
                    <span>COD surcharge</span>
                    <span className="charge-value">{formatCurrency(order.codSurcharge)}</span>
                  </div>
                )}
                <div className="charge-row total" style={{ marginTop: 4 }}>
                  <span>Total</span>
                  <span style={{ color: 'var(--primary)' }}>{formatCurrency(order.totalCharge)}</span>
                </div>
              </div>
            </div>

            {/* Delivery Agent */}
            {order.agent && (
              <div className="card">
                <h4 style={{ color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Truck size={13} /> Courier Agent
                </h4>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div className="user-avatar-circle" style={{ width: 36, height: 36 }}>
                    {order.agent.name?.[0]?.toUpperCase()}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text-primary)' }}>{order.agent.name}</div>
                    {order.agent.phone && (
                      <div style={{ fontSize: 12, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
                        <Phone size={11} /> {order.agent.phone}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
