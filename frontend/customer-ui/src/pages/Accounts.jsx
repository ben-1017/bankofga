import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { accountsApi } from '../api/accounts.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { AccountTable } from './Dashboard.jsx';

export default function Accounts() {
  const { customer } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    accountsApi
      .listByCustomer(customer.id)
      .then((data) => !cancelled && setAccounts(data))
      .catch((err) => !cancelled && setError(extractApiError(err, 'Could not load accounts')))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [customer.id]);

  return (
    <section>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Your accounts</h1>
        <Link to="/accounts/new" style={{ padding: '0.5rem 1rem', background: '#1f6feb', color: '#fff', borderRadius: 6, fontWeight: 600 }}>
          Open new account
        </Link>
      </div>
      <Alert kind="error">{error}</Alert>
      {loading ? (
        <p>Loading...</p>
      ) : accounts.length === 0 ? (
        <p>No accounts yet. <Link to="/accounts/new">Open one</Link>.</p>
      ) : (
        <AccountTable accounts={accounts} />
      )}
    </section>
  );
}
