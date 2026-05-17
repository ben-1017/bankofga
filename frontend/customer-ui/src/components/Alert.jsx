export default function Alert({ kind = 'error', children }) {
  if (!children) return null;
  const palette = {
    error: { bg: '#fde2e1', fg: '#7a1f1c', border: '#f5b1ad' },
    success: { bg: '#d8f5e1', fg: '#155724', border: '#a5e1ba' },
    info: { bg: '#e3f0ff', fg: '#1d3a72', border: '#b6d4fe' },
  }[kind];
  return (
    <div
      role={kind === 'error' ? 'alert' : 'status'}
      style={{
        background: palette.bg,
        color: palette.fg,
        border: `1px solid ${palette.border}`,
        padding: '0.75rem 1rem',
        borderRadius: 6,
        margin: '0.5rem 0',
      }}
    >
      {children}
    </div>
  );
}
