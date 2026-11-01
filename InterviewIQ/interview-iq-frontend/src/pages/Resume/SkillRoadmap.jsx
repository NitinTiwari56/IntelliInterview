import { useEffect, useState } from "react";
import { getRoadmap } from "./resumeApi.js";

const GROUPS = [["CORE", "Core skills"], ["SECONDARY", "Secondary skills"], ["TOOL", "Tools and frameworks"]];

// Skill checklist for the target role (GET /resume/roadmap). Reloads when `version` changes (after an edit).
export default function SkillRoadmap({ version }) {
  const [r, setR] = useState(null), [err, setErr] = useState("");
  useEffect(() => { setErr(""); getRoadmap().then(setR).catch((e) => setErr(e.message)); }, [version]);
  if (err) return <p className="err" role="alert">{err}</p>;
  if (!r) return <div className="apt-center"><span className="apt-spin" /></div>;
  const pct = r.totalCount ? Math.round((r.coveredCount / r.totalCount) * 100) : 0;
  const items = r.items || [];
  return (
    <div>
      <section className="glass apt-card">
        <div className="rs-rm-top"><h2>Skills for {r.targetRole}</h2><span className="pill">{r.coveredCount} of {r.totalCount} covered</span></div>
        <div className="rs-prog"><i style={{ width: pct + "%" }} /></div>
        <p className="muted">Skill match {r.skillMatchScore ?? "–"}%. Add a skill to your resume (Edit details tab) and it is ticked here.</p>
      </section>
      {GROUPS.map(([k, name]) => {
        const g = items.filter((i) => i.priority === k); if (!g.length) return null;
        return (
          <section className="glass apt-card rs-gap" key={k}>
            <h2>{name}</h2>
            <ul className="rs-skills">
              {g.map((i) => (
                <li key={i.skill} className={i.done ? "done" : ""}>
                  <span className="rs-tick" aria-hidden="true">{i.done ? "✓" : ""}</span>
                  <div><b>{i.skill}</b><p>{i.hint}</p></div>
                </li>
              ))}
            </ul>
          </section>
        );
      })}
    </div>
  );
}
