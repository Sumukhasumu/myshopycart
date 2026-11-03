function Button({
  children,
  as: Component = 'button',
  type = 'button',
  variant = 'primary',
  size = 'medium',
  disabled = false,
  className = '',
  ...props
}) {
  const buttonClassName = [
    'ui-button',
    `ui-button-${variant}`,
    `ui-button-${size}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const componentProps =
    Component === 'button'
      ? {
          ...props,
          type,
          disabled,
        }
      : props;

  return (
    <Component
      {...componentProps}
      className={buttonClassName}
    >
      {children}
    </Component>
  );
}

export default Button;