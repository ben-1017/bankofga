import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { getErrorMessage } from '../api/client.js';
import { listProducts, setProductStatus } from '../api/admin.js';
import Alert from '../components/Alert.jsx';

const PRODUCT_TYPES = [
  'CHECKING',
  'SAVINGS',
  'CD',
  'BUSINESS_CHECKING',
  'STUDENT_SAVINGS',
];

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

function formatPercent(value) {
  const amount = Number(value ?? 0);
  return `${(amount * 100).toFixed(2)}%`;
}

function formatType(type) {
  return type?.replaceAll('_', ' ') || 'Unknown';
}

export default function Products() {
  const location = useLocation();
  const [products, setProducts] = useState([]);
  const [filters, setFilters] = useState({ search: '', status: 'ALL', type: 'ALL' });
  const [isLoading, setIsLoading] = useState(true);
  const [busyId, setBusyId] = useState('');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(location.state?.notice || '');

  async function loadProducts() {
    setIsLoading(true);
    setError('');

    try {
      setProducts(await listProducts());
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadProducts();
  }, []);

  const filteredProducts = useMemo(() => {
    const search = filters.search.trim().toLowerCase();
    return products.filter((product) => {
      const matchesSearch = !search
        || product.name?.toLowerCase().includes(search)
        || product.code?.toLowerCase().includes(search)
        || product.description?.toLowerCase().includes(search);
      const matchesStatus = filters.status === 'ALL'
        || (filters.status === 'ACTIVE' && product.active)
        || (filters.status === 'INACTIVE' && !product.active);
      const matchesType = filters.type === 'ALL' || product.type === filters.type;
      return matchesSearch && matchesStatus && matchesType;
    });
  }, [filters, products]);

  const activeCount = products.filter((product) => product.active).length;

  function updateFilter(event) {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  }

  async function toggleStatus(product) {
    setBusyId(product.id);
    setError('');
    setNotice('');

    try {
      const updated = await setProductStatus(product.id, !product.active);
      setProducts((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setNotice(`${updated.name} is now ${updated.active ? 'active' : 'inactive'}.`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusyId('');
    }
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Products</h1>
          <p className="text-sm text-gray-600">Manage bank products, pricing, and availability.</p>
        </div>
        <Link
          to="/products/new"
          className="inline-flex items-center justify-center rounded-md bg-brand-accent px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
        >
          New product
        </Link>
      </div>

      {notice && (
        <Alert variant="success" title="Saved" onDismiss={() => setNotice('')}>
          {notice}
        </Alert>
      )}
      {error && (
        <Alert variant="error" title="Products unavailable" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Total products</div>
          <div className="mt-2 text-2xl font-semibold">{products.length}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Active</div>
          <div className="mt-2 text-2xl font-semibold text-green-700">{activeCount}</div>
        </div>
        <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <div className="text-sm font-medium text-gray-500">Inactive</div>
          <div className="mt-2 text-2xl font-semibold text-gray-700">{products.length - activeCount}</div>
        </div>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
        <div className="grid gap-3 border-b border-gray-200 p-4 lg:grid-cols-[1fr_180px_220px]">
          <label className="block">
            <span className="sr-only">Search products</span>
            <input
              className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
              name="search"
              value={filters.search}
              onChange={updateFilter}
              placeholder="Search code, name, or description"
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
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </label>
          <label className="block">
            <span className="sr-only">Type</span>
            <select
              className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
              name="type"
              value={filters.type}
              onChange={updateFilter}
            >
              <option value="ALL">All product types</option>
              {PRODUCT_TYPES.map((type) => (
                <option key={type} value={type}>{formatType(type)}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th scope="col" className="px-4 py-3">Product</th>
                <th scope="col" className="px-4 py-3">Type</th>
                <th scope="col" className="px-4 py-3">Minimum</th>
                <th scope="col" className="px-4 py-3">Monthly fee</th>
                <th scope="col" className="px-4 py-3">Rate</th>
                <th scope="col" className="px-4 py-3">Status</th>
                <th scope="col" className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {isLoading && (
                <tr>
                  <td colSpan="7" className="px-4 py-8 text-center text-gray-500">Loading products...</td>
                </tr>
              )}
              {!isLoading && filteredProducts.length === 0 && (
                <tr>
                  <td colSpan="7" className="px-4 py-8 text-center text-gray-500">No products match the current filters.</td>
                </tr>
              )}
              {!isLoading && filteredProducts.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-4 py-4">
                    <div className="font-semibold text-gray-900">{product.name}</div>
                    <div className="mt-1 text-xs text-gray-500">{product.code}</div>
                    {product.description && (
                      <div className="mt-1 max-w-md text-xs text-gray-500">{product.description}</div>
                    )}
                  </td>
                  <td className="px-4 py-4 text-gray-700">{formatType(product.type)}</td>
                  <td className="px-4 py-4 text-gray-700">{formatMoney(product.minimumBalance)}</td>
                  <td className="px-4 py-4 text-gray-700">{formatMoney(product.monthlyFee)}</td>
                  <td className="px-4 py-4 text-gray-700">{formatPercent(product.interestRate)}</td>
                  <td className="px-4 py-4">
                    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${
                      product.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'
                    }`}>
                      {product.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-4 text-right">
                    <div className="flex justify-end gap-2">
                      <Link
                        to={`/products/${product.id}/edit`}
                        className="rounded-md border border-gray-300 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:bg-gray-100"
                      >
                        Edit
                      </Link>
                      <button
                        type="button"
                        onClick={() => toggleStatus(product)}
                        disabled={busyId === product.id}
                        className="rounded-md border border-gray-300 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        {product.active ? 'Disable' : 'Enable'}
                      </button>
                    </div>
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
