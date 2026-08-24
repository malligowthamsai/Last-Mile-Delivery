import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Package, User, LogOut } from 'lucide-react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogoClick = () => {
    if (!user) return navigate('/');
    if (user.role === 'ADMIN') navigate('/admin');
    else if (user.role === 'AGENT') navigate('/agent');
    else navigate('/dashboard');
  };

  return (
    <header className="navbar">
      <div className="navbar-brand" onClick={handleLogoClick}>
        <div className="brand-icon-box">
          <Package size={18} strokeWidth={2.4} />
        </div>
        <span>LastMile</span>
      </div>

      {user ? (
        <div className="navbar-nav">
          <div
            onClick={() => navigate('/profile')}
            className="user-profile-chip"
            title="View Account Profile"
          >
            <div className="user-avatar-circle">
              {user.name?.[0]?.toUpperCase() || <User size={14} />}
            </div>
            <div className="user-meta">
              <span className="user-name-text">{user.name}</span>
              <span className="user-role-badge">{user.role}</span>
            </div>
          </div>

          <button
            className="btn btn-ghost btn-sm"
            onClick={logout}
            title="Log Out"
          >
            <LogOut size={15} />
            <span>Logout</span>
          </button>
        </div>
      ) : (
        <div className="navbar-nav">
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/login')}>
            Log In
          </button>
          <button className="btn btn-primary btn-sm" onClick={() => navigate('/register')}>
            Sign Up
          </button>
        </div>
      )}
    </header>
  );
}
