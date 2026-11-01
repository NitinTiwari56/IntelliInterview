import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import TrendChart from "./TrendChart.jsx";
import { getRoles, analyze, getHistory, checkFile, kb, dt, tone } from "./resumeApi.js";
import "./resume.css";

export default function ResumeHome() {
  const nav = useNavigate(), pick = useRef(null);
  const [file, setFile] = useState(null), [role, setRole] = useState(""), [roles, setRoles] = useState([]);
  const [drag, setDrag] = useState(false), [busy, setBusy] = useState(false), [err, setErr] = useState("");
  const [hist, setHist] = useState(null), [herr, setHerr] = useState("");

  useEffect(() => {
    getRoles().then((r) => { setRoles(r); if (r[0]) setRole(r[0].roleName); }).catch(() => {});
    getHistory().then(setHist).catch((e) => setHerr(e.message));
  }, []);

  const choose = (f) => { setErr(""); const m = checkFile(f); if (m) { setFile(null); setErr(m); } else setFile(f); };
  const submit = async () => {
    const m = checkFile(file); if (m) return setErr(m);
    setBusy(true); setErr("");
    try { const r = await analyze(file, role); nav(`/resume/${r.resumeId}`, { state: { resume: r } }); }
    catch (e) { setErr(e.message); setBusy(false); }
  };

  const picked = roles.find((r) => r.roleName === role);
  const list = hist?.history || [];
  const points = [...list].reverse().map((h) => ({ label: dt(h.uploadedAt), value: h.atsScore }));

  return (
    <Shell>
      <h1 className="rise">Resume analysis</h1>
      <p className="sub rise" style={{ animationDelay: "60ms" }}>Upload your resume as a PDF, pick the role you want, and get an ATS-style score with the skills you are missing.</p>

      <div className="apt-grid">
        <section className="glass apt-card rise" style={{ animationDelay: "120ms" }}>
          <h2>Analyse a resume</h2>
          {err && <p className="err" role="alert">{err}</p>}
          <div className={"rs-drop" + (drag ? " over" : "") + (file ? " has" : "")} role="button" tabIndex={0}
            onClick={() => !busy && pick.current.click()} onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && (e.preventDefault(), pick.current.click())}
            onDragOver={(e) => { e.preventDefault(); setDrag(true); }} onDragLeave={() => setDrag(false)}
            onDrop={(e) => { e.preventDefault(); setDrag(false); !busy && choose(e.dataTransfer.files?.[0]); }}>
            <span className="rs-pdf" aria-hidden="true">PDF</span>
            {file ? <span><strong>{file.name}</strong><br /><span className="muted">{kb(file.size)} · click to change</span></span>
              : <span><strong>Drop your resume here</strong><br /><span className="muted">or click to choose. PDF only, up to 2 MB, text-based (not a scan).</span></span>}
            <input ref={pick} type="file" accept="application/pdf,.pdf" hidden onChange={(e) => choose(e.target.files?.[0])} />
          </div>

          <label className="rs-field">Target role
            <select value={role} onChange={(e) => setRole(e.target.value)} disabled={busy || !roles.length}>
              {!roles.length && <option value="">Software Development Engineer (SDE)</option>}
              {roles.map((r) => <option key={r.roleName} value={r.roleName}>{r.roleName}</option>)}
            </select>
          </label>
          {picked && (
            <div className="rs-role">
              <p className="muted">{picked.description}</p>
              <div className="chips">{(picked.coreSkills || []).slice(0, 8).map((s) => <span key={s}>{s}</span>)}</div>
            </div>
          )}

          <button className="btn solid wide" onClick={submit} disabled={busy || !file}>
            {busy ? <><span className="apt-spin sm" />Analysing your resume</> : "Analyse resume"}
          </button>
          {busy && <p className="muted">This takes 1 to 5 seconds. Please keep this page open.</p>}
        </section>

        <section className="glass apt-card rise" style={{ animationDelay: "200ms" }}>
          <h2>Your progress</h2>
          {herr && <p className="err" role="alert">{herr}</p>}
          {!hist && !herr && <p className="muted"><span className="apt-spin sm" />Loading</p>}
          {hist && (
            <>
              <div className="rs-kpis">
                <div><b>{hist.totalAnalyses ?? 0}</b><span>analyses</span></div>
                <div><b>{hist.latestScore ?? "–"}</b><span>latest ATS</span></div>
                <div><b>{hist.averageAtsScore != null ? Math.round(hist.averageAtsScore) : "–"}</b><span>average</span></div>
              </div>
              <TrendChart points={points} />
            </>
          )}
        </section>
      </div>

      <section className="glass apt-card rise" style={{ animationDelay: "280ms", marginTop: 20 }}>
        <h2>Previous analyses</h2>
        {hist && list.length === 0 && <p className="muted">Nothing yet. Your first analysis will show up here.</p>}
        <div className="apt-table">
          {list.map((h) => (
            <Link className="rs-row" key={h.resumeId} to={`/resume/${h.resumeId}`}>
              <span className="rs-fn">{h.fileName}</span>
              <span className="muted">{h.targetRole}</span>
              <span className="muted">{dt(h.uploadedAt)}</span>
              <b className={"rs-sc " + tone(h.atsScore)}>{h.atsScore}</b>
            </Link>
          ))}
        </div>
      </section>
    </Shell>
  );
}
