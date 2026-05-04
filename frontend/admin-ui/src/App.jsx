import { Outlet, Link } from 'react-router-dom';

export default function App() {
  return (
    <div style={{ minHeight: '100vh' }}>
      <header style={{ padding: '1rem 2rem', background: '#0f1d2c', color: '#fff' }}>
        <Link to="/" style={{ color: '#fff', fontWeight: 700 }}>Bank of Georgia — Admin</Link>
      </header>
      <main style={{ padding: '2rem' }}>
        <Outlet />
      </main>
    </div>
  );
}
