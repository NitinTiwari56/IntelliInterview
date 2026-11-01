import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import { getProblems, getStats, label } from "./codingApi.js";
import "./coding.css";

const STATUS = { SOLVED: ["✓", "Solved"], ATTEMPTED: ["◐", "Attempted"], TODO: ["○", "To do"] };

export default function CodingList() {
  const [all, setAll] = useState(null), [list, setList] = useState(null), [stats, setStats] = useState(null), [err, setErr] = useState("");
  const [diff, setDiff] = useState(""), [topic, setTopic] = useState(""), [q, setQ] = useState("");

  useEffect(() => {
    let off = false;
    Promise.all([getProblems(), getStats()]).then(([p, s]) => { if (!off) { setAll(p); setList(p); setStats(s); } }).catch((e) => !off && setErr(e.message));
    return () => { off = true; };
  }, []);
  useEffect(() => {          // filters are applied by the backend
    if (!all) return;
    if (!diff && !topic) { setList(all); return; }
    let off = false;
    getProblems({ difficulty: diff, topic }).then((p) => !off && setList(p)).catch((e) => !off && setErr(e.message));
    return () => { off = true; };
  }, [diff, topic, all]);

  const topics = useMemo(() => [...new Set((all || []).map((p) => p.topic))].sort(), [all]);
  const shown = (list || []).filter((p) => p.title.toLowerCase().includes(q.trim().toLowerCase()));

  return (
    <Shell>
      <h1 className="rise">Coding practice</h1>
      <p className="sub rise" style={{ animationDelay: "80ms" }}>Solve problems in Java, Python, C++, C or JavaScript. Your code runs against hidden test cases.</p>
      {err && <p className="err" role="alert">{err}</p>}

      {stats && (
        <section className="glass apt-card cd-stats rise" style={{ animationDelay: "120ms" }}>
          <div className="cd-kpis">
            <div><b>{stats.problemsSolved}</b><span>solved</span></div>
            <div><b>{stats.problemsAttempted}</b><span>attempted</span></div>
            <div><b>{Math.round(stats.acceptanceRate)}%</b><span>acceptance</span></div>
            <div><b>{stats.totalSubmissions}</b><span>submissions</span></div>
          </div>
          <ul className="apt-bars cd-diffbars">
            {stats.byDifficulty.map((d, i) => (
              <li key={d.difficulty}>
                <span>{label(d.difficulty)}</span>
                <div className="track"><i className={"dif-" + d.difficulty} style={{ "--w": (d.total ? (d.solved / d.total) * 100 : 0) + "%", "--d": i * 90 + "ms", animation: "grow 1.1s var(--ease) var(--d) forwards" }} /></div>
                <em>{d.solved}/{d.total}</em>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="glass apt-card rise" style={{ animationDelay: "200ms", marginTop: 20 }}>
        <div className="cd-filters">
          <input type="search" placeholder="Search problems" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search problems" />
          <select value={diff} onChange={(e) => setDiff(e.target.value)} aria-label="Difficulty">
            <option value="">All difficulties</option><option value="EASY">Easy</option><option value="MEDIUM">Medium</option><option value="HARD">Hard</option>
          </select>
          <select value={topic} onChange={(e) => setTopic(e.target.value)} aria-label="Topic">
            <option value="">All topics</option>{topics.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>

        {!list && !err && <p className="muted">Loading problems…</p>}
        {list && shown.length === 0 && <p className="muted">No problems match these filters.</p>}
        <div className="apt-table">
          {shown.map((p) => (
            <Link className={"cd-row st-" + p.status} key={p.id} to={`/coding/${p.slug}`}>
              <span className="cd-st" title={STATUS[p.status]?.[1]}>{STATUS[p.status]?.[0] || "○"}</span>
              <b>{p.title}</b>
              <span className="chip">{p.topic}</span>
              <span className={"dif dif-" + p.difficulty}>{label(p.difficulty)}</span>
            </Link>
          ))}
        </div>
      </section>
    </Shell>
  );
}
