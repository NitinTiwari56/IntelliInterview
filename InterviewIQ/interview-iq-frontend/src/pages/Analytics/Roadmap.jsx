import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import { getDashboard, getRoadmap, regenerateRoadmap, pretty, ACTION_ROUTES } from "./analyticsApi.js";
import "./analytics.css";

const when = (iso) => { const d = iso ? new Date(iso) : null; return d && !isNaN(d) ? d.toLocaleString(undefined, { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" }) : ""; };

export default function Roadmap() {
  const [r, setR] = useState(null), [err, setErr] = useState(null), [busy, setBusy] = useState(false), [rerr, setRerr] = useState("");

  const load = async () => {
    setErr(null); setR(null);
    try {
      await getDashboard().catch(() => {});   // contract: dashboard first (it builds the student profile), roadmap after
      setR(await getRoadmap());
    } catch (e) { setErr(e.message); }
  };
  useEffect(() => { load(); }, []);

  const refresh = async () => {
    setBusy(true); setRerr("");
    try { setR(await regenerateRoadmap()); } catch (e) { setRerr(e.message); } finally { setBusy(false); }
  };

  if (err) return (
    <Shell><div className="apt-center glass"><h2>Could not load your roadmap</h2><p className="err" role="alert">{err}</p>
      <div className="row"><button className="btn solid" onClick={load}>Try again</button><Link className="btn ghost" to="/dashboard">Dashboard</Link></div></div></Shell>
  );
  if (!r) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Preparing your roadmap</h2><p className="sub">This can take a few seconds.</p></div></Shell>;

  const steps = [...(r.recommendedActions || [])].sort((a, b) => a.stepNumber - b.stepNumber);

  return (
    <Shell>
      <div className="an-head">
        <div>
          <h1 className="rise">Your roadmap</h1>
          <p className="sub rise" style={{ animationDelay: "60ms" }}>{r.targetRole ? <>Target role: <b>{r.targetRole}</b></> : "A step-by-step plan from your weakest area up"}</p>
        </div>
        <div className="row an-actions rise" style={{ animationDelay: "100ms" }}>
          <Link className="btn ghost" to="/dashboard">Dashboard</Link>
          <button className="btn solid" onClick={refresh} disabled={busy}>{busy ? <><span className="apt-spin sm" />Refreshing</> : "Refresh roadmap"}</button>
        </div>
      </div>

      {busy && <p className="note">Rebuilding your plan from your latest scores. This can take 3 to 10 seconds.</p>}
      {rerr && <p className="err" role="alert">{rerr}</p>}
      {r.genericStarter && <p className="note rise">You have no scores yet, so this is a general starter plan. Complete a module and press Refresh for a personal one.</p>}

      <section className="glass apt-card an-sum rise" style={{ animationDelay: "140ms" }}>
        <div className="an-kpis">
          <div><b>{steps.length}</b><span>steps</span></div>
          <div><b>{r.totalEstimatedHours ?? 0}</b><span>hours in total</span></div>
          <div><b>{r.overallReadinessScore != null ? Math.round(r.overallReadinessScore) : "–"}</b><span>readiness now</span></div>
        </div>
        {r.summaryVerdict && <p className="an-verdict">{r.summaryVerdict}</p>}
        {r.generatedAt && <p className="muted">Generated {when(r.generatedAt)}</p>}
      </section>

      <ol className="an-steps">
        {steps.map((s, i) => {
          const route = ACTION_ROUTES[s.actionType];
          return (
            <li key={s.stepNumber} className="rise" style={{ animationDelay: 200 + i * 70 + "ms" }}>
              <span className="an-n">{s.stepNumber}</span>
              <article className="glass an-step">
                <div className="an-sh">
                  <h3>{s.title}</h3>
                  <span className={"an-sev " + s.priority}>{pretty(s.priority)} priority</span>
                </div>
                <div className="apt-meta"><span className="chip">{pretty(s.category)}</span><span className="chip alt">{s.estimatedHours} h</span>{s.status && s.status !== "PENDING" && <span className="chip">{pretty(s.status)}</span>}</div>
                {s.description && <p className="an-desc">{s.description}</p>}
                {s.suggestedTarget && <p className="an-target"><b>Target:</b> {s.suggestedTarget}</p>}
                {route && <Link className="btn ghost sm" to={route[0]}>{route[1]} →</Link>}
              </article>
            </li>
          );
        })}
      </ol>
      {steps.length === 0 && <p className="muted">No steps yet.</p>}
    </Shell>
  );
}
