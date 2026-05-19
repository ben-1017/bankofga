import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext.jsx';
import { customersApi } from '../api/customers.js';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import Field, { inputStyle, buttonStyle, buttonSecondaryStyle } from '../components/Field.jsx';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Profile() {
  const { customer, setCustomer } = useAuth();
  const [form, setForm] = useState({
    name: customer.name || '',
    email: customer.email || '',
    phone: customer.phone || '',
  });
  const [originalUsername] = useState(customer.username || '');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');
  const [editing, setEditing] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    customersApi.profile(customer.id)
      .then((fresh) => {
        if (cancelled) return;
        setForm({ name: fresh.name || '', email: fresh.email || '', phone: fresh.phone || '' });
        setCustomer({ ...customer, ...fresh });
      })
      .catch(() => { /* fall back to cached customer */ });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customer.id]);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const validate = () => {
    const v = {};
    if (!form.name.trim()) v.name = 'Name is required';
    if (!form.email.trim()) v.email = 'Email is required';
    else if (!EMAIL_RE.test(form.email.trim())) v.email = 'Enter a valid email';
    if (!form.phone.trim()) v.phone = 'Phone is required';
    return v;
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccess('');
    const v = validate();
    setErrors(v);
    if (Object.keys(v).length > 0) return;

    setSubmitting(true);
    try {
      const updated = await customersApi.update(customer.id, {
        name: form.name.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
      });
      setCustomer({ ...customer, ...updated });
      setSuccess('Profile updated.');
      setEditing(false);
    } catch (err) {
      setServerError(extractApiError(err, 'Could not update profile'));
    } finally {
      setSubmitting(false);
    }
  };

  const cancelEdit = () => {
    setForm({ name: customer.name || '', email: customer.email || '', phone: customer.phone || '' });
    setErrors({});
    setServerError('');
    setEditing(false);
  };

  return (
    <section style={{ maxWidth: 520 }}>
      <h1>Your profile</h1>
      <Alert kind="error">{serverError}</Alert>
      <Alert kind="success">{success}</Alert>

      <form onSubmit={onSubmit} noValidate>
        <Field label="Full name" error={errors.name}>
          <input style={inputStyle} name="name" value={form.name} onChange={onChange} disabled={!editing} autoComplete="name" />
        </Field>
        <Field label="Email" error={errors.email}>
          <input style={inputStyle} name="email" type="email" value={form.email} onChange={onChange} disabled={!editing} autoComplete="email" />
        </Field>
        <Field label="Phone" error={errors.phone}>
          <input style={inputStyle} name="phone" value={form.phone} onChange={onChange} disabled={!editing} autoComplete="tel" />
        </Field>
        <Field label="Username">
          <input style={{ ...inputStyle, background: '#eef2f7' }} value={originalUsername} disabled readOnly />
        </Field>

        {editing ? (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button type="submit" style={buttonStyle} disabled={submitting}>
              {submitting ? 'Saving...' : 'Save changes'}
            </button>
            <button type="button" style={buttonSecondaryStyle} onClick={cancelEdit} disabled={submitting}>
              Cancel
            </button>
          </div>
        ) : (
          <button type="button" style={buttonStyle} onClick={() => { setEditing(true); setSuccess(''); }}>
            Edit profile
          </button>
        )}
      </form>
    </section>
  );
}
