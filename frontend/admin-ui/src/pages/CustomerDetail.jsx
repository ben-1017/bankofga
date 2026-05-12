import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getCustomer, listCustomerAccounts } from '../api/admin.js';
import { getErrorMessage } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { formatDateTime, formatLabel, formatMoney, shortId } from '../utils/format.js';

function statusClass(status) {
  if (status === 'ACTIVE') {
    return 'bg-green-100 text-green-800';
  }
  if (status === 'FROZEN') {
    return 'bg-amber-100 text-amber-800';
  }
  return 'bg-gray-100 text-gray-700';
}

export default function CustomerDetail() {
  const { id } = useParams();
  const [customer, setCustomer] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadCustomerDetail() {
      setIsLoading(true);
      setError('');

      try {
        const [nextCustomer, nextAccounts] = await Promise.all([
          getCustomer(id),
          listCustomerAccounts(id),
        ]);

        if (!ignore) {
          setCustomer(nextCustomer);
          setAccounts(nextAccounts);
        }
      } catch (err) {
        if (!ignore) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (!ignore) {
          setIsLoading(false);
        }
      }
    }

    loadCustomerDetail();

    return () => {
      ignore = true;
    };
  }, [id]);

  const totalBalance = accounts.reduce((sum, account) => sum + Number(account.balance ?? 0), 0);

  return (
    <section className="space-y-6">
      <div>
        <Link to="/customers" className="text-sm font-medium text-brand-accent hover:text-blue-700">
          Back to customers
        </Link>
        <h1 className="mt-3 text-2xl font-semibold tracking-tight">
          {customer?.name || 'Customer detail'}
        </h1>
        <p className="text-sm text-gray-600">Customer profile, owned accounts, and balances.</p>
      </div>

      {error && (
        <Alert variant="error" title="Customer unavailable" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      {isLoading ? (
        <div className="rounded-lg border border-gray-200 bg-white p-8 text-center text-sm text-gray-500 shadow-sm">
          Loading customer...
        </div>
      ) : (
        <>
          <div className="grid gap-4 lg:grid-cols-[1fr_320px]">
            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="text-lg font-semibold">Profile</h2>
              <dl className="mt-5 grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Email</dt>
                  <dd className="mt-1 text-sm text-gray-900">{customer?.email}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Username</dt>
                  <dd className="mt-1 text-sm text-gray-900">{customer?.username}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Phone</dt>
                  <dd className="mt-1 text-sm text-gray-900">{customer?.phone}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Customer ID</dt>
                  <dd className="mt-1 font-mono text-xs text-gray-700">{customer?.id}</dd>
                </div>
              </dl>
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="text-lg font-semibold">Account summary</h2>
              <dl className="mt-5 space-y-4">
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Accounts</dt>
                  <dd className="mt-1 text-2xl font-semibold">{accounts.length}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Total balance</dt>
                  <dd className="mt-1 text-2xl font-semibold text-green-700">{formatMoney(totalBalance)}</dd>
                </div>
              </dl>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
            <div className="border-b border-gray-200 p-4">
              <h2 className="text-lg font-semibold">Accounts</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                  <tr>
                    <th scope="col" className="px-4 py-3">Account</th>
                    <th scope="col" className="px-4 py-3">Product</th>
                    <th scope="col" className="px-4 py-3">Balance</th>
                    <th scope="col" className="px-4 py-3">Status</th>
                    <th scope="col" className="px-4 py-3">Opened</th>
                    <th scope="col" className="px-4 py-3 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white">
                  {accounts.length === 0 && (
                    <tr>
                      <td colSpan="6" className="px-4 py-8 text-center text-gray-500">This customer has no accounts yet.</td>
                    </tr>
                  )}
                  {accounts.map((account) => (
                    <tr key={account.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4">
                        <div className="font-semibold text-gray-900">{account.accountNumber}</div>
                        <div className="mt-1 font-mono text-xs text-gray-500">{shortId(account.id)}</div>
                      </td>
                      <td className="px-4 py-4 font-mono text-xs text-gray-500">{shortId(account.productId)}</td>
                      <td className="px-4 py-4 font-semibold text-gray-900">{formatMoney(account.balance)}</td>
                      <td className="px-4 py-4">
                        <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(account.status)}`}>
                          {formatLabel(account.status)}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-gray-700">{formatDateTime(account.openedAt)}</td>
                      <td className="px-4 py-4 text-right">
                        <Link
                          to={`/accounts/${account.id}`}
                          className="rounded-md border border-gray-300 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:bg-gray-100"
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </section>
  );
}
