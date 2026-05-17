export default function Field({ label, error, children }) {
  return (
    <label style={{ display: 'block', marginBottom: '0.85rem' }}>
      <span style={{ display: 'block', fontSize: 14, fontWeight: 600, marginBottom: 4 }}>
        {label}
      </span>
      {children}
      {error && (
        <span style={{ display: 'block', color: '#b22424', fontSize: 13, marginTop: 4 }}>
          {error}
        </span>
      )}
    </label>
  );
}

export const inputStyle = {
  width: '100%',
  padding: '0.55rem 0.7rem',
  border: '1px solid #c2cad6',
  borderRadius: 6,
  fontSize: 15,
  outline: 'none',
  background: '#fff',
};

export const buttonStyle = {
  padding: '0.6rem 1.2rem',
  background: '#1f6feb',
  color: '#fff',
  border: 'none',
  borderRadius: 6,
  fontSize: 15,
  fontWeight: 600,
  cursor: 'pointer',
};

export const buttonSecondaryStyle = {
  ...buttonStyle,
  background: '#fff',
  color: '#1f2933',
  border: '1px solid #c2cad6',
};
