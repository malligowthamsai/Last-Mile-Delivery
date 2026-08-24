import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../lib/api';
import { StatusBadge, STATUS_META, formatDate, shortId, formatCurrency } from '../lib/utils.jsx';
import { 
  Package, 
  MapPin, 
  Clock, 
  CheckCircle2, 
  AlertCircle, 
  ArrowRight, 
  Truck, 
  ArrowLeft,
  Calendar,
  Layers,
  Phone
} from 'lucide-react';

const TRACKING_STEPS = [
  { key: 'CREATED', label: 'Order Placed' },
  { key: 'AGENT_ASSIGNED', label: 'Agent Assigned' },
  { key: 'PICKED_UP', label: 'Picked Up' },
  { key: 'IN_TRANSIT', label: 'In Transit' },
  { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
  { key: 'DELIVERED', label: 'Delivered' }
];

export default function TrackOrderPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    api.get(`/orders/${id}/track`)
      .then((res) => setOrder(res.data))
      .catch((err) => {
        if (err?.response?.status === 404) setNotFound(true);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="loading-page">
        <span className="spinner" style={{ width: 36, height: 36 }} />
        <span>Loading shipment status...</span>
      </div>
    );
  }

  if (notFound || !order) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)' }}>
        <div className="card" style={{ maxWidth: 440, textAlign: 'center', padding: '48px 32px' }}>
          <div className="empty-icon-wrap" style={{ background: 'var(--danger-bg)', color: 'var(--danger)' }}>
            <AlertCircle size={28} />
          </div>
          <h2 style={{ fontSize: 20, fontWeight: 700, marginBottom: 8 }}>Order Not Found</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 24 }}>
            We couldn't find any shipment matching ID <strong style={{ color: 'var(--text-primary)' }}>{id}</strong>. Please verify the tracking number.
          </p>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
            <button className="btn btn-secondary" onClick={() => navigate('/')}>
              Back to Home
            </button>
            <Link to="/login" className="btn btn-primary">
              Log In
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const currentStatusIndex = TRACKING_STEPS.findIndex((s) => s.key === order.status);
  const isDelivered = order.status === 'DELIVERED';
  const isFailed = order.status === 'FAILED';
  const isCancelled = order.status === 'CANCELLED';

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)', paddingBottom: 64 }}>
      {/* ── Header ─────────────────────────────────────────────── */}
      <header className="navbar">
        <div className="navbar-brand" onClick={() => navigate('/')}>
          <div className="brand-icon-box">
            <Package size={18} strokeWidth={2.4} />
          </div>
          <span>LastMile Tracker</span>
        </div>
        <Link to="/login" className="btn btn-secondary btn-sm">
          Portal Login
        </Link>
      </header>

      <div className="page-wrapper" style={{ maxWidth: 840 }}>
        <div style={{ marginBottom: 20 }}>
          <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13, color: 'var(--text-secondary)', fontWeight: 500 }}>
            <ArrowLeft size={14} /> Back to Search
          </Link>
        </div>

        {/* ── Status Hero Card ────────────────────────────────── */}
        <div className="card" style={{ marginBottom: 24 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, borderBottom: '1px solid var(--border-subtle)', paddingBottom: 20 }}>
            <div>
              <div style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Shipment Tracking
              </div>
              <h1 style={{ fontSize: 24, fontWeight: 800, color: 'var(--text-primary)', marginTop: 4, letterSpacing: '-0.02em' }}>
                Order {shortId(order.id)}
              </h1>
              <div style={{ fontSize: 13, color: 'var(--text-secondary)', marginTop: 4 }}>
                Booked on {formatDate(order.createdAt)}
              </div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <StatusBadge status={order.status} />
              {order.expectedDelivery && (
                <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 6 }}>
                  Est. Delivery: {formatDate(order.expectedDelivery)}
                </div>
              )}
            </div>
          </div>

          {/* ── Visual Progress Stepper ──────────────────────── */}
          {!isCancelled && !isFailed && (
            <div style={{ padding: '24px 0 12px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: `repeat(${TRACKING_STEPS.length}, 1fr)`, gap: 8, position: 'relative' }}>
                {TRACKING_STEPS.map((s, idx) => {
                  const isDone = currentStatusIndex >= idx;
                  const isCurrent = order.status === s.key;
                  return (
                    <div key={s.key} style={{ textAlign: 'center', position: 'relative' }}>
                      <div
                        style={{
                          width: 28,
                          height: 28,
                          borderRadius: '50%',
                          background: isCurrent ? 'var(--primary)' : isDone ? 'var(--brand-dark)' : 'var(--bg-subtle)',
                          color: isDone ? '#ffffff' : 'var(--text-muted)',
                          border: `2px solid ${isCurrent ? 'var(--primary)' : isDone ? 'var(--brand-dark)' : 'var(--border)'}`,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          margin: '0 auto 8px',
                          fontSize: 12,
                          fontWeight: 700
                        }}
                      >
                        {isDone ? <CheckCircle2 size={14} /> : idx + 1}
                      </div>
                      <div style={{ fontSize: 11, fontWeight: isCurrent ? 700 : 500, color: isCurrent ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                        {s.label}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {isFailed && (
            <div className="alert alert-error" style={{ marginTop: 16 }}>
              <AlertCircle size={18} />
              <div>
                <strong>Delivery Attempt Failed.</strong> The delivery agent could not complete the dropoff. If you are the customer, please sign in to reschedule.
              </div>
            </div>
          )}
        </div>

        {/* ── Route & Location Details ───────────────────────── */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 24 }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase' }}>
              <MapPin size={15} className="text-slate-500" />
              <span>Pickup Address</span>
            </div>
            <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)' }}>
              {order.pickupAddress}
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              Pincode: {order.pickupPincode} {order.pickupZone && `• Zone: ${order.pickupZone.name}`}
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, color: 'var(--text-secondary)', fontSize: 12, fontWeight: 700, textTransform: 'uppercase' }}>
              <MapPin size={15} className="text-blue-600" />
              <span>Drop Address</span>
            </div>
            <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)' }}>
              {order.dropAddress}
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 4 }}>
              Pincode: {order.dropPincode} {order.dropZone && `• Zone: ${order.dropZone.name}`}
            </div>
          </div>
        </div>

        {/* ── Package Specs & Courier Info ───────────────────── */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 16, marginBottom: 24 }}>
          <div className="card">
            <h3 className="card-title" style={{ marginBottom: 16 }}>Package Information</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Dimensions (L×B×H)</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>
                  {order.length} × {order.breadth} × {order.height} cm
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Actual Weight</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>
                  {order.actualWeight} kg
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Volumetric Wt</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>
                  {order.volumetricWeight} kg
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginTop: 16, paddingTop: 16, borderTop: '1px solid var(--border-subtle)' }}>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Order Type</div>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>
                  {order.orderType}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Payment</div>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)', marginTop: 2 }}>
                  {order.paymentType}
                </div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Total Charge</div>
                <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--primary)', marginTop: 2 }}>
                  {formatCurrency(order.totalCharge)}
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <h3 className="card-title" style={{ marginBottom: 16 }}>Assigned Courier</h3>
            {order.assignedAgent ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                  <div className="user-avatar-circle" style={{ width: 36, height: 36 }}>
                    {order.assignedAgent.name?.[0]?.toUpperCase()}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text-primary)' }}>
                      {order.assignedAgent.name}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Delivery Partner</div>
                  </div>
                </div>
                {order.assignedAgent.phone && (
                  <div style={{ fontSize: 13, color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Phone size={13} /> {order.assignedAgent.phone}
                  </div>
                )}
              </div>
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>
                Agent dispatch in progress. A courier will be assigned shortly.
              </div>
            )}
          </div>
        </div>

        {/* ── Status History Timeline ────────────────────────── */}
        <div className="card">
          <h3 className="card-title" style={{ marginBottom: 20 }}>Tracking History</h3>
          {order.statusHistory && order.statusHistory.length > 0 ? (
            <div className="timeline">
              {order.statusHistory.map((h, i) => (
                <div key={i} className="timeline-item">
                  <div className={`timeline-dot ${i === order.statusHistory.length - 1 ? 'active' : 'completed'}`}>
                    <CheckCircle2 size={15} />
                  </div>
                  <div className="timeline-content">
                    <div className="timeline-status">
                      {STATUS_META[h.status]?.label || h.status}
                    </div>
                    <div className="timeline-time">{formatDate(h.createdAt || h.timestamp)}</div>
                    {h.note && <div className="timeline-note">{h.note}</div>}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>No updates logged yet.</div>
          )}
        </div>
      </div>
    </div>
  );
}
