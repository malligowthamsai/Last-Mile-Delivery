// Shared utility helpers across the app

export const STATUS_META = {
  CREATED:          { label: 'Created',           color: 'created' },
  AGENT_ASSIGNED:   { label: 'Agent Assigned',    color: 'agent_assigned' },
  PICKED_UP:        { label: 'Picked Up',         color: 'picked_up' },
  IN_TRANSIT:       { label: 'In Transit',        color: 'in_transit' },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery',  color: 'out_for_delivery' },
  DELIVERED:        { label: 'Delivered',         color: 'delivered' },
  FAILED:           { label: 'Failed',            color: 'failed' },
  RESCHEDULED:      { label: 'Rescheduled',       color: 'rescheduled' },
  CANCELLED:        { label: 'Cancelled',         color: 'cancelled' },
};

export function StatusBadge({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'created' };
  return (
    <span className={`badge badge-${meta.color}`}>
      <span className="badge-dot" />
      <span>{meta.label}</span>
    </span>
  );
}

export function formatCurrency(amount) {
  return `₹${Number(amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

export function formatDateOnly(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric'
  });
}

export function shortId(id) {
  return id ? `#${id.slice(-8).toUpperCase()}` : '';
}

export function getErrorMsg(err) {
  return err?.response?.data?.error ||
         err?.response?.data?.errors?.[0]?.msg ||
         err?.message ||
         'Something went wrong';
}
