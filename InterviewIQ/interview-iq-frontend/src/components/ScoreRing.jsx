import { useEffect, useState } from "react";

// Animated circular score (0-100). Uses the .apt-ring styles from aptitude.css.
export default function ScoreRing({ value, size = 150, label = "", suffix = "" }) {
  const v = typeof value === "number" && !isNaN(value) ? Math.max(0, Math.min(100, value)) : null;
  const [go, setGo] = useState(false), [n, setN] = useState(0);
  useEffect(() => {
    if (v == null) return;
    setGo(false); const f = requestAnimationFrame(() => setGo(true));
    const t0 = performance.now(); let raf;
    const step = (t) => { const k = Math.min(1, (t - t0) / 1200); setN(Math.round(v * (1 - Math.pow(1 - k, 3)))); if (k < 1) raf = requestAnimationFrame(step); };
    raf = requestAnimationFrame(step);
    return () => { cancelAnimationFrame(f); cancelAnimationFrame(raf); };
  }, [v]);
  const C = 2 * Math.PI * 52, tone = v == null ? "" : v >= 75 ? "ok" : v >= 55 ? "mid" : "low";
  return (
    <div className={"apt-ring scr " + tone} style={{ width: size, height: size }}>
      <svg viewBox="0 0 120 120" aria-hidden="true">
        <circle cx="60" cy="60" r="52" className="rg-bg" />
        <circle cx="60" cy="60" r="52" className="rg-fg" style={{ strokeDasharray: `${go && v != null ? (v / 100) * C : 0} ${C}` }} />
      </svg>
      <div><b style={{ fontSize: size * 0.22 }}>{v == null ? "–" : n}{v != null && suffix}</b>{label && <span style={{ fontSize: Math.max(10, size * 0.085) }}>{label}</span>}</div>
    </div>
  );
}
