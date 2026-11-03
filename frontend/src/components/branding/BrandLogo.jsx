function BrandLogo({
  variant = 'light',
  compact = false,
  className = '',
}) {
  const logoSource = compact
    ? `/branding/icon/myshopycart-icon-${variant}.svg`
    : `/branding/logo/myshopycart-logo-${variant}.svg`;

  const altText = compact
  ? 'MYSHOPYCART'
  : 'MYSHOPYCART - Shop Smart - Live Better';

  return (
    <img
      className={className}
      src={logoSource}
      alt={altText}
    />
  );
}

export default BrandLogo;