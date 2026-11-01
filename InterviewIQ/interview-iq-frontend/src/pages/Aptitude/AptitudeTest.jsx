import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Shell from "./Shell.jsx";
import { startTest, currentTest, sendAnswer, pretty, mmss } from "./aptitudeApi.js";

// One question at a time. The server owns the 2-minute timer for each question.
export default function AptitudeTest() {
  const nav = useNavigate();
  const [phase, setPhase] = useState("loading");   // loading | test | error
  const [err, setErr] = useState("");
  const [st, setSt] = useState(null);              // TestStateResponse from the server
  const [sel, setSel] = useState(null);            // option chosen for the CURRENT question
  const [left, setLeft] = useState(0);
  const [busy, setBusy] = useState(false);
  const [note, setNote] = useState("");
  const [sendErr, setSendErr] = useState("");
  const endRef = useRef(0), selRef = useRef(null), stRef = useRef(null), busyRef = useRef(false), autoFor = useRef(-1);
  selRef.current = sel; stRef.current = st;

  // show a state from the server: next question, or the finished result
  const apply = useCallback((s) => {
    if (s.status === "COMPLETED") {
      nav(`/aptitude/result/${s.result.resultId}`, { replace: true, state: { result: s.result } });
      return;
    }
    endRef.current = performance.now() + s.question.remainingSeconds * 1000;   // server time, not the device clock
    setSt(s); setSel(null); setLeft(s.question.remainingSeconds); setSendErr(""); setPhase("test");
    if (s.previousTimedOut) { setNote("Time ran out, so the previous question was skipped."); setTimeout(() => setNote(""), 4500); }
  }, [nav]);

  const load = useCallback(() => {
    setPhase("loading"); setErr("");
    startTest().then(apply).catch((e) => { setErr(e.message); setPhase("error"); });
  }, [apply]);
  useEffect(() => { load(); }, [load]);

  // send the answer for the current question (Next button, Skip button, or timer = 0)
  const submit = useCallback(async () => {
    const s = stRef.current;
    if (!s || busyRef.current) return;
    busyRef.current = true; setBusy(true); setSendErr("");
    try { apply(await sendAnswer(s.testId, s.question.index, selRef.current)); }
    catch (e) {
      if (e.status === 409) { try { apply(await currentTest()); } catch { nav("/aptitude", { replace: true }); } }   // out of sync: ask the server
      else if (e.status === 404) nav("/aptitude", { replace: true });
      else setSendErr(e.message);
    } finally { busyRef.current = false; setBusy(false); }
  }, [apply, nav]);

  // countdown for this question
  useEffect(() => {
    if (phase !== "test") return;
    const id = setInterval(() => setLeft(Math.max(0, Math.ceil((endRef.current - performance.now()) / 1000))), 250);
    return () => clearInterval(id);
  }, [phase, st]);
  useEffect(() => {
    if (phase === "test" && st && left === 0 && autoFor.current !== st.question.index) { autoFor.current = st.question.index; submit(); }
  }, [left, phase, st, submit]);

  useEffect(() => {
    if (phase !== "test") return;
    const f = (e) => { e.preventDefault(); e.returnValue = ""; };
    addEventListener("beforeunload", f); return () => removeEventListener("beforeunload", f);
  }, [phase]);

  // keyboard: 1-4 or a-d choose, Enter = next
  useEffect(() => {
    if (phase !== "test") return;
    const k = (e) => {
      if (/^[1-4]$/.test(e.key)) setSel(Number(e.key) - 1);
      else if (/^[a-dA-D]$/.test(e.key)) setSel(e.key.toLowerCase().charCodeAt(0) - 97);
      else if (e.key === "Enter" && document.activeElement === document.body) submit();
    };
    addEventListener("keydown", k); return () => removeEventListener("keydown", k);
  }, [phase, submit]);

  if (phase === "loading") return (
    <Shell minimal><div className="apt-center glass"><span className="apt-spin" /><h2>Preparing your test</h2><p className="sub">Getting 20 fresh questions ready. This can take up to 10 seconds.</p></div></Shell>
  );
  if (phase === "error") return (
    <Shell minimal><div className="apt-center glass"><h2>Could not start the test</h2><p className="err" role="alert">{err}</p>
      <div className="row"><button className="btn solid" onClick={load}>Try again</button><Link className="btn ghost" to="/aptitude">Back</Link></div></div></Shell>
  );

  const q = st.question, last = q.index === q.total, C = 2 * Math.PI * 26;
  return (
    <Shell minimal>
      <div className="apt-one">
        {note && <p className="note" role="status">{note}</p>}
        <div className="apt-steps" aria-label={`Question ${q.index} of ${q.total}`}>
          {Array.from({ length: q.total }, (_, i) => <i key={i} className={i + 1 < q.index ? "done" : i + 1 === q.index ? "now" : ""} />)}
        </div>

        <section className="apt-q glass" key={q.index}>
          <div className="apt-qhead">
            <div className="apt-meta">
              <span>Question {q.index} of {q.total}</span>
              <span className="chip">{pretty(q.category)}</span>
              {q.topic && <span className="chip alt">{q.topic}</span>}
            </div>
            <div className={"apt-ring2" + (left <= 20 ? " low" : "")} role="timer" aria-label={`Time left ${mmss(left)}`}>
              <svg viewBox="0 0 60 60" aria-hidden="true">
                <circle cx="30" cy="30" r="26" className="r-bg" />
                <circle cx="30" cy="30" r="26" className="r-fg" style={{ strokeDasharray: `${(left / q.secondsPerQuestion) * C} ${C}` }} />
              </svg>
              <b>{mmss(left)}</b>
            </div>
          </div>

          <h2 className="apt-qt">{q.question}</h2>
          <div className="apt-opts" role="radiogroup" aria-label="Options">
            {q.options.map((o, i) => (
              <button key={i} role="radio" aria-checked={sel === i} disabled={busy} className={"apt-opt" + (sel === i ? " sel" : "")} onClick={() => setSel(i)}>
                <b>{"ABCD"[i]}</b><span>{o}</span>
              </button>
            ))}
          </div>

          {sendErr && <p className="err" role="alert" style={{ marginTop: 14 }}>{sendErr}</p>}
          <div className="apt-actions">
            <button className="btn ghost" disabled={sel == null || busy} onClick={() => setSel(null)}>Clear</button>
            <button className="btn solid" disabled={busy} onClick={submit}>
              {busy ? "Please wait…" : sendErr ? "Try again" : sel == null ? (last ? "Skip and finish" : "Skip") : last ? "Submit test" : "Next →"}
            </button>
          </div>
          <p className="muted">You cannot come back to this question. Answers and the score are shown only at the end. Keys: 1-4 choose, Enter next.</p>
        </section>
      </div>
    </Shell>
  );
}
