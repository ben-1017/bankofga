import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { productsApi } from '../api/products.js';
import { accountsApi } from '../api/accounts.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import Field, { inputStyle, buttonStyle, buttonSecondaryStyle } from '../components/Field.jsx';

export default function OpenAccount() {
  const { customer } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [productId, setProductId] = useState('');
  const [initialDeposit, setInitialDeposit] = useState('0');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    productsApi
      .list()
      .then((list) => {
        if (cancelled) return;
        const active = list.filter((p) => p.active);
        setProducts(active);
        if (active.length > 0) setProductId(active[0].id);
      })
      .catch((err) => !cancelled && setServerError(extractApiError(err, 'Could not load products')))
      .finally(() => !cancelled && setLoadingProducts(false));
    return () => { cancelled = true; };
  }, []);

  const selected = products.find((p) => p.id === productId);

  const onSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    const v = {};
    if (!productId) v.productId = 'Choose a product';
    const deposit = Number(initialDeposit);
    if (Number.isNaN(deposit) || deposit < 0) v.initialDeposit = 'Enter a non-negative amount';
    if (selected && deposit < Number(selected.minimumBalance ?? 0)) {
      v.initialDeposit = `Minimum opening deposit is $${selected.minimumBalance}`;
    }
    setErrors(v);
    if (Object.keys(v).length > 0) return;

    setSubmitting(true);
    try {
      const account = await accountsApi.open({
        customerId: customer.id,
        productId,
        initialDeposit: deposit,
      });
      navigate(`/accounts/${account.id}`, { replace: true });
    } catch (err) {
      setServerError(extractApiError(err, 'Could not open account'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section style={{ maxWidth: 520 }}>
      <p style={{ marginBottom: '0.5rem' }}>
        <Link to="/accounts">&larr; All accounts</Link>
      </p>
      <h1>Open a new account</h1>
      <Alert kind="error">{serverError}</Alert>

      {loadingProducts ? (
        <p>Loading products...</p>
      ) : products.length === 0 ? (
        <p>No products are currently available.</p>
      ) : (
        <form onSubmit={onSubmit} noValidate>
          <Field label="Product" error={errors.productId}>
            <select
              style={inputStyle}
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
            >
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.type})
                </option>
              ))}
            </select>
          </Field>

          {selected && (
            <div style={{ background: '#eef2f7', padding: '0.75rem 1rem', borderRadius: 6, marginBottom: '0.85rem', fontSize: 14 }}>
              <div><strong>Minimum balance:</strong> ${Number(selected.minimumBalance ?? 0).toFixed(2)}</div>
              <div><strong>Monthly fee:</strong> ${Number(selected.monthlyFee ?? 0).toFixed(2)}</div>
              <div><strong>Interest rate:</strong> {(Number(selected.interestRate ?? 0) * 100).toFixed(2)}%</div>
              {selected.description && <div style={{ marginTop: 4, color: '#52606d' }}>{selected.description}</div>}
            </div>
          )}

          <Field label="Initial deposit (USD)" error={errors.initialDeposit}>
            <input
              style={inputStyle}
              type="number"
              min="0"
              step="0.01"
              value={initialDeposit}
              onChange={(e) => setInitialDeposit(e.target.value)}
            />
          </Field>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button type="submit" style={buttonStyle} disabled={submitting}>
              {submitting ? 'Opening...' : 'Open account'}
            </button>
            <Link to="/accounts" style={{ ...buttonSecondaryStyle, textDecoration: 'none', display: 'inline-block' }}>
              Cancel
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
