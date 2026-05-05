import Alert from '../components/Alert.jsx';

export default function Home() {
  return (
    <section className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Admin Console</h1>
        <p className="text-sm text-gray-600">Operations dashboard for Bank of Georgia staff.</p>
      </header>
      <Alert variant="info" title="Sprint in progress">
        Pages land May 5+. Shared layout, navigation, and alert components are in place.
      </Alert>
    </section>
  );
}
