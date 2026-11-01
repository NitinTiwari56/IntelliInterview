// Robot avatar. The .bot/.bst/.bfl/.eye/.mouth styles live in index.css.
export default function Bot() {
  return (
    <svg className="bot" viewBox="0 0 64 64" aria-hidden="true">
      <path className="bst" d="M32 13V7" />
      <circle className="bfl beat" cx="32" cy="5" r="3" />
      <rect className="bst" x="10" y="14" width="44" height="34" rx="12" />
      <rect className="bfl eye" x="20" y="26" width="6" height="8" rx="3" />
      <rect className="bfl eye" x="38" y="26" width="6" height="8" rx="3" />
      <path className="bst mouth" d="M24 40q8 5 16 0" />
      <path className="bst" d="M4 28v8M60 28v8M22 48v6h20v-6" />
    </svg>
  );
}
