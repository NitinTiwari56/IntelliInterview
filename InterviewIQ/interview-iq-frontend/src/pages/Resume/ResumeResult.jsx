import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import ScoreRing from "../../components/ScoreRing.jsx";
import ResumeEditor from "./ResumeEditor.jsx";
import SkillRoadmap from "./SkillRoadmap.jsx";
import { getResume, getHistory, getRoles, kb, dt } from "./resumeApi.js";
import "./resume.css";

const TABS = [["overview", "Overview"], ["edit", "Edit details"], ["roadmap", "Skill roadmap"]];

export default function ResumeResult() {
  const { resumeId } = useParams(), loc = useLocation();
  const fromState = loc.state?.resume && String(loc.state.resume.resumeId) === resumeId ? loc.state.resume : null;
  const [r, setR] = useState(fromState), [err, setErr] = useState(null), [tab, setTab] = useState("overview");
  const [latestId, setLatestId] = useState(null), [roles, setRoles] = useState([]), [ver, setVer] = useState(0);

  useEffect(() => { setR(fromState); setErr(null); setTab("overview"); if (!fromState) getResume(resumeId).then(setR).catch((e) => setErr(e.message)); }, [resumeId]);
  useEffect(() => {
    getHistory().then((h) => setLatestId(h.history?.[0]?.resumeId ?? null)).catch(() => {});
    getRoles().then(setRoles).catch(() => {});
  }, [resumeId]);

  if (err) return <Shell><div className="apt-center glass"><h2>Could not load this analysis</h2><p className="err" role="alert">{err}</p><div className="row"><Link className="btn ghost" to="/resume">Back</Link></div></div></Shell>;
  if (!r) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Loading analysis</h2></div></Shell>;

  const isLatest = latestId != null && String(latestId) === String(r.resumeId);
  const x = r.extractedData || {}, ci = x.candidateInfo || {};
  const saved = (nr) => { setR(nr); setVer((v) => v + 1); };

  return (
    <Shell>
      <Link className="back" to="/resume">← All analyses</Link>
      <div className="rs-head">
        <div>
          <h1 className="rise">{ci.name || "Resume analysis"}</h1>
          <p className="sub rise" style={{ animationDelay: "60ms" }}>{r.fileName} · {kb(r.fileSize || 0)} · {dt(r.uploadedAt)} · target <b>{r.targetRole}</b></p>
        </div>
        <span className={"rs-src " + (r.analysisSource === "AI_POWERED" ? "ai" : "rule")} title={r.analysisSource === "AI_POWERED" ? "Scored with AI" : "Scored with the built-in rules (AI was not available)"}>
          {r.analysisSource === "AI_POWERED" ? "AI powered" : "Rule based"}
        </span>
      </div>

      <div className="ui-tabs-wrap"><div className="apt-tabs" role="tablist">
        {TABS.map(([k, l]) => <button key={k} role="tab" aria-selected={tab === k} onClick={() => setTab(k)}>{l}</button>)}
      </div></div>

      {tab !== "overview" && !isLatest && (
        <p className="note">Editing and the skill roadmap work on your <b>latest</b> analysis only. {latestId && <Link to={`/resume/${latestId}`}>Open the latest one</Link>}</p>
      )}

      {tab === "overview" && (
        <div className="rs-overview">
          <section className="glass apt-card rise">
            <div className="rs-rings">
              <div><ScoreRing value={r.atsScore} size={128} label="ATS" /><p>ATS score <small>from the PDF</small></p></div>
              <div><ScoreRing value={r.formatScore} size={104} label="format" /><p>Format</p></div>
              <div><ScoreRing value={r.experienceScore} size={104} label="experience" /><p>Experience</p></div>
              <div><ScoreRing value={r.skillMatchScore} size={104} label="match" /><p>Skill match <small>{r.lastEditedAt ? "live, edited " + dt(r.lastEditedAt) : "live"}</small></p></div>
            </div>
            {r.summaryFeedback && <p className="rs-summary">{r.summaryFeedback}</p>}
          </section>

          <div className="apt-grid rs-gap">
            <section className="glass apt-card">
              <h2>What to improve</h2>
              {(r.suggestions || []).length === 0 && <p className="muted">No suggestions.</p>}
              <ol className="rs-sugg">{(r.suggestions || []).map((s, i) => <li key={i}>{s}</li>)}</ol>
            </section>
            <section className="glass apt-card">
              <h2>Skills for {r.targetRole}</h2>
              <p className="rs-lbl">Matched ({(r.matchedSkills || []).length})</p>
              <div className="chips rs-ok">{(r.matchedSkills || []).map((s) => <span key={s}>{s}</span>)}{!(r.matchedSkills || []).length && <em className="muted">None yet</em>}</div>
              <p className="rs-lbl">Missing ({(r.missingSkills || []).length})</p>
              <div className="chips rs-bad">{(r.missingSkills || []).map((s) => <span key={s}>{s}</span>)}{!(r.missingSkills || []).length && <em className="muted">Nothing missing</em>}</div>
              {(r.extractedSkills || []).length > 0 && <><p className="rs-lbl">Found in your resume</p><div className="chips">{r.extractedSkills.map((s) => <span key={s}>{s}</span>)}</div></>}
              {isLatest && <div className="row"><button className="btn ghost sm" onClick={() => setTab("edit")}>Add skills</button><button className="btn ghost sm" onClick={() => setTab("roadmap")}>Open skill roadmap</button></div>}
            </section>
          </div>

          <section className="glass apt-card rs-gap">
            <h2>What we read from your resume</h2>
            <div className="rs-read">
              <div><p className="rs-lbl">Contact</p><ul className="rs-kv">{["email", "phone", "linkedin", "github", "portfolio"].filter((k) => ci[k]).map((k) => <li key={k}><span>{k}</span>{ci[k]}</li>)}</ul></div>
              <div><p className="rs-lbl">Education</p>{(x.education || []).map((e, i) => <p key={i} className="rs-p"><b>{e.degree}</b><br />{e.institution} {e.year && "· " + e.year} {e.score && "· " + e.score}</p>)}</div>
              <div><p className="rs-lbl">Experience</p>{(x.experience || []).map((e, i) => <p key={i} className="rs-p"><b>{e.role}</b> {e.company && "at " + e.company}<br /><span className="muted">{e.duration}</span></p>)}{!(x.experience || []).length && <p className="muted">None found</p>}</div>
              <div><p className="rs-lbl">Projects</p>{(x.projects || []).map((p, i) => <p key={i} className="rs-p"><b>{p.title}</b><br /><span className="muted">{(p.techStack || []).join(", ")}</span></p>)}{!(x.projects || []).length && <p className="muted">None found</p>}</div>
            </div>
            <p className="muted">Something wrong here? Fix it in the <button className="link" onClick={() => setTab("edit")}>Edit details</button> tab.</p>
          </section>
        </div>
      )}

      {tab === "edit" && isLatest && <ResumeEditor key={r.resumeId + ":" + ver} data={r} roles={roles} onSaved={saved} />}
      {tab === "roadmap" && isLatest && <SkillRoadmap version={ver} />}
    </Shell>
  );
}
