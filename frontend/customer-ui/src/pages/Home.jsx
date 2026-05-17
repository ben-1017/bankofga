import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <section style={{ textAlign: 'center', padding: '3rem 1rem' }}>
      <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>Banking, simplified.</h1>
      <p style={{ color: '#52606d', maxWidth: 520, margin: '0 auto 2rem' }}>
        Open accounts, move money, and stay on top of your finances — all from one place.
      </p>
      <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
        <Link
          to="/register"
          style={{
            padding: '0.7rem 1.4rem',
            background: '#1f6feb',
            color: '#fff',
            borderRadius: 6,
            fontWeight: 600,
          }}
        >
          Create an account
        </Link>
        <Link
          to="/login"
          style={{
            padding: '0.7rem 1.4rem',
            background: '#fff',
            color: '#1f2933',
            border: '1px solid #c2cad6',
            borderRadius: 6,
            fontWeight: 600,
          }}
        >
          Log in
        </Link>
      </div>
    </section>
  );
}
