// Small line chart of ATS score over time. points = [{label, value}] oldest first.
export default function TrendChart({ points }) {
  if (!points || points.length < 2) return <p className="muted">Upload at least two resumes to see your progress here.</p>;
  const W = 420, H = 150, px = 28, py = 16, n = points.length;
  const x = (i) => px + (i * (W - px * 2)) / (n - 1), y = (v) => H - py - ((H - py * 2) * v) / 100;
  const line = points.map((p, i) => `${i ? "L" : "M"}${x(i).toFixed(1)} ${y(p.value).toFixed(1)}`).join(" ");
  const area = `${line} L${x(n - 1)} ${H - py} L${x(0)} ${H - py} Z`;
  return (
    <svg className="rs-trend" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="ATS score trend">
      <defs><linearGradient id="rs-g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#3DE0E6" stopOpacity=".35" /><stop offset="1" stopColor="#3DE0E6" stopOpacity="0" /></linearGradient></defs>
      {[0, 50, 100].map((v) => <g key={v}><line className="tg" x1={px} x2={W - px} y1={y(v)} y2={y(v)} /><text className="tt" x={px - 6} y={y(v)} textAnchor="end" dominantBaseline="middle">{v}</text></g>)}
      <path d={area} fill="url(#rs-g)" /><path className="tl" d={line} />
      {points.map((p, i) => <g key={i}><circle className="td" cx={x(i)} cy={y(p.value)} r="4" /><title>{p.label}: {p.value}</title></g>)}
    </svg>
  );
}
