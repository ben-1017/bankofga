const VARIANTS = {
  info: 'bg-blue-50 text-blue-800 border-blue-200',
  success: 'bg-green-50 text-green-800 border-green-200',
  warning: 'bg-amber-50 text-amber-800 border-amber-200',
  error: 'bg-red-50 text-red-800 border-red-200',
};

export default function Alert({ variant = 'info', title, children, onDismiss }) {
  const tone = VARIANTS[variant] ?? VARIANTS.info;
  return (
    <div role="alert" className={`flex items-start gap-3 rounded-md border px-4 py-3 ${tone}`}>
      <div className="flex-1">
        {title && <div className="font-semibold">{title}</div>}
        {children && <div className="text-sm">{children}</div>}
      </div>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss"
          className="text-current/60 hover:text-current"
        >
          ✕
        </button>
      )}
    </div>
  );
}
