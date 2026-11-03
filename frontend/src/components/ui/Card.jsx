function Card({
  children,
  variant = 'default',
  className = '',
}) {
  const cardClassName = [
    'ui-card',
    `ui-card-${variant}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <section className={cardClassName}>
      {children}
    </section>
  );
}

export default Card;