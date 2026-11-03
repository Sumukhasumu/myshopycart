function EmptyState({
  title,
  description,
  action,
  className = '',
}) {
  return (
    <section
      className={`ui-empty-state ${className}`.trim()}
      aria-labelledby="empty-state-title"
    >
      <div
        className="ui-empty-state-icon"
        aria-hidden="true"
      >
        <span>∅</span>
      </div>

      {title && (
        <h2 id="empty-state-title">
          {title}
        </h2>
      )}

      {description && (
        <p>{description}</p>
      )}

      {action && (
        <div className="ui-empty-state-action">
          {action}
        </div>
      )}
    </section>
  );
}

export default EmptyState;