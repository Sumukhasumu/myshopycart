function Input({
  id,
  label,
  helperText,
  error,
  className = '',
  ...props
}) {
  const inputId = id || 'ui-input';

  const messageId = `${inputId}-message`;

  const inputClassName = [
    'ui-input',
    error ? 'ui-input-error' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className="ui-input-group">
      {label && (
        <label
          className="ui-input-label"
          htmlFor={inputId}
        >
          {label}
        </label>
      )}

      <input
        {...props}
        id={inputId}
        className={inputClassName}
        aria-invalid={Boolean(error)}
        aria-describedby={
          helperText || error
            ? messageId
            : undefined
        }
      />

      {(helperText || error) && (
        <p
          id={messageId}
          className={
            error
              ? 'ui-input-message ui-input-message-error'
              : 'ui-input-message'
          }
        >
          {error || helperText}
        </p>
      )}
    </div>
  );
}

export default Input;