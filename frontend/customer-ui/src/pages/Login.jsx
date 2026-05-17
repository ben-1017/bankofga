import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { extractApiError } from '../api/client.js';
import Alert from '../components/Alert.jsx';
import Field, { inputStyle, buttonStyle } from '../components/Field.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from || '/dashboard';

  const [form, setForm] = useState({ username: '', password: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    const v = {};
    if (!form.username.trim()) v.username = 'Username is required';
    if (!form.password) v.password = 'Password is required';
    setErrors(v);
    if (Object.keys(v).length > 0) return;

    setSubmitting(true);
    try {
      await login(form.username.trim(), form.password);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setServerError(extractApiError(err, 'Invalid username or password'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section style={{ maxWidth: 400 }}>
      <h1>Log in</h1>
      <Alert kind="error">{serverError}</Alert>
      <form onSubmit={onSubmit} noValidate>
        <Field label="Username" error={errors.username}>
          <input style={inputStyle} name="username" value={form.username} onChange={onChange} autoComplete="username" />
        </Field>
        <Field label="Password" error={errors.password}>
          <input style={inputStyle} name="password" type="password" value={form.password} onChange={onChange} autoComplete="current-password" />
        </Field>
        <button type="submit" style={buttonStyle} disabled={submitting}>
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
      <p style={{ marginTop: '1rem' }}>
        New here? <Link to="/register">Create an account</Link>
      </p>
    </section>
  );
}
