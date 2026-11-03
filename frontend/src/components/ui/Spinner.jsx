function Spinner({
  size = 'medium',
  label = 'Loading',
}) {
  return (
    <span
      className={`ui-spinner ui-spinner-${size}`}
      role="status"
      aria-label={label}
    />
  );
}

export default Spinner;