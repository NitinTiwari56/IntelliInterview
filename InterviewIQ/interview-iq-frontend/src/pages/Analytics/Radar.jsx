// Radar chart (pure SVG) for dashboard.categoryChartData. Needs 3 or more categories.
export default function Radar({ data }) {
  const n = data.length, cx = 150, cy = 150, R = 100;
  const pt = (i, v) => {
    const a = -Math.PI / 2 + (2 * Math.PI * i) / n, r = (R * Math.max(0, Math.min(100, v))) / 100;
    return [cx + r * Math.cos(a), cy + r * Math.sin(a)];
  };
  const ring = (v) => data.map((_, i) => pt(i, v).join(",")).join(" ");
  const shape = data.map((d, i) => pt(i, d.normalizedScore ?? d.score ?? 0).join(",")).join(" ");
  return (
    <svg className="an-radar" viewBox="0 0 300 300" role="img" aria-label="Score profile radar chart">
      {[25, 50, 75, 100].map((v) => <polygon key={v} className="rd-grid" points={ring(v)} />)}
      {data.map((_, i) => { const [x, y] = pt(i, 100); return <line key={i} className="rd-axis" x1={cx} y1={cy} x2={x} y2={y} />; })}
      <polygon className="rd-shape" points={shape} />
      {data.map((d, i) => {
        const [x, y] = pt(i, d.normalizedScore ?? d.score ?? 0), [lx, ly] = pt(i, 122);
        const anchor = lx < cx - 8 ? "end" : lx > cx + 8 ? "start" : "middle";
        return (
          <g key={d.category}>
            <circle className="rd-dot" cx={x} cy={y} r="4.5" style={{ fill: d.color || "var(--c)" }} />
            <text className="rd-lbl" x={lx} y={ly} textAnchor={anchor} dominantBaseline="middle">{d.category}</text>
          </g>
        );
      })}
    </svg>
  );
}
