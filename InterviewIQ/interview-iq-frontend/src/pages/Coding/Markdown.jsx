// Very small, safe markdown renderer for problem descriptions (no HTML is injected).
// Supports: # headings, ``` code blocks, - / * lists, 1. lists, **bold**, `inline code`, paragraphs.
const inline = (t) =>
  t.split(/(`[^`]+`|\*\*[^*]+\*\*)/g).filter(Boolean).map((p, i) =>
    p.length > 2 && p.startsWith("`") && p.endsWith("`") ? <code key={i}>{p.slice(1, -1)}</code>
      : p.length > 4 && p.startsWith("**") && p.endsWith("**") ? <strong key={i}>{p.slice(2, -2)}</strong> : p);

const UL = /^\s*[-*]\s+/, OL = /^\s*\d+[.)]\s+/;

export default function Markdown({ text = "" }) {
  const lines = text.replace(/\r/g, "").split("\n"), out = [];
  let i = 0, k = 0;
  while (i < lines.length) {
    const l = lines[i];
    if (l.trim().startsWith("```")) {
      const buf = []; i++;
      while (i < lines.length && !lines[i].trim().startsWith("```")) buf.push(lines[i++]);
      i++; out.push(<pre key={k++}><code>{buf.join("\n")}</code></pre>); continue;
    }
    const h = /^(#{1,4})\s+(.*)$/.exec(l);
    if (h) { out.push(<h4 key={k++} className="md-h">{inline(h[2])}</h4>); i++; continue; }
    if (UL.test(l)) {
      const items = []; while (i < lines.length && UL.test(lines[i])) items.push(lines[i++].replace(UL, ""));
      out.push(<ul key={k++}>{items.map((x, j) => <li key={j}>{inline(x)}</li>)}</ul>); continue;
    }
    if (OL.test(l)) {
      const items = []; while (i < lines.length && OL.test(lines[i])) items.push(lines[i++].replace(OL, ""));
      out.push(<ol key={k++}>{items.map((x, j) => <li key={j}>{inline(x)}</li>)}</ol>); continue;
    }
    if (!l.trim()) { i++; continue; }
    const para = [];
    while (i < lines.length && lines[i].trim() && !lines[i].trim().startsWith("```") && !/^#{1,4}\s/.test(lines[i]) && !UL.test(lines[i]) && !OL.test(lines[i])) para.push(lines[i++]);
    out.push(<p key={k++}>{inline(para.join(" "))}</p>);
  }
  return <div className="md">{out}</div>;
}
