function PageBackground({
  children,
  variant = 'default',
  className = '',
}) {
  const backgroundClassName = [
    'page-background',
    `page-background-${variant}`,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={backgroundClassName}>
      {children}
    </div>
  );
}

export default PageBackground;