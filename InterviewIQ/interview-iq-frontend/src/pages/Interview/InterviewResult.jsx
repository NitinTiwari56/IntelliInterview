import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import ScoreRing from "../../components/ScoreRing.jsx";
import { getSession, pretty, dt, avg, uniq, tone } from "./interviewApi.js";
import "./interview.css";

export default function InterviewResult() {
  const { sessionId } = useParams(), loc = useLocation();
  const [s, setS] = useState(null), [err, setErr] = useState(null), [open, setOpen] = useState(0);
  useEffect(() => { setS(null); setErr(null); getSession(sessionId).then(setS).catch((e) => setErr(e.message)); }, [sessionId]);

  if (err) return <Shell><div className="apt-center glass"><h2>Could not load the result</h2><p className="err" role="alert">{err}</p><div className="row"><Link className="btn ghost" to="/interview">Back</Link></div></div></Shell>;
  if (!s) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Loading your result</h2></div></Shell>;

  const qs = s.questions || [], sm = loc.state?.summary;
  const score = s.averageScore ?? sm?.averageScore ?? avg(qs.map((x) => x.score));
  const strengths = sm?.strengthsSummary?.length ? sm.strengthsSummary : uniq(qs.map((x) => x.strengths || []));
  const improve = sm?.areasForImprovement?.length ? sm.areasForImprovement : uniq(qs.map((x) => x.improvements || []));
  const verdict = s.consolidatedFeedback || sm?.consolidatedFeedback;
  const running = s.status === "IN_PROGRESS";

  return (
    <Shell>
      <Link className="back" to="/interview">← All interviews</Link>
      <h1 className="rise">Interview result</h1>
      <p className="sub rise" style={{ animationDelay: "60ms" }}>{pretty(s.interviewType)} round · {s.targetRole} · {dt(s.completedAt || s.startedAt)}</p>

      {running && <div className="note iv-resume">This interview is not finished. <Link className="btn solid sm" to={`/interview/session/${s.sessionId}`}>Continue</Link></div>}
      {s.status === "INCOMPLETE" && <p className="note">You ended this interview early. The score covers the {qs.length} question{qs.length === 1 ? "" : "s"} you answered.</p>}

      <section className="glass apt-card apt-hero rise" style={{ animationDelay: "100ms" }}>
        <ScoreRing value={score} size={150} label="average" />
        <div className="apt-score">
          <p className="muted">Overall</p>
          <h2><b>{score != null ? Math.round(score) : "–"}</b> / 100</h2>
          <div className="apt-pills"><span>{qs.length} of {s.maxQuestions ?? qs.length} answered</span><span className={"p-" + ({ ok: "ok", mid: "skip", low: "bad", "": "skip" }[tone(score)])}>{score == null ? "No score" : score >= 75 ? "Strong" : score >= 55 ? "Getting there" : "Needs work"}</span></div>
        </div>
        <div className="row"><Link className="btn solid" to="/interview">Take another interview</Link><Link className="btn ghost" to="/roadmap">See my roadmap</Link></div>
      </section>

      {verdict && <section className="glass apt-card iv-gap rise" style={{ animationDelay: "160ms" }}><h2>Overall feedback</h2><p className="iv-verdict">{verdict}</p></section>}

      <div className="apt-grid iv-gap">
        <section className="glass apt-card rise" style={{ animationDelay: "200ms" }}><h2>Strengths</h2><ul className="iv-ul good">{strengths.map((x, i) => <li key={i}>{x}</li>)}{!strengths.length && <li className="muted">Answer a few questions to see strengths.</li>}</ul></section>
        <section className="glass apt-card rise" style={{ animationDelay: "260ms" }}><h2>Work on</h2><ul className="iv-ul warn">{improve.map((x, i) => <li key={i}>{x}</li>)}{!improve.length && <li className="muted">Nothing to flag.</li>}</ul></section>
      </div>

      <section className="glass apt-card iv-gap rise" style={{ animationDelay: "320ms" }}>
        <h2>Question by question</h2>
        <ul className="apt-bars iv-qbars">
          {qs.map((x, i) => (
            <li key={x.questionNumber} className={x.score != null && x.score < 60 ? "weak" : ""}>
              <span>Q{x.questionNumber}</span>
              <div className="track"><i style={{ "--w": (x.score ?? 0) + "%", "--d": i * 90 + "ms", animation: "grow 1.1s var(--ease) var(--d) forwards" }} /></div>
              <em>{x.score ?? "–"}</em>
            </li>
          ))}
        </ul>
        <div className="iv-acc">
          {qs.map((x, i) => (
            <article className="iv-qa" key={x.questionNumber}>
              <button className="iv-qh" onClick={() => setOpen(open === i ? -1 : i)} aria-expanded={open === i}>
                <span className="iv-qn">Q{x.questionNumber}</span><span className="iv-qtxt">{x.question}</span><b className={"iv-sc " + tone(x.score)}>{x.score ?? "–"}</b><i aria-hidden="true">{open === i ? "−" : "+"}</i>
              </button>
              {open === i && (
                <div className="iv-qb fade">
                  <p className="iv-lbl">Your answer</p><p className="iv-yours-t">{x.answer || <span className="muted">Skipped</span>}</p>
                  <p className="iv-lbl">Feedback</p><p>{x.feedback}</p>
                  <div className="apt-grid iv-sw">
                    <div><p className="iv-lbl good">Strengths</p><ul className="iv-ul good">{(x.strengths || []).map((t, k) => <li key={k}>{t}</li>)}</ul></div>
                    <div><p className="iv-lbl warn">To improve</p><ul className="iv-ul warn">{(x.improvements || []).map((t, k) => <li key={k}>{t}</li>)}</ul></div>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
    </Shell>
  );
}
