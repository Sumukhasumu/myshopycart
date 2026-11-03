function Badge({
  children,
  variant = 'neutral',
  className = '',
}) {
  const badgeClassName = [
    'ui-badge',
    `ui-badge-${variant}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <span className={badgeClassName}>
      {children}
    </span>
  );
}

export default Badge;