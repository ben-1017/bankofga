import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { accountsApi } from '../api/accounts.js';
import { transactionsApi } from '../api/transactions.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { inputStyle } from '../components/Field.jsx';

const PAGE_SIZE = 10;

function formatMoney(amount) {
  return Number(amount ?? 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

function TypeBadge({ type }) {
  const palette = {
    DEPOSIT: { bg: '#d8f5e1', fg: '#155724' },
    WITHDRAWAL: { bg: '#fde2e1', fg: '#7a1f1c' },
    FEE: { bg: '#fff4cc', fg: '#7a5b00' },
    INTEREST: { bg: '#e3f0ff', fg: '#1d3a72' },
  }[type] || { bg: '#e4e9f0', fg: '#52606d' };
  return (
    <span style={{
      background: palette.bg, color: palette.fg, padding: '0.15rem 0.55rem',
      borderRadius: 999, fontSize: 12, fontWeight: 600,
    }}>
      {type}
    </span>
  );
}

export default function TransactionHistory() {
  const { id: accountId } = useParams();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [typeFilter, setTypeFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    Promise.all([accountsApi.get(accountId), transactionsApi.listByAccount(accountId)])
      .then(([a, txs]) => {
        if (cancelled) return;
        setAccount(a);
        const sorted = [...txs].sort((x, y) => new Date(y.createdAt) - new Date(x.createdAt));
        setTransactions(sorted);
      })
      .catch((err) => !cancelled && setError(extractApiError(err, 'Could not load transactions')))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [accountId]);

  const filtered = useMemo(() => {
    const needle = search.trim().toLowerCase();
    return transactions.filter((t) => {
      if (typeFilter !== 'ALL' && t.type !== typeFilter) return false;
      if (needle && !(t.description || '').toLowerCase().includes(needle)) return false;
      return true;
    });
  }, [transactions, typeFilter, search]);

  useEffect(() => { setPage(1); }, [typeFilter, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, totalPages);
  const pageItems = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  if (loading) return <p>Loading transactions...</p>;
  if (error) return <Alert kind="error">{error}</Alert>;

  return (
    <section>
      <p style={{ marginBottom: '0.5rem' }}>
        <Link to={`/accounts/${accountId}`}>&larr; Back to account</Link>
      </p>
      <h1 style={{ marginBottom: 4 }}>Transactions</h1>
      {account && (
        <p style={{ color: '#52606d', marginTop: 0 }}>
          Account {account.accountNumber} &middot; balance {formatMoney(account.balance)}
        </p>
      )}

      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', margin: '1rem 0' }}>
        <select
          style={{ ...inputStyle, width: 'auto' }}
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          aria-label="Filter by type"
        >
          <option value="ALL">All types</option>
          <option value="DEPOSIT">Deposits</option>
          <option value="WITHDRAWAL">Withdrawals</option>
          <option value="FEE">Fees</option>
          <option value="INTEREST">Interest</option>
        </select>
        <input
          style={{ ...inputStyle, width: 'auto', flex: 1, minWidth: 200 }}
          type="search"
          placeholder="Search description..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search description"
        />
      </div>

      {filtered.length === 0 ? (
        <p>No transactions match the current filters.</p>
      ) : (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.08)' }}>
            <thead style={{ background: '#eef2f7' }}>
              <tr>
                <th style={th}>Date</th>
                <th style={th}>Type</th>
                <th style={th}>Description</th>
                <th style={{ ...th, textAlign: 'right' }}>Amount</th>
                <th style={{ ...th, textAlign: 'right' }}>Balance after</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((t) => (
                <tr key={t.id} style={{ borderTop: '1px solid #e4e9f0' }}>
                  <td style={td}>{new Date(t.createdAt).toLocaleString()}</td>
                  <td style={td}><TypeBadge type={t.type} /></td>
                  <td style={td}>{t.description || <span style={{ color: '#7e8b9b' }}>&mdash;</span>}</td>
                  <td style={{ ...td, textAlign: 'right', fontVariantNumeric: 'tabular-nums', color: t.type === 'DEPOSIT' || t.type === 'INTEREST' ? '#155724' : '#7a1f1c' }}>
                    {t.type === 'DEPOSIT' || t.type === 'INTEREST' ? '+' : '-'}{formatMoney(t.amount)}
                  </td>
                  <td style={{ ...td, textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>{formatMoney(t.balanceAfter)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
            <span style={{ color: '#52606d', fontSize: 14 }}>
              Page {safePage} of {totalPages} &middot; {filtered.length} transaction{filtered.length === 1 ? '' : 's'}
            </span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={safePage <= 1} style={pageBtn}>Prev</button>
              <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={safePage >= totalPages} style={pageBtn}>Next</button>
            </div>
          </div>
        </>
      )}
    </section>
  );
}

const th = { textAlign: 'left', padding: '0.6rem 0.9rem', fontSize: 13, color: '#52606d', textTransform: 'uppercase', letterSpacing: 0.5 };
const td = { padding: '0.65rem 0.9rem', fontSize: 15 };
const pageBtn = {
  padding: '0.4rem 0.9rem',
  background: '#fff',
  color: '#1f2933',
  border: '1px solid #c2cad6',
  borderRadius: 6,
  cursor: 'pointer',
  fontWeight: 600,
};
