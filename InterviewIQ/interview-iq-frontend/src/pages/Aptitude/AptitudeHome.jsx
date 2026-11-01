import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Shell from "./Shell.jsx";
import { getHistory, getStats, pretty } from "./aptitudeApi.js";

const RULES = ["20 questions, 1 mark each, shown one at a time", "2 minutes for each question", "You cannot go back to an earlier question", "No negative marking", "If time ends, the question is skipped and the next one opens", "Your score and the correct answers appear only at the end", "A refresh is safe: the same question and the remaining time come back"];

export default function AptitudeHome() {
  const nav = useNavigate();
  const [hist, setHist] = useState(null), [stats, setStats] = useState(null), [err, setErr] = useState("");
  useEffect(() => {
    let off = false;
    Promise.all([getHistory(), getStats()]).then(([h, s]) => { if (!off) { setHist(h); setStats(s); } }).catch((e) => !off && setErr(e.message));
    return () => { off = true; };
  }, []);

  return (
    <Shell>
      <h1 className="rise">Aptitude test</h1>
      <p className="sub rise" style={{ animationDelay: "80ms" }}>Quantitative, logical and verbal questions in one timed test.</p>
      {err && <p className="err" role="alert">{err}</p>}

      <div className="apt-grid">
        <section className="glass apt-card rise" style={{ animationDelay: "140ms" }}>
          <h2>Before you start</h2>
          <ul className="apt-rules">{RULES.map((r) => <li key={r}>{r}</li>)}</ul>
          <button className="btn solid lg" onClick={() => nav("/aptitude/test")}>Start / resume test</button>
          <p className="muted">If a test is already running, it opens at the current question.</p>
        </section>

        <section className="glass apt-card rise" style={{ animationDelay: "220ms" }}>
          <h2>Your accuracy by category</h2>
          {!stats && !err && <p className="muted">Loading…</p>}
          {stats && stats.length === 0 && <p className="muted">No attempts yet. Take your first test to see this chart.</p>}
          {stats && stats.length > 0 && (
            <ul className="apt-bars">
              {stats.map((s, i) => (
                <li key={s.category} className={s.accuracy < 60 ? "weak" : ""}>
                  <span>{pretty(s.category)}</span>
                  <div className="track"><i style={{ "--w": s.accuracy + "%", "--d": i * 90 + "ms", animation: "grow 1.1s var(--ease) var(--d) forwards" }} /></div>
                  <em>{Math.round(s.accuracy)}%</em>
                </li>
              ))}
            </ul>
          )}
          {stats && stats.length > 0 && <p className="muted">Red means below 60%. Practise those first.</p>}
        </section>
      </div>

      <section className="glass apt-card rise" style={{ animationDelay: "300ms", marginTop: 20 }}>
        <h2>Previous attempts</h2>
        {!hist && !err && <p className="muted">Loading…</p>}
        {hist && hist.length === 0 && <p className="muted">Nothing here yet.</p>}
        {hist && hist.length > 0 && (
          <div className="apt-table">
            {hist.map((h) => (
              <div className="apt-row" key={h.resultId}>
                <span>{new Date(h.submittedAt).toLocaleString()}</span>
                <b>{h.score}/{h.totalMarks}</b>
                <span>{Math.round(h.percentage)}%</span>
                <Link className="btn ghost" to={`/aptitude/result/${h.resultId}`}>View</Link>
              </div>
            ))}
          </div>
        )}
      </section>
    </Shell>
  );
}
