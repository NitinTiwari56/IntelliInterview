import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import { startSession, listSessions, getSummary, getRoles, TYPES, MODES, pretty, dt, tone } from "./interviewApi.js";
import "./interview.css";

const COUNTS = [3, 5, 7, 10];

export default function InterviewHome() {
  const nav = useNavigate();
  const [type, setType] = useState("TECHNICAL"), [mode, setMode] = useState("TEXT"), [role, setRole] = useState(""), [max, setMax] = useState(5);
  const [roles, setRoles] = useState([]), [sum, setSum] = useState(null), [sess, setSess] = useState(null), [herr, setHerr] = useState("");
  const [busy, setBusy] = useState(false), [err, setErr] = useState("");

  useEffect(() => {
    getRoles().then(setRoles).catch(() => {});
    getSummary().then(setSum).catch((e) => setHerr(e.message));
    listSessions().then(setSess).catch((e) => setHerr(e.message));
  }, []);

  const start = async () => {
    setBusy(true); setErr("");
    try {
      const s = await startSession({ interviewType: type, inputMode: mode, maxQuestions: max, ...(role ? { targetRole: role } : {}) });
      nav(`/interview/session/${s.sessionId}`, { state: { session: s } });
    } catch (e) { setErr(e.message); setBusy(false); }
  };

  const running = (sess || []).find((s) => s.status === "IN_PROGRESS");
  const stat = (v) => (v == null ? "–" : Math.round(v));

  return (
    <Shell>
      <h1 className="rise">AI mock interview</h1>
      <p className="sub rise" style={{ animationDelay: "60ms" }}>Questions are built from your latest resume. Each answer gets a score, feedback, strengths and improvements.</p>

      {running && (
        <div className="note iv-resume rise">You have an interview in progress ({pretty(running.interviewType)}, question {running.currentQuestionNumber ?? "?"}).
          <Link className="btn solid sm" to={`/interview/session/${running.sessionId}`}>Continue</Link></div>
      )}

      <div className="apt-grid">
        <section className="glass apt-card rise" style={{ animationDelay: "120ms" }}>
          <h2>Start an interview</h2>
          {err && <p className="err" role="alert">{err}</p>}

          <p className="iv-lbl">Round</p>
          <div className="iv-opts">
            {TYPES.map(([k, l, d]) => <button key={k} className={"iv-opt" + (type === k ? " sel" : "")} onClick={() => setType(k)} aria-pressed={type === k}><b>{l}</b><span>{d}</span></button>)}
          </div>

          <p className="iv-lbl">How will you answer?</p>
          <div className="iv-opts three">
            {MODES.map(([k, l, d]) => <button key={k} className={"iv-opt" + (mode === k ? " sel" : "")} onClick={() => setMode(k)} aria-pressed={mode === k}><b>{l}</b><span>{d}</span></button>)}
          </div>

          <div className="iv-two">
            <label>Target role
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="">Use my resume's role</option>
                {roles.map((r) => <option key={r.roleName} value={r.roleName}>{r.roleName}</option>)}
              </select>
            </label>
            <label>Questions
              <select value={max} onChange={(e) => setMax(+e.target.value)}>{COUNTS.map((n) => <option key={n} value={n}>{n} questions</option>)}</select>
            </label>
          </div>

          <button className="btn solid wide" onClick={start} disabled={busy}>
            {busy ? <><span className="apt-spin sm" />Preparing your questions</> : "Start interview"}
          </button>
          {busy && <p className="muted">The first question can take up to 8 seconds.</p>}
          <p className="muted">No resume uploaded yet? Questions will use your role only. <Link to="/resume">Analyse a resume</Link></p>
        </section>

        <section className="glass apt-card rise" style={{ animationDelay: "200ms" }}>
          <h2>Your interview stats</h2>
          {herr && <p className="err" role="alert">{herr}</p>}
          {!sum && !herr && <p className="muted"><span className="apt-spin sm" />Loading</p>}
          {sum && (
            <>
              <div className="iv-kpis">
                <div><b>{sum.totalSessions ?? 0}</b><span>sessions</span></div>
                <div><b>{sum.completedSessions ?? 0}</b><span>completed</span></div>
                <div><b>{stat(sum.overallAverageScore)}</b><span>average score</span></div>
              </div>
              <ul className="apt-bars iv-bars">
                {[["Technical", sum.averageTechnicalScore], ["HR", sum.averageHrScore]].map(([n, v], i) => (
                  <li key={n} className={v != null && v < 60 ? "weak" : ""}>
                    <span>{n}</span>
                    <div className="track"><i style={{ "--w": (v ?? 0) + "%", "--d": i * 90 + "ms", animation: "grow 1.1s var(--ease) var(--d) forwards" }} /></div>
                    <em>{v == null ? "–" : Math.round(v)}</em>
                  </li>
                ))}
              </ul>
            </>
          )}
        </section>
      </div>

      <section className="glass apt-card rise" style={{ animationDelay: "280ms", marginTop: 20 }}>
        <h2>Previous interviews</h2>
        {sess && sess.length === 0 && <p className="muted">No interviews yet.</p>}
        <div className="apt-table">
          {(sess || []).map((s) => {
            const done = s.status !== "IN_PROGRESS";
            return (
              <Link className="iv-row" key={s.sessionId} to={done ? `/interview/result/${s.sessionId}` : `/interview/session/${s.sessionId}`}>
                <span><b>{pretty(s.interviewType)}</b> <span className="muted">· {s.targetRole}</span></span>
                <span className="muted">{dt(s.startedAt)}</span>
                <span className={"iv-st " + s.status}>{pretty(s.status)}</span>
                <b className={"iv-sc " + tone(s.averageScore)}>{s.averageScore != null ? Math.round(s.averageScore) : "–"}</b>
              </Link>
            );
          })}
        </div>
      </section>
    </Shell>
  );
}
