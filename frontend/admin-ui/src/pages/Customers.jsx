import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { listCustomers } from '../api/admin.js';
import { getErrorMessage } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import { shortId } from '../utils/format.js';

export default function Customers() {
  const [customers, setCustomers] = useState([]);
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadCustomers() {
      setIsLoading(true);
      setError('');

      try {
        setCustomers(await listCustomers());
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setIsLoading(false);
      }
    }

    loadCustomers();
  }, []);

  const filteredCustomers = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) {
      return customers;
    }

    return customers.filter((customer) => (
      customer.name?.toLowerCase().includes(term)
      || customer.email?.toLowerCase().includes(term)
      || customer.username?.toLowerCase().includes(term)
      || customer.phone?.toLowerCase().includes(term)
    ));
  }, [customers, search]);

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Customers</h1>
        <p className="text-sm text-gray-600">Search registered customers and open their account profile.</p>
      </div>

      {error && (
        <Alert variant="error" title="Customers unavailable" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Total customers</div>
          <div className="mt-2 text-2xl font-semibold">{customers.length}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Visible after search</div>
          <div className="mt-2 text-2xl font-semibold">{filteredCustomers.length}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Source</div>
          <div className="mt-2 text-sm font-semibold text-gray-700">Employee customer API</div>
        </div>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
        <div className="border-b border-gray-200 p-4">
          <label className="block">
            <span className="sr-only">Search customers</span>
            <input
              className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search name, email, username, or phone"
            />
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th scope="col" className="px-4 py-3">Customer</th>
                <th scope="col" className="px-4 py-3">Username</th>
                <th scope="col" className="px-4 py-3">Phone</th>
                <th scope="col" className="px-4 py-3">Customer ID</th>
                <th scope="col" className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {isLoading && (
                <tr>
                  <td colSpan="5" className="px-4 py-8 text-center text-gray-500">Loading customers...</td>
                </tr>
              )}
              {!isLoading && filteredCustomers.length === 0 && (
                <tr>
                  <td colSpan="5" className="px-4 py-8 text-center text-gray-500">No customers match the current search.</td>
                </tr>
              )}
              {!isLoading && filteredCustomers.map((customer) => (
                <tr key={customer.id} className="hover:bg-gray-50">
                  <td className="px-4 py-4">
                    <div className="font-semibold text-gray-900">{customer.name}</div>
                    <div className="mt-1 text-xs text-gray-500">{customer.email}</div>
                  </td>
                  <td className="px-4 py-4 text-gray-700">{customer.username}</td>
                  <td className="px-4 py-4 text-gray-700">{customer.phone}</td>
                  <td className="px-4 py-4 font-mono text-xs text-gray-500">{shortId(customer.id)}</td>
                  <td className="px-4 py-4 text-right">
                    <Link
                      to={`/customers/${customer.id}`}
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
    </section>
  );
}
