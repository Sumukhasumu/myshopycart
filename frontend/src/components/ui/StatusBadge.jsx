import Badge from './Badge';

function StatusBadge({
  status,
  children,
}) {
  const normalizedStatus = String(
    status || 'neutral'
  )
    .toLowerCase()
    .replace(/\s+/g, '-');

  const variantMap = {
    success: 'success',
    completed: 'success',
    delivered: 'success',
    paid: 'success',

    warning: 'warning',
    pending: 'warning',
    processing: 'warning',

    error: 'error',
    failed: 'error',
    cancelled: 'error',

    info: 'info',
    created: 'info',
    shipped: 'info',

    neutral: 'neutral',
  };

  const variant =
    variantMap[normalizedStatus] || 'neutral';

  return (
    <Badge variant={variant}>
      {children || status}
    </Badge>
  );
}

export default StatusBadge;