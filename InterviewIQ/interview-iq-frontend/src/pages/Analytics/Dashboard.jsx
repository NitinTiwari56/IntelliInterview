import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import Radar from "./Radar.jsx";
import { getDashboard, pretty, levelTone, num } from "./analyticsApi.js";
import "./analytics.css";

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

const SERVICE_LABEL = { ResumeService: "Resume", InterviewService: "Interview", CodingService: "Coding", AptitudeService: "Aptitude" };

function Bar({ label, value, note, i, color }) {
  const v = num(value);
  return (
    <li className={"an-mod" + (v == null ? " none" : v < 60 ? " weak" : "")}>
      <span>{label}{note && <small> {note}</small>}</span>
      <div className="an-track"><i style={{ "--w": (v ?? 0) + "%", "--d": i * 90 + "ms", ...(color ? { background: color } : {}) }} /></div>
      <em>{v == null ? "Not attempted" : Math.round(v)}</em>
    </li>
  );
}

export default function Dashboard() {
  const [d, setD] = useState(null), [err, setErr] = useState(null), [go, setGo] = useState(false);
  const load = () => { setErr(null); setD(null); getDashboard().then(setD).catch((e) => setErr(e.message)); };
  useEffect(load, []);
  useEffect(() => { if (d) requestAnimationFrame(() => setGo(true)); }, [d]);
  const score = useCount(num(d?.overallReadinessScore));

  if (err) return (
    <Shell><div className="apt-center glass"><h2>Could not load your dashboard</h2><p className="err" role="alert">{err}</p>
      <div className="row"><button className="btn solid" onClick={load}>Try again</button><Link className="btn ghost" to="/">Home</Link></div></div></Shell>
  );
  if (!d) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Building your dashboard</h2><p className="sub">Collecting your scores from every module.</p></div></Shell>;

  const m = d.moduleScores || {}, chart = d.categoryChartData || [];
  const C = 2 * Math.PI * 52, pct = num(d.overallReadinessScore) ?? 0, tone = levelTone(d.readinessLevel);
  const apt = m.aptitudeBreakdown;
  const down = Object.entries(d.serviceStatus || {}).filter(([, s]) => s !== "AVAILABLE");

  return (
    <Shell>
      <h1 className="rise">Your dashboard</h1>
      <p className="sub rise" style={{ animationDelay: "60ms" }}>{d.targetRole ? <>Target role: <b>{d.targetRole}</b></> : "Your progress across every module"}</p>

      {!d.hasData && (
        <section className="glass apt-center an-empty rise" style={{ margin: "4vh auto 0" }}>
          <h2>No scores yet</h2>
          <p className="sub">Take a resume analysis, an interview, a coding problem or an aptitude test. Your dashboard fills up as you go.</p>
          <div className="row"><Link className="btn solid" to="/roadmap">See starter roadmap</Link><Link className="btn ghost" to="/aptitude">Take an aptitude test</Link></div>
        </section>
      )}

      {d.hasData && (<>
        <section className="glass apt-card apt-hero rise" style={{ animationDelay: "100ms" }}>
          <div className="apt-ring">
            <svg viewBox="0 0 120 120" aria-hidden="true">
              <circle cx="60" cy="60" r="52" className="rg-bg" />
              <circle cx="60" cy="60" r="52" className="rg-fg" style={{ strokeDasharray: `${go ? (pct / 100) * C : 0} ${C}` }} />
            </svg>
            <div><b>{score}</b><span>/ 100</span></div>
          </div>
          <div className="apt-score">
            <p className="muted">Placement readiness</p>
            <h2 className={"an-level " + tone}>{d.readinessLevel || "Getting Started"}</h2>
            <p className="an-hint">Weighted across the modules you have attempted.</p>
          </div>
          <div className="row"><Link className="btn solid" to="/roadmap">View my roadmap</Link></div>
        </section>

        <div className="apt-grid an-gap">
          <section className="glass apt-card rise" style={{ animationDelay: "180ms" }}>
            <h2>Score profile</h2>
            {chart.length >= 3 ? <Radar data={chart} /> : chart.length ? (
              <ul className="an-mods">{chart.map((c, i) => <Bar key={c.category} label={c.category} value={c.normalizedScore ?? c.score} i={i} color={c.color} />)}</ul>
            ) : <p className="muted">No chart data yet.</p>}
            {chart.length >= 3 && <ul className="an-legend">{chart.map((c) => <li key={c.category}><i style={{ background: c.color }} />{c.category} <b>{Math.round(c.score)}</b></li>)}</ul>}
          </section>

          <section className="glass apt-card rise" style={{ animationDelay: "260ms" }}>
            <h2>Module scores</h2>
            <ul className={"an-mods" + (go ? " go" : "")}>
              <Bar i={0} label="Resume (ATS)" value={m.resumeAtsScore} />
              <Bar i={1} label="Technical interview" value={m.technicalInterviewScore} />
              <Bar i={2} label="HR interview" value={m.hrInterviewScore} />
              <Bar i={3} label="Coding" value={m.codingScore} note={num(m.codingProblemsSolved) != null ? `(${m.codingProblemsSolved} solved)` : ""} />
              <Bar i={4} label="Aptitude" value={m.aptitudeScore} />
            </ul>
            {apt && (
              <div className="an-sub">
                <p className="muted">Aptitude breakdown</p>
                <div className="chips">{Object.entries(apt).map(([k, v]) => <span key={k}>{pretty(k)} {num(v) == null ? "–" : Math.round(v)}</span>)}</div>
              </div>
            )}
          </section>
        </div>

        <div className="apt-grid an-gap">
          <section className="glass apt-card rise" style={{ animationDelay: "320ms" }}>
            <h2>Weak areas</h2>
            {(d.weakAreas || []).length === 0 && <p className="muted">No weak areas right now. Keep practising.</p>}
            <ul className="an-list">
              {(d.weakAreas || []).map((w) => (
                <li key={w.category} className={"sev-" + w.severity}>
                  <div className="an-lh"><b>{pretty(w.category)}</b><span className={"an-sev " + w.severity}>{pretty(w.severity)}</span>{num(w.score) != null && <em>{Math.round(w.score)}/100</em>}</div>
                  {w.gapDescription && <p>{w.gapDescription}</p>}
                </li>
              ))}
            </ul>
          </section>
          <section className="glass apt-card rise" style={{ animationDelay: "380ms" }}>
            <h2>Strengths</h2>
            {(d.strengths || []).length === 0 && <p className="muted">Strengths show up once you have some scores.</p>}
            <ul className="an-good">{(d.strengths || []).map((s) => <li key={s}>{s}</li>)}</ul>
          </section>
        </div>
      </>)}

      {down.length > 0 && (
        <p className="note an-status">Some data is missing right now: {down.map(([k]) => SERVICE_LABEL[k] || k).join(", ")}. The dashboard uses whatever is available.</p>
      )}
    </Shell>
  );
}
