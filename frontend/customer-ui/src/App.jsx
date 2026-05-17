import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from './auth/AuthContext.jsx';

export default function App() {
  const { customer, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div style={{ minHeight: '100vh' }}>
      <header
        style={{
          padding: '1rem 2rem',
          background: '#1f2933',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Link to={isAuthenticated ? '/dashboard' : '/'} style={{ color: '#fff', fontWeight: 700 }}>
          Bank of Georgia
        </Link>
        <nav style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" style={{ color: '#fff' }}>Dashboard</Link>
              <Link to="/accounts" style={{ color: '#fff' }}>Accounts</Link>
              <span style={{ opacity: 0.8 }}>Hi, {customer.name?.split(' ')[0]}</span>
              <button
                onClick={handleLogout}
                style={{
                  background: 'transparent',
                  color: '#fff',
                  border: '1px solid #fff',
                  padding: '0.35rem 0.8rem',
                  borderRadius: 4,
                  cursor: 'pointer',
                }}
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ color: '#fff' }}>Log in</Link>
              <Link to="/register" style={{ color: '#fff' }}>Register</Link>
            </>
          )}
        </nav>
      </header>
      <main style={{ padding: '2rem', maxWidth: 960, margin: '0 auto' }}>
        <Outlet />
      </main>
    </div>
  );
}
