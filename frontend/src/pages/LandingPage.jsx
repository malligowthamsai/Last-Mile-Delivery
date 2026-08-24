import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Package, 
  Search, 
  ArrowRight, 
  Truck, 
  MapPin, 
  Zap, 
  ShieldCheck, 
  Layers, 
  BarChart3, 
  Clock,
  CheckCircle2
} from 'lucide-react';

const features = [
  {
    icon: <Clock size={22} className="text-blue-600" />,
    title: 'Real-Time Tracking',
    desc: 'Live milestone updates from package pickup to doorstep delivery. Both sender and receiver stay informed.'
  },
  {
    icon: <Zap size={22} className="text-amber-600" />,
    title: 'Intelligent Auto-Dispatch',
    desc: 'Automated agent assignment engine routes parcels to the nearest available fleet agent in the pickup zone.'
  },
  {
    icon: <Layers size={22} className="text-emerald-600" />,
    title: 'Zone-Based Pricing',
    desc: 'Dynamic volumetric weight calculation with custom B2B/B2C rate cards and COD surcharge support.'
  },
  {
    icon: <ShieldCheck size={22} className="text-indigo-600" />,
    title: 'Role-Based Access',
    desc: 'Tailored portals for operations Admins, field Agents, and business or retail Customers.'
  },
  {
    icon: <BarChart3 size={22} className="text-purple-600" />,
    title: 'Live Operations KPIs',
    desc: 'Real-time metrics on delivery throughput, pending dispatches, and agent availability.'
  },
  {
    icon: <MapPin size={22} className="text-rose-600" />,
    title: 'Granular Coverage Areas',
    desc: 'Map individual pincodes to delivery zones to dynamically manage urban and suburban delivery corridors.'
  }
];

const steps = [
  {
    step: '01',
    title: 'Create & Quote',
    desc: 'Enter sender and receiver pincodes along with parcel dimensions for instant volumetric pricing.'
  },
  {
    step: '02',
    title: 'Intelligent Dispatch',
    desc: 'The routing engine identifies active fleet agents in the zone and dispatches the pickup automatically.'
  },
  {
    step: '03',
    title: 'Doorstep Delivery',
    desc: 'Monitor live status changes with timestamps until the order is safely delivered to the destination.'
  }
];

export default function LandingPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [trackId, setTrackId] = useState('');

  const dashboardLink =
    user?.role === 'ADMIN' ? '/admin' :
    user?.role === 'AGENT' ? '/agent' :
    user ? '/dashboard' : null;

  const handleTrackSubmit = (e) => {
    e.preventDefault();
    if (!trackId.trim()) return;
    const cleanId = trackId.replace('#', '').trim();
    navigate(`/track/${cleanId}`);
  };

  return (
    <div className="landing-root">
      {/* ── Top Navbar ────────────────────────────────────────── */}
      <nav className="landing-nav">
        <div className="landing-nav-inner">
          <div className="navbar-brand" onClick={() => navigate('/')}>
            <div className="brand-icon-box">
              <Package size={18} strokeWidth={2.4} />
            </div>
            <span>LastMile</span>
          </div>

          <div className="landing-nav-links">
            <a href="#features" className="landing-nav-link">Platform</a>
            <a href="#how-it-works" className="landing-nav-link">How It Works</a>
            <a href="#metrics" className="landing-nav-link">Capabilities</a>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            {dashboardLink ? (
              <Link to={dashboardLink} className="btn btn-primary btn-sm">
                Dashboard <ArrowRight size={14} />
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost btn-sm">
                  Log In
                </Link>
                <Link to="/register" className="btn btn-primary btn-sm">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* ── Hero Section ──────────────────────────────────────── */}
      <section className="landing-hero">
        <div className="landing-hero-badge">
          <span className="pill">NEW</span>
          <span>Intelligent Last-Mile Logistics & Dispatch</span>
        </div>

        <h1 className="landing-hero-title">
          Effortless parcel delivery with <span className="landing-hero-title-highlight">smart dispatch</span>
        </h1>

        <p className="landing-hero-desc">
          Automate agent dispatch, calculate volumetric zone rates, and provide customers with live real-time shipment updates.
        </p>

        {/* ── Quick Tracking Box ──────────────────────────────── */}
        <form className="hero-track-card" onSubmit={handleTrackSubmit}>
          <Search size={18} className="text-slate-400" style={{ marginLeft: 8 }} />
          <input
            type="text"
            className="hero-track-input"
            placeholder="Enter Order ID to track shipment (e.g. 66c8d...)"
            value={trackId}
            onChange={(e) => setTrackId(e.target.value)}
          />
          <button type="submit" className="btn btn-blue btn-sm" style={{ padding: '8px 18px' }}>
            Track Order
          </button>
        </form>

        <div className="landing-hero-actions">
          {dashboardLink ? (
            <Link to={dashboardLink} className="btn btn-primary btn-lg">
              Go to Dashboard <ArrowRight size={16} />
            </Link>
          ) : (
            <>
              <Link to="/register" className="btn btn-primary btn-lg">
                Create Free Account <ArrowRight size={16} />
              </Link>
              <Link to="/login" className="btn btn-secondary btn-lg">
                Sign In to Portal
              </Link>
            </>
          )}
        </div>
      </section>

      {/* ── Metrics Ribbon ────────────────────────────────────── */}
      <section id="metrics" className="landing-metrics">
        <div className="landing-metrics-grid">
          <div>
            <div className="metric-number">99.4%</div>
            <div className="metric-label">On-Time Delivery Rate</div>
          </div>
          <div>
            <div className="metric-number">&lt; 2 min</div>
            <div className="metric-label">Auto-Agent Assignment</div>
          </div>
          <div>
            <div className="metric-number">Multi-Zone</div>
            <div className="metric-label">Dynamic Pincode Routing</div>
          </div>
          <div>
            <div className="metric-number">100% Live</div>
            <div className="metric-label">End-to-End Status Visibility</div>
          </div>
        </div>
      </section>

      {/* ── Features Section ──────────────────────────────────── */}
      <section id="features" className="landing-section">
        <div className="section-header">
          <div className="section-eyebrow">Enterprise Features</div>
          <h2 className="section-title">Built for speed, accuracy, and scale</h2>
          <p className="section-subtitle">
            From automated dispatching to zone pricing and courier routing, manage every stage of your delivery cycle.
          </p>
        </div>

        <div className="features-grid">
          {features.map((item, idx) => (
            <div key={idx} className="feature-box">
              <div className="feature-icon-wrap">
                {item.icon}
              </div>
              <h3 className="feature-title">{item.title}</h3>
              <p className="feature-desc">{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── How It Works ──────────────────────────────────────── */}
      <section id="how-it-works" className="landing-section" style={{ background: '#f8fafc', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)' }}>
        <div className="section-header">
          <div className="section-eyebrow">Seamless Workflow</div>
          <h2 className="section-title">How LastMile works</h2>
          <p className="section-subtitle">
            A frictionless three-step process connecting senders, dispatchers, and field delivery agents.
          </p>
        </div>

        <div className="steps-container">
          {steps.map((s, idx) => (
            <div key={idx} className="step-card-modern">
              <div className="step-number-badge">{s.step}</div>
              <h3 className="feature-title">{s.title}</h3>
              <p className="feature-desc">{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA Banner ────────────────────────────────────────── */}
      <div className="container">
        <section className="landing-cta-banner">
          <h2 className="landing-cta-title">Ready to streamline your deliveries?</h2>
          <p className="landing-cta-desc">
            Sign in as customer, agent, or administrator to experience our intelligent last-mile routing engine.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', gap: 12, flexWrap: 'wrap' }}>
            <Link to="/register" className="btn btn-blue btn-lg" style={{ background: '#ffffff', color: '#0f172a', borderColor: '#ffffff' }}>
              Get Started Free
            </Link>
            <Link to="/login" className="btn btn-ghost btn-lg" style={{ color: '#ffffff', borderColor: 'rgba(255,255,255,0.2)' }}>
              Sign In
            </Link>
          </div>
        </section>
      </div>

      {/* ── Footer ────────────────────────────────────────────── */}
      <footer className="landing-footer">
        <div className="landing-footer-inner">
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div className="brand-icon-box" style={{ width: 28, height: 28, fontSize: 11 }}>
              LM
            </div>
            <span style={{ fontWeight: 700, fontSize: 15 }}>LastMile Delivery</span>
          </div>

          <div className="footer-nav">
            <Link to="/login">Log In</Link>
            <Link to="/register">Register</Link>
            <a href="#features">Features</a>
            <a href="#how-it-works">How It Works</a>
          </div>

          <div className="footer-copy">
            © {new Date().getFullYear()} LastMile Delivery Tracker. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
