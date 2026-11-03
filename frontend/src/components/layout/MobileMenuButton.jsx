
function MobileMenuButton({ isOpen, onClick }) {
  return (
    <button
      type="button"
      className="mobile-menu-button"
      onClick={onClick}
      aria-label={
        isOpen ? 'Close navigation menu' : 'Open navigation menu'
      }
      aria-expanded={isOpen}
      aria-controls="mobile-navigation"
    >
      <span
        className="mobile-menu-icon"
        aria-hidden="true"
      >
        <span />
        <span />
        <span />
      </span>
    </button>
  );
}

export default MobileMenuButton;
