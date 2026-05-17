import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import Field, { inputStyle, buttonStyle } from '../components/Field.jsx';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validate(form) {
  const errors = {};
  if (!form.name.trim()) errors.name = 'Name is required';
  if (!form.email.trim()) errors.email = 'Email is required';
  else if (!EMAIL_RE.test(form.email.trim())) errors.email = 'Enter a valid email';
  if (!form.username.trim()) errors.username = 'Username is required';
  if (!form.phone.trim()) errors.phone = 'Phone is required';
  if (!form.password) errors.password = 'Password is required';
  else if (form.password.length < 8) errors.password = 'Password must be at least 8 characters';
  if (form.password !== form.confirm) errors.confirm = 'Passwords do not match';
  return errors;
}

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '', email: '', username: '', phone: '', password: '', confirm: '',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    const v = validate(form);
    setErrors(v);
    if (Object.keys(v).length > 0) return;

    setSubmitting(true);
    try {
      await register({
        name: form.name.trim(),
        email: form.email.trim(),
        username: form.username.trim(),
        phone: form.phone.trim(),
        password: form.password,
      });
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setServerError(extractApiError(err, 'Registration failed'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section style={{ maxWidth: 460 }}>
      <h1>Create your account</h1>
      <Alert kind="error">{serverError}</Alert>
      <form onSubmit={onSubmit} noValidate>
        <Field label="Full name" error={errors.name}>
          <input style={inputStyle} name="name" value={form.name} onChange={onChange} autoComplete="name" />
        </Field>
        <Field label="Email" error={errors.email}>
          <input style={inputStyle} name="email" type="email" value={form.email} onChange={onChange} autoComplete="email" />
        </Field>
        <Field label="Username" error={errors.username}>
          <input style={inputStyle} name="username" value={form.username} onChange={onChange} autoComplete="username" />
        </Field>
        <Field label="Phone" error={errors.phone}>
          <input style={inputStyle} name="phone" value={form.phone} onChange={onChange} autoComplete="tel" />
        </Field>
        <Field label="Password" error={errors.password}>
          <input style={inputStyle} name="password" type="password" value={form.password} onChange={onChange} autoComplete="new-password" />
        </Field>
        <Field label="Confirm password" error={errors.confirm}>
          <input style={inputStyle} name="confirm" type="password" value={form.confirm} onChange={onChange} autoComplete="new-password" />
        </Field>
        <button type="submit" style={buttonStyle} disabled={submitting}>
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>
      <p style={{ marginTop: '1rem' }}>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </section>
  );
}
