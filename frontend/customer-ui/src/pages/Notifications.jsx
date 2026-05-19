import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../auth/AuthContext.jsx';
import { notificationsApi } from '../api/notifications.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { inputStyle } from '../components/Field.jsx';

function TypeChip({ type }) {
  const palette = {
    LOW_BALANCE_NOTIFICATION: { bg: '#fde2e1', fg: '#7a1f1c' },
    WITHDRAW_NOTIFICATION: { bg: '#fff4cc', fg: '#7a5b00' },
    DEPOSIT_NOTIFICATION: { bg: '#d8f5e1', fg: '#155724' },
    APPLY_MONTHLY_FEE: { bg: '#e3f0ff', fg: '#1d3a72' },
  }[type] || { bg: '#e4e9f0', fg: '#52606d' };
  return (
    <span style={{
      background: palette.bg, color: palette.fg, padding: '0.15rem 0.55rem',
      borderRadius: 999, fontSize: 12, fontWeight: 600,
    }}>
      {type.replaceAll('_', ' ')}
    </span>
  );
}

export default function Notifications() {
  const { customer } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [channelFilter, setChannelFilter] = useState('ALL');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    notificationsApi.listByCustomer(customer.id)
      .then((list) => {
        if (cancelled) return;
        const sorted = [...list].sort((x, y) => new Date(y.createdAt) - new Date(x.createdAt));
        setNotifications(sorted);
      })
      .catch((err) => !cancelled && setError(extractApiError(err, 'Could not load notifications')))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [customer.id]);

  const filtered = useMemo(() => {
    if (channelFilter === 'ALL') return notifications;
    return notifications.filter((n) => n.channel === channelFilter);
  }, [notifications, channelFilter]);

  if (loading) return <p>Loading notifications...</p>;
  if (error) return <Alert kind="error">{error}</Alert>;

  return (
    <section>
      <h1>Notifications</h1>
      <p style={{ color: '#52606d' }}>Account activity and alerts sent to you.</p>

      <div style={{ margin: '1rem 0' }}>
        <select
          style={{ ...inputStyle, width: 'auto' }}
          value={channelFilter}
          onChange={(e) => setChannelFilter(e.target.value)}
          aria-label="Filter by channel"
        >
          <option value="ALL">All channels</option>
          <option value="EMAIL">Email</option>
          <option value="SMS">SMS</option>
        </select>
      </div>

      {filtered.length === 0 ? (
        <p>No notifications to show.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
          {filtered.map((n) => (
            <li
              key={n.id}
              style={{
                background: '#fff',
                padding: '1rem 1.25rem',
                borderRadius: 8,
                boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
                marginBottom: '0.75rem',
                borderLeft: `4px solid ${n.delivered ? '#1f6feb' : '#c2cad6'}`,
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem', flexWrap: 'wrap' }}>
                <div>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: 4 }}>
                    <strong>{n.subject || '(no subject)'}</strong>
                    <TypeChip type={n.type} />
                    <span style={{ fontSize: 12, color: '#52606d' }}>{n.channel}</span>
                  </div>
                  <div style={{ color: '#3e4c59', whiteSpace: 'pre-wrap' }}>{n.body}</div>
                </div>
                <div style={{ textAlign: 'right', fontSize: 13, color: '#52606d', whiteSpace: 'nowrap' }}>
                  {new Date(n.createdAt).toLocaleString()}
                  <div style={{ marginTop: 4 }}>
                    {n.delivered ? (
                      <span style={{ color: '#155724' }}>Delivered</span>
                    ) : (
                      <span style={{ color: '#7a5b00' }}>Pending</span>
                    )}
                  </div>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
