import { useState, useEffect } from 'react';
import api from '../../lib/api';
import { getErrorMsg } from '../../lib/utils.jsx';
import { Map, Plus, Trash2, MapPin } from 'lucide-react';

export default function AdminZones() {
  const [zones, setZones] = useState([]);
  const [areas, setAreas] = useState([]);
  const [newZone, setNewZone] = useState('');
  const [newArea, setNewArea] = useState({ name: '', pincode: '', zoneId: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchAll = () => {
    Promise.all([api.get('/zones'), api.get('/areas')]).then(([zr, ar]) => {
      setZones(zr.data); setAreas(ar.data);
    });
  };

  useEffect(() => { fetchAll(); }, []);

  const addZone = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.post('/zones', { name: newZone });
      setSuccess(`Zone "${newZone}" created`);
      setNewZone('');
      fetchAll();
    } catch (err) { setError(getErrorMsg(err)); }
  };

  const deleteZone = async (id, name) => {
    if (!confirm(`Delete zone "${name}"? All associated areas will also be removed.`)) return;
    try {
      await api.delete(`/zones/${id}`);
      fetchAll();
    } catch (err) { setError(getErrorMsg(err)); }
  };

  const addArea = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.post('/areas', newArea);
      setSuccess(`Area "${newArea.name}" mapped to pincode ${newArea.pincode}`);
      setNewArea({ name: '', pincode: '', zoneId: '' });
      fetchAll();
    } catch (err) { setError(getErrorMsg(err)); }
  };

  const deleteArea = async (id) => {
    try {
      await api.delete(`/areas/${id}`);
      fetchAll();
    } catch (err) { setError(getErrorMsg(err)); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Zones & Coverage Areas</h1>
        <p className="page-subtitle">Create delivery zones and map pincodes for route coverage</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {/* Left: Zones */}
        <div>
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-header">
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Map size={16} /> Add Zone
              </h3>
            </div>
            <form onSubmit={addZone} style={{ display: 'flex', gap: 12 }}>
              <input id="new-zone-name" className="form-input" placeholder="Zone name (e.g. North Mumbai)"
                value={newZone} onChange={(e) => setNewZone(e.target.value)} required />
              <button id="add-zone-btn" type="submit" className="btn btn-primary">
                <Plus size={14} /> Add
              </button>
            </form>
          </div>

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Zones ({zones.length})</h3>
            </div>
            {zones.length === 0 ? (
              <div className="empty-state" style={{ padding: 30 }}>
                <div className="empty-icon-wrap"><Map size={20} /></div>
                <p>No zones created yet</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
                {zones.map((zone) => (
                  <div key={zone.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text-primary)' }}>{zone.name}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>{zone.areas?.length || 0} pincodes mapped</div>
                    </div>
                    <button className="btn btn-danger btn-sm" onClick={() => deleteZone(zone.id, zone.name)}>
                      <Trash2 size={13} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right: Areas */}
        <div>
          <div className="card" style={{ marginBottom: 20 }}>
            <div className="card-header">
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <MapPin size={16} /> Map Pincode → Zone
              </h3>
            </div>
            <form onSubmit={addArea}>
              <div className="form-group">
                <label className="form-label">Area Name</label>
                <input id="new-area-name" className="form-input" placeholder="e.g. Borivali West"
                  value={newArea.name} onChange={(e) => setNewArea({ ...newArea, name: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Pincode</label>
                <input id="new-area-pincode" className="form-input" placeholder="6-digit pincode"
                  value={newArea.pincode} onChange={(e) => setNewArea({ ...newArea, pincode: e.target.value })}
                  maxLength={6} required />
              </div>
              <div className="form-group">
                <label className="form-label">Assign to Zone</label>
                <select id="new-area-zone" className="form-select"
                  value={newArea.zoneId} onChange={(e) => setNewArea({ ...newArea, zoneId: e.target.value })} required>
                  <option value="">— Select zone —</option>
                  {zones.map((z) => <option key={z.id} value={z.id}>{z.name}</option>)}
                </select>
              </div>
              <button id="add-area-btn" type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                <Plus size={14} /> Map Pincode
              </button>
            </form>
          </div>

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Mapped Areas ({areas.length})</h3>
            </div>
            <div style={{ maxHeight: 340, overflowY: 'auto' }}>
              {areas.length === 0 ? (
                <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 24 }}>No areas mapped yet.</div>
              ) : areas.map((area) => (
                <div key={area.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid var(--border-subtle)', fontSize: 14 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{area.pincode}</span>
                    <span style={{ color: 'var(--text-secondary)' }}>{area.name}</span>
                    <span className="badge badge-agent_assigned" style={{ fontSize: 10, padding: '2px 8px' }}>
                      {area.zone?.name}
                    </span>
                  </div>
                  <button className="btn btn-ghost btn-sm btn-icon-only" onClick={() => deleteArea(area.id)}
                    style={{ color: 'var(--danger)' }}>
                    <Trash2 size={13} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
