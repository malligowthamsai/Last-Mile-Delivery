import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../lib/api';
import { StatusBadge, formatCurrency, shortId, formatDate } from '../../lib/utils.jsx';
import {
  Package,
  Clock,
  Truck,
  CheckCircle2,
  AlertCircle,
  Users,
  ArrowRight,
  Zap
} from 'lucide-react';

const STATUS_ORDER = [
  { key: 'CREATED',          label: 'Pending Assignment', color: '#d97706' },
  { key: 'AGENT_ASSIGNED',   label: 'Agent Assigned',     color: '#0284c7' },
  { key: 'PICKED_UP',        label: 'Picked Up',          color: '#8b5cf6' },
  { key: 'IN_TRANSIT',       label: 'In Transit',         color: '#ea580c' },
  { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery',   color: '#059669' },
  { key: 'DELIVERED',        label: 'Delivered',          color: '#16a34a' },
  { key: 'FAILED',           label: 'Failed',             color: '#dc2626' },
  { key: 'RESCHEDULED',      label: 'Rescheduled',        color: '#d97706' },
  { key: 'CANCELLED',        label: 'Cancelled',          color: '#94a3b8' },
];

export default function AdminOverview() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [agents, setAgents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.get('/orders'),
      api.get('/agents')
    ]).then(([ordersRes, agentsRes]) => {
      setOrders(ordersRes.data);
      setAgents(agentsRes.data);
    }).finally(() => setLoading(false));
  }, []);

  const handleAutoAssign = async (orderId) => {
    setActionLoading(orderId);
    setError('');
    try {
      await api.post(`/orders/${orderId}/auto-assign`);
      const res = await api.get('/orders');
      setOrders(res.data);
    } catch (err) {
      setError(err?.response?.data?.error || 'Auto-assign failed');
    } finally {
      setActionLoading(null);
    }
  };

  const counts = {
    total:      orders.length,
    created:    orders.filter((o) => o.status === 'CREATED').length,
    inProgress: orders.filter((o) => ['AGENT_ASSIGNED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY'].includes(o.status)).length,
    delivered:  orders.filter((o) => o.status === 'DELIVERED').length,
    failed:     orders.filter((o) => o.status === 'FAILED').length,
    rescheduled: orders.filter((o) => o.status === 'RESCHEDULED').length,
    agents:     agents.length,
    agentsAvailable: agents.filter((a) => a.agentProfile?.isAvailable).length,
  };

  const pendingOrders = orders.filter((o) => ['CREATED', 'RESCHEDULED'].includes(o.status));

  const chartData = STATUS_ORDER.map((s) => ({
    ...s,
    count: orders.filter((o) => o.status === s.key).length
  })).filter((s) => s.count > 0);

  const maxCount = Math.max(...chartData.map((s) => s.count), 1);

  if (loading) {
    return (
      <div className="loading-page" style={{ minHeight: 300 }}>
        <span className="spinner" style={{ width: 32, height: 32 }} />
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Dashboard Overview</h1>
        <p className="page-subtitle">Real-time delivery operations at a glance</p>
      </div>

      {/* ── Stat Cards ─────────────────────────────────────────── */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">Total Orders</span>
            <div className="stat-icon"><Package size={16} /></div>
          </div>
          <div className="stat-value">{counts.total}</div>
        </div>
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">Pending Dispatch</span>
            <div className="stat-icon" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
              <Clock size={16} />
            </div>
          </div>
          <div className="stat-value" style={{ color: 'var(--warning)' }}>{counts.created}</div>
        </div>
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">In Progress</span>
            <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
              <Truck size={16} />
            </div>
          </div>
          <div className="stat-value" style={{ color: 'var(--info)' }}>{counts.inProgress}</div>
        </div>
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">Delivered</span>
            <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
              <CheckCircle2 size={16} />
            </div>
          </div>
          <div className="stat-value" style={{ color: 'var(--success)' }}>{counts.delivered}</div>
        </div>
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">Failed / Resched.</span>
            <div className="stat-icon" style={{ background: 'var(--danger-bg)', color: 'var(--danger)' }}>
              <AlertCircle size={16} />
            </div>
          </div>
          <div className="stat-value" style={{ color: 'var(--danger)' }}>{counts.failed + counts.rescheduled}</div>
        </div>
        <div className="stat-card">
          <div className="stat-header">
            <span className="stat-label">Fleet Available</span>
            <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
              <Users size={16} />
            </div>
          </div>
          <div className="stat-value">{counts.agentsAvailable}<span style={{ fontSize: 16, color: 'var(--text-muted)' }}>/{counts.agents}</span></div>
        </div>
      </div>

      {/* ── Alert Banners ──────────────────────────────────────── */}
      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}
      {counts.created > 0 && (
        <div className="alert alert-warning" style={{ marginBottom: 12 }}>
          <Clock size={16} /> {counts.created} order{counts.created > 1 ? 's' : ''} waiting for agent assignment.
        </div>
      )}
      {counts.rescheduled > 0 && (
        <div className="alert alert-warning" style={{ marginBottom: 12 }}>
          <AlertCircle size={16} /> {counts.rescheduled} rescheduled order{counts.rescheduled > 1 ? 's' : ''} need a new agent.
        </div>
      )}

      {/* ── Two Column: Chart + Fleet ──────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Order Status Distribution</h3>
          </div>
          {chartData.length === 0 ? (
            <div className="empty-state" style={{ padding: 30 }}>
              <div className="empty-icon-wrap"><Package size={20} /></div>
              <p>No orders yet</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {chartData.map((s) => (
                <div key={s.key}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 13 }}>
                    <span style={{ color: 'var(--text-secondary)' }}>{s.label}</span>
                    <span style={{ fontWeight: 700, color: s.color }}>{s.count}</span>
                  </div>
                  <div style={{ background: 'var(--bg-subtle)', borderRadius: 6, height: 8, overflow: 'hidden' }}>
                    <div style={{
                      height: '100%',
                      width: `${(s.count / maxCount) * 100}%`,
                      background: s.color,
                      borderRadius: 6,
                      transition: 'width 0.5s ease',
                      minWidth: s.count > 0 ? 8 : 0
                    }} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Agent Fleet Status</h3>
          </div>
          {agents.length === 0 ? (
            <div className="empty-state" style={{ padding: 30 }}>
              <div className="empty-icon-wrap"><Users size={20} /></div>
              <p>No agents registered</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
                <div style={{ flex: 1, background: 'var(--bg-subtle)', borderRadius: 6, height: 10, overflow: 'hidden', display: 'flex' }}>
                  <div style={{
                    height: '100%',
                    width: `${agents.length ? (counts.agentsAvailable / counts.agents) * 100 : 0}%`,
                    background: 'var(--success)',
                    transition: 'width 0.5s ease'
                  }} />
                </div>
                <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--success)', whiteSpace: 'nowrap' }}>
                  {counts.agentsAvailable} online
                </span>
              </div>
              {agents.map((agent) => (
                <div key={agent.id} style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '8px 12px', background: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)'
                }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)' }}>{agent.name}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Zone: {agent.agentProfile?.zone?.name || '—'}</div>
                  </div>
                  <span className={`badge ${agent.agentProfile?.isAvailable ? 'badge-delivered' : 'badge-cancelled'}`}>
                    <span className="badge-dot" />
                    {agent.agentProfile?.isAvailable ? 'Available' : 'Busy'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Pending Assignment Table ───────────────────────────── */}
      {pendingOrders.length > 0 && (
        <div className="card">
          <div className="card-header">
            <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Zap size={16} style={{ color: 'var(--warning)' }} /> Orders Needing Dispatch
            </h3>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/admin/orders')}>
              View All <ArrowRight size={14} />
            </button>
          </div>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Customer</th>
                  <th>Route</th>
                  <th>Type</th>
                  <th>Charge</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pendingOrders.map((order) => (
                  <tr key={order.id}>
                    <td
                      style={{ fontWeight: 600, color: 'var(--text-primary)', cursor: 'pointer' }}
                      onClick={() => navigate(`/orders/${order.id}`)}
                    >
                      {shortId(order.id)}
                    </td>
                    <td style={{ fontSize: 13 }}>{order.customer?.name || '—'}</td>
                    <td style={{ fontSize: 12 }}>
                      <div>{order.pickupZone?.name || order.pickupPincode}</div>
                      <div style={{ color: 'var(--text-muted)' }}>→ {order.dropZone?.name || order.dropPincode}</div>
                    </td>
                    <td style={{ fontSize: 12 }}>{order.orderType} / {order.paymentType}</td>
                    <td style={{ fontWeight: 600 }}>{formatCurrency(order.totalCharge)}</td>
                    <td><StatusBadge status={order.status} /></td>
                    <td style={{ fontSize: 12 }}>{formatDate(order.createdAt)}</td>
                    <td>
                      <button
                        id={`overview-auto-assign-${order.id}`}
                        className="btn btn-primary btn-sm"
                        onClick={() => handleAutoAssign(order.id)}
                        disabled={actionLoading === order.id}
                      >
                        {actionLoading === order.id ? <span className="spinner" /> : (
                          <>
                            <Zap size={13} /> Assign
                          </>
                        )}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {pendingOrders.length === 0 && counts.total > 0 && (
        <div className="card" style={{ textAlign: 'center', padding: 40 }}>
          <div className="empty-icon-wrap" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <CheckCircle2 size={24} />
          </div>
          <div style={{ fontWeight: 600, color: 'var(--success)', fontSize: 16 }}>All orders are dispatched</div>
          <div style={{ color: 'var(--text-muted)', fontSize: 14, marginTop: 4 }}>
            No pending agent assignments at this time.
          </div>
        </div>
      )}
    </div>
  );
}
