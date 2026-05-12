export default function ComingSoon({ title, description }) {
  return (
    <section className="space-y-3">
      <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="max-w-2xl text-sm text-gray-600">{description}</p>
      <div className="rounded-lg border border-dashed border-gray-300 bg-white p-8 text-sm text-gray-500">
        This admin workflow is queued after product management.
      </div>
    </section>
  );
}
