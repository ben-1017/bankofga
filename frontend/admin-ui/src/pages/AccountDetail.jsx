import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  getAccount,
  getCustomer,
  getProduct,
  listAccountTransactions,
} from '../api/admin.js';
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

function transactionClass(type) {
  if (type === 'DEPOSIT') {
    return 'text-green-700';
  }
  if (type === 'WITHDRAWAL' || type === 'FEE') {
    return 'text-red-700';
  }
  return 'text-gray-900';
}

export default function AccountDetail() {
  const { id } = useParams();
  const [account, setAccount] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [product, setProduct] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [metadataError, setMetadataError] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadAccountDetail() {
      setIsLoading(true);
      setError('');
      setMetadataError('');

      try {
        const nextAccount = await getAccount(id);
        const [customerResult, productResult, transactionResult] = await Promise.allSettled([
          getCustomer(nextAccount.customerId),
          getProduct(nextAccount.productId),
          listAccountTransactions(nextAccount.id),
        ]);

        if (!ignore) {
          setAccount(nextAccount);
          setCustomer(customerResult.status === 'fulfilled' ? customerResult.value : null);
          setProduct(productResult.status === 'fulfilled' ? productResult.value : null);
          setTransactions(transactionResult.status === 'fulfilled' ? transactionResult.value : []);

          if ([customerResult, productResult, transactionResult].some((result) => result.status === 'rejected')) {
            setMetadataError('Some related customer, product, or transaction details could not be loaded.');
          }
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

    loadAccountDetail();

    return () => {
      ignore = true;
    };
  }, [id]);

  return (
    <section className="space-y-6">
      <div>
        <Link to="/accounts" className="text-sm font-medium text-brand-accent hover:text-blue-700">
          Back to accounts
        </Link>
        <h1 className="mt-3 text-2xl font-semibold tracking-tight">
          {account?.accountNumber || 'Account detail'}
        </h1>
        <p className="text-sm text-gray-600">Account balance, owner, product, and transaction history.</p>
      </div>

      {error && (
        <Alert variant="error" title="Account unavailable" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}
      {metadataError && (
        <Alert variant="warning" title="Partial data" onDismiss={() => setMetadataError('')}>
          {metadataError}
        </Alert>
      )}

      {isLoading ? (
        <div className="rounded-lg border border-gray-200 bg-white p-8 text-center text-sm text-gray-500 shadow-sm">
          Loading account...
        </div>
      ) : (
        <>
          <div className="grid gap-4 lg:grid-cols-3">
            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <div className="text-sm font-medium text-gray-500">Balance</div>
              <div className="mt-2 text-3xl font-semibold text-gray-900">{formatMoney(account?.balance)}</div>
            </div>
            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <div className="text-sm font-medium text-gray-500">Status</div>
              <div className="mt-3">
                <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(account?.status)}`}>
                  {formatLabel(account?.status)}
                </span>
              </div>
            </div>
            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <div className="text-sm font-medium text-gray-500">Transactions</div>
              <div className="mt-2 text-3xl font-semibold text-gray-900">{transactions.length}</div>
            </div>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="text-lg font-semibold">Account</h2>
              <dl className="mt-5 grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Account number</dt>
                  <dd className="mt-1 text-sm text-gray-900">{account?.accountNumber}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Opened</dt>
                  <dd className="mt-1 text-sm text-gray-900">{formatDateTime(account?.openedAt)}</dd>
                </div>
                <div className="sm:col-span-2">
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Account ID</dt>
                  <dd className="mt-1 font-mono text-xs text-gray-700">{account?.id}</dd>
                </div>
              </dl>
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <h2 className="text-lg font-semibold">Owner and product</h2>
              <dl className="mt-5 grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Customer</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {customer ? (
                      <Link to={`/customers/${customer.id}`} className="font-medium text-brand-accent hover:text-blue-700">
                        {customer.name}
                      </Link>
                    ) : (
                      shortId(account?.customerId)
                    )}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Product</dt>
                  <dd className="mt-1 text-sm text-gray-900">{product?.name || shortId(account?.productId)}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Customer email</dt>
                  <dd className="mt-1 text-sm text-gray-900">{customer?.email || 'N/A'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wide text-gray-500">Product type</dt>
                  <dd className="mt-1 text-sm text-gray-900">{formatLabel(product?.type)}</dd>
                </div>
              </dl>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 bg-white shadow-sm">
            <div className="border-b border-gray-200 p-4">
              <h2 className="text-lg font-semibold">Transaction history</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                  <tr>
                    <th scope="col" className="px-4 py-3">Type</th>
                    <th scope="col" className="px-4 py-3">Amount</th>
                    <th scope="col" className="px-4 py-3">Before</th>
                    <th scope="col" className="px-4 py-3">After</th>
                    <th scope="col" className="px-4 py-3">Description</th>
                    <th scope="col" className="px-4 py-3">Created</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white">
                  {transactions.length === 0 && (
                    <tr>
                      <td colSpan="6" className="px-4 py-8 text-center text-gray-500">No transactions found for this account.</td>
                    </tr>
                  )}
                  {transactions.map((transaction) => (
                    <tr key={transaction.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4 font-semibold text-gray-900">{formatLabel(transaction.type)}</td>
                      <td className={`px-4 py-4 font-semibold ${transactionClass(transaction.type)}`}>
                        {formatMoney(transaction.amount)}
                      </td>
                      <td className="px-4 py-4 text-gray-700">{formatMoney(transaction.balanceBefore)}</td>
                      <td className="px-4 py-4 text-gray-700">{formatMoney(transaction.balanceAfter)}</td>
                      <td className="px-4 py-4 text-gray-700">{transaction.description || 'N/A'}</td>
                      <td className="px-4 py-4 text-gray-700">{formatDateTime(transaction.createdAt)}</td>
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
