import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { accountsApi } from '../api/accounts.js';
import { productsApi } from '../api/products.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { StatusBadge } from './Dashboard.jsx';

export default function AccountDetail() {
  const { id } = useParams();
  const [account, setAccount] = useState(null);
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    accountsApi
      .get(id)
      .then(async (a) => {
        if (cancelled) return;
        setAccount(a);
        try {
          const p = await productsApi.get(a.productId);
          if (!cancelled) setProduct(p);
        } catch {
          /* product lookup is best-effort */
        }
      })
      .catch((err) => !cancelled && setError(extractApiError(err, 'Could not load account')))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [id]);

  if (loading) return <p>Loading account...</p>;
  if (error) return <Alert kind="error">{error}</Alert>;
  if (!account) return null;

  return (
    <section>
      <p style={{ marginBottom: '0.5rem' }}>
        <Link to="/accounts">&larr; All accounts</Link>
      </p>
      <h1 style={{ marginBottom: 4 }}>Account {account.accountNumber}</h1>
      <div style={{ marginBottom: '1rem' }}>
        <StatusBadge status={account.status} />
      </div>

      <div
        style={{
          background: '#fff',
          padding: '1.5rem',
          borderRadius: 8,
          boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
          marginBottom: '1.5rem',
        }}
      >
        <div style={{ color: '#52606d', fontSize: 14 }}>Current balance</div>
        <div style={{ fontSize: 32, fontWeight: 700, marginTop: 4 }}>
          {Number(account.balance ?? 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
        <Link
          to={`/accounts/${account.id}/deposit`}
          style={{ ...actionPrimary, pointerEvents: account.status === 'ACTIVE' ? 'auto' : 'none', opacity: account.status === 'ACTIVE' ? 1 : 0.5 }}
        >
          Deposit
        </Link>
        <Link
          to={`/accounts/${account.id}/withdraw`}
          style={{ ...actionSecondary, pointerEvents: account.status === 'ACTIVE' ? 'auto' : 'none', opacity: account.status === 'ACTIVE' ? 1 : 0.5 }}
        >
          Withdraw
        </Link>
        <Link to={`/accounts/${account.id}/transactions`} style={actionSecondary}>
          Transaction history
        </Link>
      </div>

      <dl style={{ background: '#fff', padding: '1rem 1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.08)' }}>
        <Row label="Account ID" value={account.id} />
        <Row label="Product" value={product ? `${product.name} (${product.code})` : account.productId} />
        <Row label="Opened" value={new Date(account.openedAt).toLocaleDateString()} />
      </dl>
    </section>
  );
}

const actionPrimary = {
  display: 'inline-block',
  padding: '0.55rem 1.1rem',
  background: '#1f6feb',
  color: '#fff',
  borderRadius: 6,
  fontWeight: 600,
  textDecoration: 'none',
};

const actionSecondary = {
  display: 'inline-block',
  padding: '0.55rem 1.1rem',
  background: '#fff',
  color: '#1f2933',
  border: '1px solid #c2cad6',
  borderRadius: 6,
  fontWeight: 600,
  textDecoration: 'none',
};

function Row({ label, value }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 0', borderBottom: '1px solid #eef2f7' }}>
      <dt style={{ color: '#52606d' }}>{label}</dt>
      <dd style={{ margin: 0, fontWeight: 500 }}>{value}</dd>
    </div>
  );
}
