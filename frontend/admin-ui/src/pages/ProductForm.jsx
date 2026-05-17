import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { createProduct, getProduct, updateProduct } from '../api/admin.js';
import { getErrorMessage } from '../api/client.js';
import Alert from '../components/Alert.jsx';

const PRODUCT_TYPES = [
  'CHECKING',
  'SAVINGS',
  'CD',
  'BUSINESS_CHECKING',
  'STUDENT_SAVINGS',
];

const INITIAL_FORM = {
  code: '',
  type: 'CHECKING',
  name: '',
  description: '',
  minimumBalance: '0',
  monthlyFee: '0',
  interestRate: '0',
};

function formatType(type) {
  return type.replaceAll('_', ' ');
}

function toNumber(value) {
  return Number(value === '' ? 0 : value);
}

export default function ProductForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = Boolean(id);
  const [form, setForm] = useState(INITIAL_FORM);
  const [isLoading, setIsLoading] = useState(isEditing);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadProduct() {
      setIsLoading(true);
      setError('');

      try {
        const product = await getProduct(id);
        if (!ignore) {
          setForm({
            code: product.code || '',
            type: product.type || 'CHECKING',
            name: product.name || '',
            description: product.description || '',
            minimumBalance: String(product.minimumBalance ?? 0),
            monthlyFee: String(product.monthlyFee ?? 0),
            interestRate: String(product.interestRate ?? 0),
          });
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

    if (isEditing) {
      loadProduct();
    }

    return () => {
      ignore = true;
    };
  }, [id, isEditing]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function buildPayload() {
    const payload = {
      type: form.type,
      name: form.name.trim(),
      description: form.description.trim(),
      minimumBalance: toNumber(form.minimumBalance),
      monthlyFee: toNumber(form.monthlyFee),
      interestRate: toNumber(form.interestRate),
    };

    if (!isEditing) {
      payload.code = form.code.trim();
    }

    return payload;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const saved = isEditing
        ? await updateProduct(id, buildPayload())
        : await createProduct(buildPayload());
      navigate('/products', {
        replace: true,
        state: { notice: `${saved.name} was ${isEditing ? 'updated' : 'created'}.` },
      });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <Link to="/products" className="text-sm font-medium text-brand-accent hover:text-blue-700">
          Back to products
        </Link>
        <h1 className="mt-3 text-2xl font-semibold tracking-tight">
          {isEditing ? 'Edit product' : 'New product'}
        </h1>
        <p className="text-sm text-gray-600">
          {isEditing ? 'Update product details and pricing.' : 'Create a banking product for customers to open.'}
        </p>
      </div>

      {error && (
        <Alert variant="error" title="Unable to save product" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      <form className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        {isLoading ? (
          <div className="py-8 text-center text-sm text-gray-500">Loading product...</div>
        ) : (
          <div className="grid gap-5 md:grid-cols-2">
            {!isEditing && (
              <label className="block">
                <span className="text-sm font-medium text-gray-700">Code</span>
                <input
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                  name="code"
                  value={form.code}
                  onChange={updateField}
                  placeholder="CHK-STD"
                  required
                />
              </label>
            )}

            {isEditing && (
              <label className="block">
                <span className="text-sm font-medium text-gray-700">Code</span>
                <input
                  className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-600"
                  value={form.code}
                  disabled
                />
              </label>
            )}

            <label className="block">
              <span className="text-sm font-medium text-gray-700">Type</span>
              <select
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                name="type"
                value={form.type}
                onChange={updateField}
                required
              >
                {PRODUCT_TYPES.map((type) => (
                  <option key={type} value={type}>{formatType(type)}</option>
                ))}
              </select>
            </label>

            <label className="block md:col-span-2">
              <span className="text-sm font-medium text-gray-700">Name</span>
              <input
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                name="name"
                value={form.name}
                onChange={updateField}
                placeholder="Standard Checking"
                required
              />
            </label>

            <label className="block md:col-span-2">
              <span className="text-sm font-medium text-gray-700">Description</span>
              <textarea
                className="mt-1 block min-h-24 w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                name="description"
                value={form.description}
                onChange={updateField}
                placeholder="Everyday checking"
              />
            </label>

            <label className="block">
              <span className="text-sm font-medium text-gray-700">Minimum balance</span>
              <input
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                type="number"
                min="0"
                step="0.01"
                name="minimumBalance"
                value={form.minimumBalance}
                onChange={updateField}
              />
            </label>

            <label className="block">
              <span className="text-sm font-medium text-gray-700">Monthly fee</span>
              <input
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                type="number"
                min="0"
                step="0.01"
                name="monthlyFee"
                value={form.monthlyFee}
                onChange={updateField}
              />
            </label>

            <label className="block">
              <span className="text-sm font-medium text-gray-700">Interest rate</span>
              <input
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition focus:border-brand-accent focus:ring-2 focus:ring-brand-accent/20"
                type="number"
                min="0"
                step="0.0001"
                name="interestRate"
                value={form.interestRate}
                onChange={updateField}
              />
            </label>
          </div>
        )}

        <div className="mt-6 flex flex-wrap justify-end gap-3 border-t border-gray-200 pt-5">
          <Link
            to="/products"
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-100"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={isLoading || isSubmitting}
            className="rounded-md bg-brand-accent px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
          >
            {isSubmitting ? 'Saving...' : 'Save product'}
          </button>
        </div>
      </form>
    </section>
  );
}
