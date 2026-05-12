import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { listAccounts } from '../api/admin.js';
import { getErrorMessage } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { formatDateTime, formatLabel, formatMoney, shortId } from '../utils/format.js';

const PAGE_SIZE = 20;
const STATUSES = ['ACTIVE', 'FROZEN', 'CLOSED'];

function statusClass(status) {
  if (status === 'ACTIVE') {
    return 'bg-green-100 text-green-800';
  }
  if (status === 'FROZEN') {
    return 'bg-amber-100 text-amber-800';
  }
  return 'bg-gray-100 text-gray-700';
}

export default function Accounts() {
  const [page, setPage] = useState(0);
  const [response, setResponse] = useState(null);
  const [filters, setFilters] = useState({ search: '', status: 'ALL' });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadAccounts() {
      setIsLoading(true);
      setError('');

      try {
        setResponse(await listAccounts({ page, size: PAGE_SIZE }));
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setIsLoading(false);
      }
    }

    loadAccounts();
  }, [page]);

  const accounts = response?.content || [];
  const filteredAccounts = useMemo(() => {
    const term = filters.search.trim().toLowerCase();

    return accounts.filter((account) => {
      const matchesSearch = !term
        || account.accountNumber?.toLowerCase().includes(term)
        || account.customerId?.toLowerCase().includes(term)
        || account.productId?.toLowerCase().includes(term);
      const matchesStatus = filters.status === 'ALL' || account.status === filters.status;
      return matchesSearch && matchesStatus;
    });
  }, [accounts, filters]);

  const totalElements = response?.totalElements ?? accounts.length;
  const totalPages = response?.totalPages ?? 1;
  const canGoBack = page > 0;
  const canGoForward = page + 1 < totalPages;

  function updateFilter(event) {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  }

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Accounts</h1>
        <p className="text-sm text-gray-600">Review account balances, statuses, and customer ownership.</p>
      </div>

      {error && (
        <Alert variant="error" title="Accounts unavailable" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Total accounts</div>
          <div className="mt-2 text-2xl font-semibold">{totalElements}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Current page</div>
          <div className="mt-2 text-2xl font-semibold">{page + 1} / {Math.max(totalPages, 1)}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Visible rows</div>
          <div className="mt-2 text-2xl font-semibold">{filteredAccounts.length}</div>
        </div>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
        <div className="grid gap-3 border-b border-gray-200 p-4 md:grid-cols-[1fr_180px]">
          <label className="block">
            <span className="sr-only">Search accounts</span>
            <input
              className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
              name="search"
              value={filters.search}
              onChange={updateFilter}
              placeholder="Search account, customer, or product id"
            />
          </label>
          <label className="block">
            <span className="sr-only">Status</span>
            <select
              className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
              name="status"
              value={filters.status}
              onChange={updateFilter}
            >
              <option value="ALL">All statuses</option>
              {STATUSES.map((status) => (
                <option key={status} value={status}>{formatLabel(status)}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th scope="col" className="px-4 py-3">Account</th>
                <th scope="col" className="px-4 py-3">Customer</th>
                <th scope="col" className="px-4 py-3">Product</th>
                <th scope="col" className="px-4 py-3">Balance</th>
                <th scope="col" className="px-4 py-3">Status</th>
                <th scope="col" className="px-4 py-3">Opened</th>
                <th scope="col" className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {isLoading && (
                <tr>
                  <td colSpan="7" className="px-4 py-8 text-center text-gray-500">Loading accounts...</td>
                </tr>
              )}
              {!isLoading && filteredAccounts.length === 0 && (
                <tr>
                  <td colSpan="7" className="px-4 py-8 text-center text-gray-500">No accounts match the current filters.</td>
                </tr>
              )}
              {!isLoading && filteredAccounts.map((account) => (
                <tr key={account.id} className="hover:bg-gray-50">
                  <td className="px-4 py-4">
                    <div className="font-semibold text-gray-900">{account.accountNumber}</div>
                    <div className="mt-1 font-mono text-xs text-gray-500">{shortId(account.id)}</div>
                  </td>
                  <td className="px-4 py-4">
                    <Link to={`/customers/${account.customerId}`} className="font-mono text-xs text-brand-accent hover:text-blue-700">
                      {shortId(account.customerId)}
                    </Link>
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

        <div className="flex flex-wrap items-center justify-between gap-3 border-t border-gray-200 px-4 py-3 text-sm text-gray-600">
          <div>Page {page + 1} of {Math.max(totalPages, 1)}</div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setPage((current) => Math.max(current - 1, 0))}
              disabled={!canGoBack || isLoading}
              className="rounded-md border border-gray-300 px-3 py-1.5 font-semibold text-gray-700 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Previous
            </button>
            <button
              type="button"
              onClick={() => setPage((current) => current + 1)}
              disabled={!canGoForward || isLoading}
              className="rounded-md border border-gray-300 px-3 py-1.5 font-semibold text-gray-700 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
