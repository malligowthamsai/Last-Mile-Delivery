import { NavLink, Routes, Route } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/Navbar.jsx';
import AdminOrders from './AdminOrders.jsx';
import AdminZones from './AdminZones.jsx';
import AdminRateCards from './AdminRateCards.jsx';
import AdminAgents from './AdminAgents.jsx';
import AdminOverview from './AdminOverview.jsx';
import AdminCreateOrder from './AdminCreateOrder.jsx';
import {
  LayoutDashboard,
  Package,
  PlusCircle,
  Map,
  CreditCard,
  Users,
  LogOut
} from 'lucide-react';

export default function AdminLayout() {
  const { user, logout } = useAuth();

  const navItems = [
    { path: '/admin', label: 'Overview', end: true, icon: <LayoutDashboard size={16} /> },
    { path: '/admin/orders', label: 'All Orders', icon: <Package size={16} /> },
    { path: '/admin/create-order', label: 'Create Order', icon: <PlusCircle size={16} /> },
    { path: '/admin/zones', label: 'Zones & Areas', icon: <Map size={16} /> },
    { path: '/admin/rate-cards', label: 'Rate Cards', icon: <CreditCard size={16} /> },
    { path: '/admin/agents', label: 'Agent Fleet', icon: <Users size={16} /> },
  ];

  return (
    <>
      <Navbar />
      <div className="app-layout">
        <aside className="sidebar">
          <div style={{ padding: '0 12px 16px', marginBottom: 12, borderBottom: '1px solid var(--border-subtle)' }}>
            <div style={{ fontSize: 11, fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
              Admin Console
            </div>
            <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 4, fontWeight: 500 }}>
              {user?.email}
            </div>
          </div>

          <nav className="sidebar-nav">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
              >
                {item.icon}
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="sidebar-footer">
            <button className="sidebar-link" onClick={logout} style={{ color: 'var(--danger)' }}>
              <LogOut size={16} />
              Sign Out
            </button>
          </div>
        </aside>

        <main className="main-content">
          <Routes>
            <Route index element={<AdminOverview />} />
            <Route path="orders" element={<AdminOrders />} />
            <Route path="zones" element={<AdminZones />} />
            <Route path="create-order" element={<AdminCreateOrder />} />
            <Route path="rate-cards" element={<AdminRateCards />} />
            <Route path="agents" element={<AdminAgents />} />
          </Routes>
        </main>
      </div>
    </>
  );
}
