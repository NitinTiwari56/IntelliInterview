export default function Logo({ size = 34 }) {
  return (
    <svg
      className="logo-mark"
      width={size}
      height={size}
      viewBox="0 0 40 40"
      aria-hidden="true"
    >
      <defs>
        <linearGradient id="iiq-lg" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#7C5CFF" />
          <stop offset="1" stopColor="#3DE0E6" />
        </linearGradient>
      </defs>
      <rect width="40" height="40" rx="11" fill="url(#iiq-lg)" />
      <path
        d="M11 12h18a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H19l-6 5v-5h-2a3 3 0 0 1-3-3v-9a3 3 0 0 1 3-3z"
        fill="#07060F"
      />
      <rect x="13.5" y="16.5" width="2.8" height="6" rx="1.4" fill="#3DE0E6" />
      <rect x="18.6" y="14.5" width="2.8" height="10" rx="1.4" fill="#fff" />
      <rect x="23.7" y="16" width="2.8" height="7" rx="1.4" fill="#3DE0E6" />
    </svg>
  );
}
