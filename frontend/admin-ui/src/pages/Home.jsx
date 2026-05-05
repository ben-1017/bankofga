import Alert from '../components/Alert.jsx';
import { useAuth } from '../auth/useAuth.js';

export default function Home() {
  const { user } = useAuth();
  return (
    <section className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">
          {user ? `Signed in as ${user.email ?? user.name}` : 'Admin Console'}
        </h1>
        <p className="text-sm text-gray-600">Operations dashboard for Bank of Georgia staff.</p>
      </header>
      <Alert variant="info" title="Sprint in progress">
        Foundation in place: Tailwind, shared layout (Navbar/Footer/Alert), auth context, and API service modules.
        Employee login, products, and customer pages land May 5+.
      </Alert>
    </section>
  );
}
