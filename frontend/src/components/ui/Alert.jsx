function Alert({
  children,
  variant = 'info',
  title,
  className = '',
}) {
  const alertClassName = [
    'ui-alert',
    `ui-alert-${variant}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div
      className={alertClassName}
      role={
        variant === 'error'
          ? 'alert'
          : 'status'
      }
    >
      {title && (
        <strong className="ui-alert-title">
          {title}
        </strong>
      )}

      <div className="ui-alert-content">
        {children}
      </div>
    </div>
  );
}

export default Alert;