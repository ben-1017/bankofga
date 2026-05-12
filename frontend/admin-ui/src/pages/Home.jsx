import { Link } from 'react-router-dom';
import Alert from '../components/Alert.jsx';

export default function Home() {
  return (
    <section className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Admin Console</h1>
        <p className="text-sm text-gray-600">Operations dashboard for Bank of Georgia staff.</p>
      </header>

      <Alert variant="success" title="Admin session active">
        Product management is ready for backend API integration through the gateway.
      </Alert>

      <div className="grid gap-4 md:grid-cols-3">
        <Link
          to="/products"
          className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:border-brand-accent hover:shadow"
        >
          <div className="text-sm font-medium text-gray-500">Manage</div>
          <div className="mt-2 text-xl font-semibold text-gray-900">Products</div>
          <p className="mt-2 text-sm text-gray-600">Create products, edit pricing, and toggle availability.</p>
        </Link>
        <Link
          to="/customers"
          className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:border-brand-accent hover:shadow"
        >
          <div className="text-sm font-medium text-gray-500">Review</div>
          <div className="mt-2 text-xl font-semibold text-gray-900">Customers</div>
          <p className="mt-2 text-sm text-gray-600">Search customer records and inspect account ownership.</p>
        </Link>
        <Link
          to="/accounts"
          className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:border-brand-accent hover:shadow"
        >
          <div className="text-sm font-medium text-gray-500">Monitor</div>
          <div className="mt-2 text-xl font-semibold text-gray-900">Accounts</div>
          <p className="mt-2 text-sm text-gray-600">View balances, statuses, and transaction history.</p>
        </Link>
      </div>
    </section>
  );
}
