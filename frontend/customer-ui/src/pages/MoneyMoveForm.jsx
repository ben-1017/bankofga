import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { accountsApi } from '../api/accounts.js';
import { transactionsApi } from '../api/transactions.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import Field, { inputStyle, buttonStyle, buttonSecondaryStyle } from '../components/Field.jsx';
import { StatusBadge } from './Dashboard.jsx';

const COPY = {
  deposit: {
    title: 'Deposit funds',
    cta: 'Deposit',
    submitting: 'Depositing...',
    successPrefix: 'Deposited',
    apiFn: (payload) => transactionsApi.deposit(payload),
  },
  withdraw: {
    title: 'Withdraw funds',
    cta: 'Withdraw',
    submitting: 'Withdrawing...',
    successPrefix: 'Withdrew',
    apiFn: (payload) => transactionsApi.withdraw(payload),
  },
};

function formatMoney(amount) {
  return Number(amount ?? 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export default function MoneyMoveForm({ kind }) {
  const copy = COPY[kind];
  const { id: accountId } = useParams();
  const navigate = useNavigate();

  const [account, setAccount] = useState(null);
  const [loadingAccount, setLoadingAccount] = useState(true);
  const [accountError, setAccountError] = useState('');

  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoadingAccount(true);
    accountsApi
      .get(accountId)
      .then((a) => !cancelled && setAccount(a))
      .catch((err) => !cancelled && setAccountError(extractApiError(err, 'Could not load account')))
      .finally(() => !cancelled && setLoadingAccount(false));
    return () => { cancelled = true; };
  }, [accountId]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccess('');

    const v = {};
    const numeric = Number(amount);
    if (!amount.trim()) v.amount = 'Amount is required';
    else if (Number.isNaN(numeric) || numeric <= 0) v.amount = 'Enter an amount greater than zero';
    else if (kind === 'withdraw' && account && numeric > Number(account.balance ?? 0)) {
      v.amount = `Insufficient funds (balance: ${formatMoney(account.balance)})`;
    }
    setErrors(v);
    if (Object.keys(v).length > 0) return;

    setSubmitting(true);
    try {
      const tx = await copy.apiFn({
        accountId,
        amount: numeric,
        description: description.trim() || null,
      });
      setSuccess(`${copy.successPrefix} ${formatMoney(tx.amount)}. New balance: ${formatMoney(tx.balanceAfter)}.`);
      setAmount('');
      setDescription('');
      setAccount((a) => (a ? { ...a, balance: tx.balanceAfter } : a));
    } catch (err) {
      setServerError(extractApiError(err, `Could not ${kind}`));
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingAccount) return <p>Loading account...</p>;
  if (accountError) return <Alert kind="error">{accountError}</Alert>;
  if (!account) return null;

  return (
    <section style={{ maxWidth: 520 }}>
      <p style={{ marginBottom: '0.5rem' }}>
        <Link to={`/accounts/${accountId}`}>&larr; Back to account</Link>
      </p>
      <h1>{copy.title}</h1>
      <div style={{ marginBottom: '1rem', color: '#52606d' }}>
        Account {account.accountNumber} <StatusBadge status={account.status} />
        <div style={{ marginTop: 4 }}>Current balance: <strong>{formatMoney(account.balance)}</strong></div>
      </div>

      <Alert kind="error">{serverError}</Alert>
      <Alert kind="success">{success}</Alert>

      <form onSubmit={onSubmit} noValidate>
        <Field label="Amount (USD)" error={errors.amount}>
          <input
            style={inputStyle}
            type="number"
            min="0"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            autoFocus
          />
        </Field>
        <Field label="Description (optional)">
          <input
            style={inputStyle}
            type="text"
            maxLength={255}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Paycheck, Rent, etc."
          />
        </Field>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button type="submit" style={buttonStyle} disabled={submitting || account.status !== 'ACTIVE'}>
            {submitting ? copy.submitting : copy.cta}
          </button>
          <Link to={`/accounts/${accountId}`} style={{ ...buttonSecondaryStyle, textDecoration: 'none', display: 'inline-block' }}>
            Cancel
          </Link>
        </div>
        {account.status !== 'ACTIVE' && (
          <p style={{ marginTop: '0.75rem', color: '#7a1f1c', fontSize: 14 }}>
            This account is {account.status.toLowerCase()} and cannot transact.
          </p>
        )}
      </form>
    </section>
  );
}
