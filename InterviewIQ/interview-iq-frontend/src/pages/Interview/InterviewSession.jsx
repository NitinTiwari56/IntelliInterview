import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import Bot from "../../components/Bot.jsx";
import ScoreRing from "../../components/ScoreRing.jsx";
import { reconnect, sendAnswer, endSession, MAX_ANSWER, pretty } from "./interviewApi.js";
import { useSpeech, speak, hush } from "./useSpeech.js";
import "./interview.css";

// Camera preview. The video stays in the browser, nothing is uploaded.
function Camera() {
  const ref = useRef(null), [err, setErr] = useState("");
  useEffect(() => {
    let stream, dead = false;
    navigator.mediaDevices?.getUserMedia({ video: true }).then((s) => { if (dead) return s.getTracks().forEach((t) => t.stop()); stream = s; if (ref.current) ref.current.srcObject = s; })
      .catch(() => setErr("Camera not available or permission denied."));
    return () => { dead = true; stream?.getTracks().forEach((t) => t.stop()); };
  }, []);
  return (
    <div className="iv-cam glass">
      {err ? <p className="muted">{err}</p> : <video ref={ref} autoPlay muted playsInline />}
      <span className="muted">Camera stays in your browser</span>
    </div>
  );
}

export default function InterviewSession() {
  const { sessionId } = useParams(), nav = useNavigate(), loc = useLocation();
  const [s, setS] = useState(null), [q, setQ] = useState(null), [fatal, setFatal] = useState(null);
  const [phase, setPhase] = useState("answering");           // answering | evaluating | feedback
  const [text, setText] = useState(""), [ev, setEv] = useState(null), [err, setErr] = useState("");
  const [aloud, setAloud] = useState(false), [ending, setEnding] = useState(false), [busyEnd, setBusyEnd] = useState(false);
  const area = useRef(null);
  const speech = useSpeech(setText);

  // open the session (from navigation state, otherwise reconnect after a refresh)
  useEffect(() => {
    const init = (x) => {
      if (x.status && x.status !== "IN_PROGRESS") return nav(`/interview/result/${sessionId}`, { replace: true });
      setS(x); setQ({ n: x.currentQuestionNumber, text: x.currentQuestion }); setAloud(x.inputMode && x.inputMode !== "TEXT");
    };
    const st = loc.state?.session;
    if (st && st.sessionId === sessionId) init(st); else reconnect(sessionId).then(init).catch((e) => setFatal(e));
  }, [sessionId]);

  useEffect(() => { if (q && phase === "answering" && aloud) speak(q.text); return hush; }, [q, aloud, phase]);
  useEffect(() => { if (phase === "answering") area.current?.focus(); }, [phase, q]);

  const submit = useCallback(async () => {
    const a = text.trim(); if (!a || phase !== "answering") return;
    speech.stop(); hush(); setErr(""); setPhase("evaluating");
    try { setEv(await sendAnswer(sessionId, a)); setPhase("feedback"); }
    catch (e) { setErr(e.message); setPhase("answering"); if (e.status === 404) setFatal(e); }
  }, [text, phase, sessionId, speech]);

  const next = () => { setQ({ n: ev.nextQuestionNumber, text: ev.nextQuestion }); setText(""); setEv(null); setPhase("answering"); };
  const finish = () => nav(`/interview/result/${sessionId}`, { state: { summary: ev?.sessionSummary }, replace: true });
  const end = async () => {
    setBusyEnd(true);
    try { await endSession(sessionId); hush(); nav(`/interview/result/${sessionId}`, { replace: true }); }
    catch (e) { setErr(e.message); setBusyEnd(false); setEnding(false); }
  };

  if (fatal) return <Shell minimal><div className="apt-center glass"><h2>Could not open this interview</h2><p className="err" role="alert">{fatal.message}</p><div className="row"><Link className="btn solid" to="/interview">Back to interviews</Link></div></div></Shell>;
  if (!s || !q) return <Shell minimal><div className="apt-center glass"><span className="apt-spin" /><h2>Opening your interview</h2></div></Shell>;

  const total = s.maxQuestions || 5, mode = s.inputMode || "TEXT";
  const canSpeak = mode !== "TEXT" && speech.supported;

  return (
    <Shell minimal wide>
      <div className={"iv-wrap" + (mode === "VIDEO" ? " with-cam" : "")}>
        <div className="iv-main">
          <div className="apt-steps" aria-label={`Question ${q.n} of ${total}`}>
            {Array.from({ length: total }, (_, i) => <i key={i} className={i + 1 < q.n || (phase === "feedback" && i + 1 === q.n) ? "done" : i + 1 === q.n ? "now" : ""} />)}
          </div>

          <section className="glass iv-q" key={q.n}>
            <div className="iv-top">
              <div className={"iv-bot" + (phase === "evaluating" ? " think" : "")}><Bot /></div>
              <div>
                <b>AI interviewer</b><br />
                <span className="muted">{pretty(s.interviewType)} round · {s.targetRole} · question {q.n} of {total}</span>
              </div>
              {mode !== "TEXT" && <button className={"btn ghost sm iv-aloud" + (aloud ? " on" : "")} onClick={() => { hush(); setAloud(!aloud); }} aria-pressed={aloud}>{aloud ? "Reading aloud" : "Read aloud"}</button>}
            </div>
            <h2 className="iv-qt">{q.text}</h2>

            {phase !== "feedback" && (
              <>
                <label className="iv-ans">
                  <span className="sr-only">Your answer</span>
                  <textarea ref={area} value={text} rows={7} maxLength={MAX_ANSWER} disabled={phase === "evaluating"}
                    placeholder={canSpeak ? "Press the microphone and speak, or type here." : "Type your answer here. Explain your thinking and give an example."}
                    onChange={(e) => setText(e.target.value)} onKeyDown={(e) => (e.ctrlKey || e.metaKey) && e.key === "Enter" && submit()} />
                </label>
                {canSpeak && speech.listening && <div className="wave iv-wave" aria-hidden="true">{Array.from({ length: 36 }, (_, k) => <i key={k} style={{ "--i": k, "--m": (0.3 + 0.7 * Math.sin((Math.PI * (k + 0.5)) / 36)).toFixed(2) }} />)}</div>}
                {mode !== "TEXT" && !speech.supported && <p className="note">Voice input is not supported in this browser. Use Chrome or Edge, or type your answer.</p>}
                {speech.error && <p className="err" role="alert">{speech.error}</p>}
                {err && <p className="err" role="alert">{err}</p>}
                <div className="iv-bar">
                  {canSpeak && <button type="button" className={"btn ghost iv-mic" + (speech.listening ? " live" : "")} disabled={phase === "evaluating"} onClick={() => (speech.listening ? speech.stop() : speech.start(text))}>{speech.listening ? "Stop recording" : "● Speak"}</button>}
                  <span className="muted iv-count">{text.length} / {MAX_ANSWER}</span>
                  <button className="btn solid" onClick={submit} disabled={!text.trim() || phase === "evaluating"}>
                    {phase === "evaluating" ? <><span className="apt-spin sm" />Evaluating your answer</> : q.n >= total ? "Submit final answer" : "Submit answer"}
                  </button>
                </div>
              </>
            )}

            {phase === "feedback" && ev && (
              <div className="iv-fb">
                <div className="iv-yours"><span className="muted">Your answer</span><p>{ev.answer || text}</p></div>
                <div className="iv-score">
                  <ScoreRing value={ev.score} size={104} label="score" />
                  <p className="iv-feed">{ev.feedback}</p>
                </div>
                <div className="apt-grid iv-sw">
                  <div><p className="iv-lbl good">Strengths</p><ul className="iv-ul good">{(ev.strengths || []).map((x, i) => <li key={i}>{x}</li>)}{!(ev.strengths || []).length && <li className="muted">None noted</li>}</ul></div>
                  <div><p className="iv-lbl warn">To improve</p><ul className="iv-ul warn">{(ev.improvements || []).map((x, i) => <li key={i}>{x}</li>)}{!(ev.improvements || []).length && <li className="muted">None noted</li>}</ul></div>
                </div>
                <div className="iv-bar">
                  {ev.completed ? <button className="btn solid" onClick={finish}>See my results</button> : <button className="btn solid" onClick={next}>Next question →</button>}
                </div>
              </div>
            )}
          </section>

          {phase !== "evaluating" && (
            <div className="iv-end">
              {!ending ? <button className="link" onClick={() => setEnding(true)}>End interview</button> : (
                <span>End now? Your answered questions are kept. <button className="btn ghost sm" onClick={end} disabled={busyEnd}>{busyEnd ? "Ending" : "Yes, end"}</button> <button className="btn ghost sm" onClick={() => setEnding(false)}>Keep going</button></span>
              )}
            </div>
          )}
        </div>
        {mode === "VIDEO" && <aside><Camera /></aside>}
      </div>
    </Shell>
  );
}
