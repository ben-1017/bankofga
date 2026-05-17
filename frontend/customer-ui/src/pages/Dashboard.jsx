import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { accountsApi } from '../api/accounts.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';

function formatMoney(amount) {
  const n = Number(amount ?? 0);
  return n.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export default function Dashboard() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    accountsApi
      .listByCustomer(customer.id)
      .then((data) => {
        if (!cancelled) setAccounts(data);
      })
      .catch((err) => {
        if (!cancelled) setError(extractApiError(err, 'Could not load accounts'));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [customer.id]);

  const totalBalance = accounts
    .filter((a) => a.status === 'ACTIVE')
    .reduce((sum, a) => sum + Number(a.balance ?? 0), 0);

  return (
    <section>
      <h1>Welcome back, {customer.name?.split(' ')[0]}</h1>
      <p style={{ color: '#52606d' }}>Here's a snapshot of your accounts.</p>

      <div
        style={{
          background: '#fff',
          padding: '1.5rem',
          borderRadius: 8,
          boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
          margin: '1.5rem 0',
        }}
      >
        <div style={{ color: '#52606d', fontSize: 14 }}>Total balance (active accounts)</div>
        <div style={{ fontSize: 32, fontWeight: 700, marginTop: 4 }}>{formatMoney(totalBalance)}</div>
        <div style={{ marginTop: 8, color: '#52606d' }}>
          {accounts.length} {accounts.length === 1 ? 'account' : 'accounts'}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
        <Link to="/accounts" style={linkBtn}>View all accounts</Link>
        <Link to="/accounts/new" style={primaryLink}>Open new account</Link>
      </div>

      <Alert kind="error">{error}</Alert>
      {loading ? (
        <p>Loading accounts...</p>
      ) : accounts.length === 0 ? (
        <p>You don't have any accounts yet. <Link to="/accounts/new">Open one</Link>.</p>
      ) : (
        <AccountTable accounts={accounts.slice(0, 5)} />
      )}
    </section>
  );
}

export function AccountTable({ accounts }) {
  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' }}>
      <thead style={{ background: '#eef2f7' }}>
        <tr>
          <th style={th}>Account #</th>
          <th style={th}>Status</th>
          <th style={{ ...th, textAlign: 'right' }}>Balance</th>
          <th style={th}></th>
        </tr>
      </thead>
      <tbody>
        {accounts.map((a) => (
          <tr key={a.id} style={{ borderTop: '1px solid #e4e9f0' }}>
            <td style={td}>{a.accountNumber}</td>
            <td style={td}><StatusBadge status={a.status} /></td>
            <td style={{ ...td, textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>
              {Number(a.balance ?? 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
            </td>
            <td style={{ ...td, textAlign: 'right' }}>
              <Link to={`/accounts/${a.id}`}>View</Link>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export function StatusBadge({ status }) {
  const color = status === 'ACTIVE' ? '#155724' : status === 'CLOSED' ? '#7a1f1c' : '#52606d';
  const bg = status === 'ACTIVE' ? '#d8f5e1' : status === 'CLOSED' ? '#fde2e1' : '#e4e9f0';
  return (
    <span style={{ background: bg, color, padding: '0.15rem 0.55rem', borderRadius: 999, fontSize: 12, fontWeight: 600 }}>
      {status}
    </span>
  );
}

const th = { textAlign: 'left', padding: '0.6rem 0.9rem', fontSize: 13, color: '#52606d', textTransform: 'uppercase', letterSpacing: 0.5 };
const td = { padding: '0.65rem 0.9rem', fontSize: 15 };
const linkBtn = {
  display: 'inline-block',
  padding: '0.5rem 1rem',
  background: '#fff',
  color: '#1f2933',
  border: '1px solid #c2cad6',
  borderRadius: 6,
  fontWeight: 600,
};
const primaryLink = {
  display: 'inline-block',
  padding: '0.5rem 1rem',
  background: '#1f6feb',
  color: '#fff',
  borderRadius: 6,
  fontWeight: 600,
};
