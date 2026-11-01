import { useState } from "react";
import { saveDetails } from "./resumeApi.js";

// Comma/Enter separated chips: type a skill, press Enter or comma.
function Chips({ label, value, onChange, max = 60 }) {
  const [t, setT] = useState("");
  const add = () => { const v = t.trim().replace(/,$/, ""); if (v && !value.includes(v) && value.length < max) onChange([...value, v]); setT(""); };
  return (
    <div className="rs-chips">
      <span className="rs-lbl">{label}</span>
      <div className="rs-chipbox">
        {value.map((s) => <button type="button" key={s} className="rs-chip" onClick={() => onChange(value.filter((x) => x !== s))} title="Remove">{s} <i>×</i></button>)}
        <input value={t} placeholder="Add and press Enter" onChange={(e) => setT(e.target.value)} onBlur={add}
          onKeyDown={(e) => { if (e.key === "Enter" || e.key === ",") { e.preventDefault(); add(); } else if (e.key === "Backspace" && !t && value.length) onChange(value.slice(0, -1)); }} />
      </div>
    </div>
  );
}

const F = ({ label, value, onChange, ...p }) => <label className="rs-f">{label}<input value={value ?? ""} onChange={(e) => onChange(e.target.value)} {...p} /></label>;

// Editor for the latest analysis. Sends whole lists back (backend replaces each field that is sent).
export default function ResumeEditor({ data, roles, onSaved }) {
  const d = data.extractedData || {};
  const [ci, setCi] = useState({ name: "", email: "", phone: "", linkedin: "", github: "", portfolio: "", ...(d.candidateInfo || {}) });
  const [sk, setSk] = useState({ languages: [], frameworks: [], toolsAndDatabases: [], coreSubjects: [], ...(d.skills || {}) });
  const [edu, setEdu] = useState(d.education || []), [exp, setExp] = useState(d.experience || []);
  const [pr, setPr] = useState(d.projects || []), [cert, setCert] = useState(d.certifications || []);
  const [role, setRole] = useState(data.targetRole || "");
  const [busy, setBusy] = useState(false), [msg, setMsg] = useState(null);

  const upd = (set, list, i, k, v) => set(list.map((x, j) => (j === i ? { ...x, [k]: v } : x)));
  const del = (set, list, i) => set(list.filter((_, j) => j !== i));

  const save = async () => {
    setBusy(true); setMsg(null);
    try {
      const r = await saveDetails({
        candidateInfo: ci, education: edu, experience: exp.map((e) => ({ ...e, highlights: e.highlights || [] })),
        projects: pr.map((p) => ({ ...p, techStack: p.techStack || [] })), skills: sk, certifications: cert, targetRole: role || undefined,
      });
      setMsg({ ok: true, text: "Saved. Your skill match was recalculated." }); onSaved(r);
    } catch (e) { setMsg({ ok: false, text: e.message }); } finally { setBusy(false); }
  };

  return (
    <div className="rs-edit">
      <p className="note">Changes update your <b>skill match</b> instantly. The ATS, format and experience scores come from the PDF and do not change. Upload a new PDF to refresh those.</p>

      <section className="glass apt-card"><h2>Target role</h2>
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          {role && !roles.some((r) => r.roleName === role) && <option value={role}>{role}</option>}
          {roles.map((r) => <option key={r.roleName} value={r.roleName}>{r.roleName}</option>)}
        </select>
      </section>

      <section className="glass apt-card"><h2>Skills</h2>
        <Chips label="Languages" value={sk.languages || []} onChange={(v) => setSk({ ...sk, languages: v })} />
        <Chips label="Frameworks" value={sk.frameworks || []} onChange={(v) => setSk({ ...sk, frameworks: v })} />
        <Chips label="Tools and databases" value={sk.toolsAndDatabases || []} onChange={(v) => setSk({ ...sk, toolsAndDatabases: v })} />
        <Chips label="Core subjects" value={sk.coreSubjects || []} onChange={(v) => setSk({ ...sk, coreSubjects: v })} />
      </section>

      <section className="glass apt-card"><h2>Projects</h2>
        {pr.map((p, i) => (
          <div className="rs-item" key={i}>
            <div className="rs-two"><F label="Title" value={p.title} onChange={(v) => upd(setPr, pr, i, "title", v)} /><F label="Link" value={p.link} onChange={(v) => upd(setPr, pr, i, "link", v)} /></div>
            <Chips label="Tech stack" value={p.techStack || []} onChange={(v) => upd(setPr, pr, i, "techStack", v)} max={30} />
            <label className="rs-f">Description<textarea rows={3} value={p.description ?? ""} onChange={(e) => upd(setPr, pr, i, "description", e.target.value)} /></label>
            <button type="button" className="btn ghost sm" onClick={() => del(setPr, pr, i)}>Remove project</button>
          </div>
        ))}
        {pr.length < 20 && <button type="button" className="btn ghost sm" onClick={() => setPr([...pr, { title: "", techStack: [], description: "", link: "" }])}>+ Add project</button>}
      </section>

      <section className="glass apt-card"><h2>Experience</h2>
        {exp.map((x, i) => (
          <div className="rs-item" key={i}>
            <div className="rs-two"><F label="Company" value={x.company} onChange={(v) => upd(setExp, exp, i, "company", v)} /><F label="Role" value={x.role} onChange={(v) => upd(setExp, exp, i, "role", v)} /></div>
            <F label="Duration" value={x.duration} onChange={(v) => upd(setExp, exp, i, "duration", v)} />
            <label className="rs-f">Highlights (one per line)<textarea rows={3} value={(x.highlights || []).join("\n")} onChange={(e) => upd(setExp, exp, i, "highlights", e.target.value.split("\n"))} /></label>
            <button type="button" className="btn ghost sm" onClick={() => del(setExp, exp, i)}>Remove</button>
          </div>
        ))}
        {exp.length < 20 && <button type="button" className="btn ghost sm" onClick={() => setExp([...exp, { company: "", role: "", duration: "", highlights: [] }])}>+ Add experience</button>}
      </section>

      <section className="glass apt-card"><h2>Education</h2>
        {edu.map((x, i) => (
          <div className="rs-item" key={i}>
            <div className="rs-two"><F label="Institution" value={x.institution} onChange={(v) => upd(setEdu, edu, i, "institution", v)} /><F label="Degree" value={x.degree} onChange={(v) => upd(setEdu, edu, i, "degree", v)} /></div>
            <div className="rs-two"><F label="Year" value={x.year} onChange={(v) => upd(setEdu, edu, i, "year", v)} /><F label="Score" value={x.score} onChange={(v) => upd(setEdu, edu, i, "score", v)} /></div>
            <button type="button" className="btn ghost sm" onClick={() => del(setEdu, edu, i)}>Remove</button>
          </div>
        ))}
        {edu.length < 20 && <button type="button" className="btn ghost sm" onClick={() => setEdu([...edu, { institution: "", degree: "", year: "", score: "" }])}>+ Add education</button>}
      </section>

      <section className="glass apt-card"><h2>Certifications</h2>
        <Chips label="Certifications" value={cert} onChange={setCert} max={30} />
      </section>

      <section className="glass apt-card"><h2>Contact details</h2>
        <div className="rs-two"><F label="Name" value={ci.name} onChange={(v) => setCi({ ...ci, name: v })} /><F label="Email" value={ci.email} onChange={(v) => setCi({ ...ci, email: v })} /></div>
        <div className="rs-two"><F label="Phone" value={ci.phone} onChange={(v) => setCi({ ...ci, phone: v })} /><F label="LinkedIn" value={ci.linkedin} onChange={(v) => setCi({ ...ci, linkedin: v })} /></div>
        <div className="rs-two"><F label="GitHub" value={ci.github} onChange={(v) => setCi({ ...ci, github: v })} /><F label="Portfolio" value={ci.portfolio} onChange={(v) => setCi({ ...ci, portfolio: v })} /></div>
      </section>

      <div className="rs-save">
        {msg && <p className={msg.ok ? "rs-okmsg" : "err"} role="status">{msg.text}</p>}
        <button className="btn solid" onClick={save} disabled={busy}>{busy ? <><span className="apt-spin sm" />Saving</> : "Save changes"}</button>
      </div>
    </div>
  );
}
