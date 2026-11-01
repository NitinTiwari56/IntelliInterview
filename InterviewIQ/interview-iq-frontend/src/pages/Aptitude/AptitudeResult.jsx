import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import Shell from "./Shell.jsx";
import { getResult, pretty } from "./aptitudeApi.js";

function useCount(to, ms = 1300) {
  const [v, setV] = useState(0);
  useEffect(() => {
    if (to == null) return;
    const t0 = performance.now(); let raf;
    const step = (n) => { const k = Math.min(1, (n - t0) / ms); setV(Math.round(to * (1 - Math.pow(1 - k, 3)))); if (k < 1) raf = requestAnimationFrame(step); };
    raf = requestAnimationFrame(step); return () => cancelAnimationFrame(raf);
  }, [to, ms]);
  return v;
}

export default function AptitudeResult() {
  const { resultId } = useParams(), loc = useLocation();
  // right after a test the result (with review) comes in the navigation state; from the history we fetch the summary
  const fromState = loc.state?.result && String(loc.state.result.resultId) === resultId ? loc.state.result : null;
  const [res, setRes] = useState(fromState), [err, setErr] = useState(null), [tab, setTab] = useState("all"), [go, setGo] = useState(false);
  useEffect(() => { if (!fromState) getResult(resultId).then(setRes).catch((e) => setErr({ status: e.status, message: e.message })); }, [resultId, fromState]);
  useEffect(() => { if (res) requestAnimationFrame(() => setGo(true)); }, [res]);
  const score = useCount(res?.score);

  if (err) return (
    <Shell><div className="apt-center glass"><h2>Could not load the result</h2><p className="err" role="alert">{err.message}</p>
      <div className="row"><Link className="btn ghost" to="/aptitude">Back</Link></div></div></Shell>
  );
  if (!res) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Loading your result</h2></div></Shell>;

  const C = 2 * Math.PI * 52, pct = Math.round(res.percentage), review = res.review;
  const list = (review || []).filter((r) => tab === "all" || (tab === "wrong" && r.selectedIndex != null && !r.correct) || (tab === "skipped" && r.selectedIndex == null));

  return (
    <Shell>
      <h1 className="rise">Your result</h1>
      {res.autoEnded && <p className="note rise">The test was ended automatically because you left it for too long.</p>}

      <section className="glass apt-card apt-hero rise" style={{ animationDelay: "100ms" }}>
        <div className="apt-ring">
          <svg viewBox="0 0 120 120" aria-hidden="true">
            <circle cx="60" cy="60" r="52" className="rg-bg" />
            <circle cx="60" cy="60" r="52" className="rg-fg" style={{ strokeDasharray: `${go ? (pct / 100) * C : 0} ${C}` }} />
          </svg>
          <div><b>{pct}%</b><span>accuracy</span></div>
        </div>
        <div className="apt-score">
          <p className="muted">Score</p>
          <h2><b>{score}</b> / {res.totalMarks}</h2>
          <div className="apt-pills">
            <span className="p-ok">{res.correctCount} correct</span>
            <span className="p-bad">{res.incorrectCount} incorrect</span>
            <span className="p-skip">{res.skippedCount} skipped</span>
          </div>
        </div>
        <div className="row"><Link className="btn solid" to="/aptitude/test">Take another test</Link><Link className="btn ghost" to="/aptitude">Back</Link></div>
      </section>

      <section className="glass apt-card rise" style={{ animationDelay: "200ms", marginTop: 20 }}>
        <h2>By category</h2>
        <ul className="apt-bars">
          {res.categoryBreakdown.map((c, i) => (
            <li key={c.category} className={c.accuracy < 60 ? "weak" : ""}>
              <span>{pretty(c.category)} <small>{c.correct}/{c.total}</small></span>
              <div className="track"><i style={{ "--w": c.accuracy + "%", "--d": i * 90 + "ms", animation: "grow 1.1s var(--ease) var(--d) forwards" }} /></div>
              <em>{Math.round(c.accuracy)}%</em>
            </li>
          ))}
        </ul>
      </section>

      {!review && <p className="note" style={{ marginTop: 20 }}>The question-by-question review is available only right after you finish a test.</p>}
      {review && (
        <section className="apt-review">
          <div className="apt-tabs" role="tablist">
            {[["all", "All"], ["wrong", "Incorrect"], ["skipped", "Skipped"]].map(([k, l]) => <button key={k} role="tab" aria-selected={tab === k} onClick={() => setTab(k)}>{l}</button>)}
          </div>
          {list.length === 0 && <p className="muted">Nothing to show here.</p>}
          {list.map((r) => (
            <article className="glass apt-rv" key={r.position}>
              <div className="apt-meta">
                <span>Q{r.position}</span><span className="chip">{pretty(r.category)}</span>{r.topic && <span className="chip alt">{r.topic}</span>}
                <span className={"verdict " + (r.selectedIndex == null ? "skip" : r.correct ? "ok" : "bad")}>{r.selectedIndex == null ? "Skipped" : r.correct ? "Correct" : "Incorrect"}</span>
              </div>
              <h3>{r.question}</h3>
              <ul className="apt-ro">
                {r.options.map((o, i) => (
                  <li key={i} className={(i === r.correctIndex ? "right " : "") + (i === r.selectedIndex && !r.correct ? "wrong" : "")}>
                    <b>{"ABCD"[i]}</b><span>{o}</span>{i === r.correctIndex && <em>Correct answer</em>}{i === r.selectedIndex && i !== r.correctIndex && <em>Your answer</em>}
                  </li>
                ))}
              </ul>
              {r.explanation && <p className="expl"><b>Why: </b>{r.explanation}</p>}
            </article>
          ))}
        </section>
      )}
    </Shell>
  );
}
