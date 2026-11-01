import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Shell from "../aptitude/Shell.jsx";
import Markdown from "./Markdown.jsx";
import { getLanguages, getProblem, runCode, submitCode, getProblemSubmissions, getSubmission, label, tone } from "./codingApi.js";
import "./coding.css";

const COOLDOWN = 5;   // backend allows one run/submit every 5 seconds

function CodeEditor({ value, onChange }) {
  const gut = useRef(null);
  const onKey = (e) => {
    if (e.key !== "Tab") return;
    e.preventDefault();
    const t = e.target, s = t.selectionStart;
    onChange(value.slice(0, s) + "    " + value.slice(t.selectionEnd));
    requestAnimationFrame(() => { t.selectionStart = t.selectionEnd = s + 4; });
  };
  const n = value.split("\n").length;
  return (
    <div className="cd-ed">
      <pre className="cd-gut" ref={gut} aria-hidden="true">{Array.from({ length: n }, (_, i) => i + 1).join("\n")}</pre>
      <textarea value={value} onChange={(e) => onChange(e.target.value)} onKeyDown={onKey} onScroll={(e) => { gut.current.scrollTop = e.target.scrollTop; }}
        spellCheck={false} wrap="off" aria-label="Code editor" />
    </div>
  );
}

const Block = ({ title, text }) => <div className="cd-io"><span>{title}</span><pre>{text === "" || text == null ? " " : text}</pre></div>;

function RunOutput({ out }) {
  if (out.kind === "error") return <p className="err" role="alert">{out.msg}</p>;
  if (out.kind === "run") {
    const d = out.data, custom = d.verdict === "COMPLETED";
    return (
      <>
        <div className="cd-vhead"><span className={"vb " + tone(d.verdict)}>{label(d.verdict)}</span>{!custom && <span className="muted">{d.results?.filter((r) => r.status === "PASSED").length}/{d.results?.length} sample cases passed</span>}</div>
        {d.compileOutput && <Block title="Compiler output" text={d.compileOutput} />}
        {(d.results || []).map((r) => (
          <div className="cd-case" key={r.caseNumber}>
            <div className="cd-chead"><b>{custom ? "Your input" : `Case ${r.caseNumber}`}</b><span className={"vb sm " + tone(r.status)}>{label(r.status)}</span>{r.timeMs != null && <span className="muted">{r.timeMs} ms</span>}</div>
            <div className="cd-grid">
              <Block title="Input" text={r.input} />
              {!custom && <Block title="Expected" text={r.expectedOutput} />}
              <Block title="Your output" text={r.actualOutput} />
            </div>
            {r.stderr && <Block title="Error output" text={r.stderr} />}
          </div>
        ))}
      </>
    );
  }
  const s = out.data;
  return (
    <>
      <div className="cd-vhead big"><span className={"vb " + tone(s.verdict)}>{label(s.verdict)}</span><b>{s.passedTests}/{s.totalTests} tests passed</b></div>
      <p className="muted">{s.maxTimeMs != null && `Slowest test: ${s.maxTimeMs} ms`}{s.maxMemoryKb != null && ` · Memory: ${(s.maxMemoryKb / 1024).toFixed(1)} MB`}</p>
      {s.message && <Block title="Details" text={s.message} />}
      {s.failedCase && <div className="cd-case"><div className="cd-chead"><b>Failed sample case</b></div><div className="cd-grid"><Block title="Input" text={s.failedCase.input} /><Block title="Expected" text={s.failedCase.expectedOutput} /><Block title="Your output" text={s.failedCase.actualOutput} /></div></div>}
      {s.failedOnHiddenTest && <p className="note">Your code failed on a hidden test case. Hidden inputs are not shown. Think about edge cases (empty input, very large values, negative numbers).</p>}
    </>
  );
}

export default function CodingProblem() {
  const { slug } = useParams();
  const [prob, setProb] = useState(null), [langs, setLangs] = useState(null), [err, setErr] = useState("");
  const [lang, setLang] = useState(() => localStorage.getItem("iiq_lang") || "python");
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(null), [cool, setCool] = useState(0);        // busy: "run" | "submit" | null
  const [out, setOut] = useState(null), [useCustom, setUseCustom] = useState(false), [custom, setCustom] = useState("");
  const [tab, setTab] = useState("desc"), [subs, setSubs] = useState(null);
  const loaded = useRef(""), pending = useRef(null);   // pending = code of a past submission to load into the editor

  useEffect(() => {
    let off = false;
    Promise.all([getProblem(slug), getLanguages()]).then(([p, l]) => { if (!off) { setProb(p); setLangs(l); } }).catch((e) => !off && setErr(e.status === 404 ? "This problem does not exist." : e.message));
    return () => { off = true; };
  }, [slug]);

  const starter = useCallback((k) => langs?.find((l) => l.key === k)?.starterCode ?? "", [langs]);
  const draftKey = `iiq_code_${slug}_${lang}`;
  // load the saved draft (or the starter code) when the problem / language changes
  useEffect(() => {
    if (!langs || !prob) return;
    if (!langs.some((l) => l.key === lang)) { setLang(langs[0].key); return; }
    let saved = null; try { saved = localStorage.getItem(draftKey); } catch { /* ignore */ }
    const p = pending.current; pending.current = null;
    loaded.current = draftKey; setCode(p ?? saved ?? starter(lang));
  }, [langs, prob, lang, draftKey, starter]);
  useEffect(() => {          // auto-save the draft in this browser
    if (loaded.current !== draftKey) return;
    try { localStorage.setItem(draftKey, code); } catch { /* ignore */ }
  }, [code, draftKey]);
  useEffect(() => { try { localStorage.setItem("iiq_lang", lang); } catch { /* ignore */ } }, [lang]);

  useEffect(() => {          // cooldown timer
    if (cool <= 0) return;
    const id = setTimeout(() => setCool((c) => c - 1), 1000); return () => clearTimeout(id);
  }, [cool]);

  const loadSubs = useCallback(() => getProblemSubmissions(slug).then(setSubs).catch(() => setSubs([])), [slug]);
  useEffect(() => { if (tab === "subs" && subs == null) loadSubs(); }, [tab, subs, loadSubs]);

  const go = async (kind) => {
    if (busy || cool > 0) return;
    setBusy(kind); setOut(null);
    try {
      if (kind === "run") setOut({ kind: "run", data: await runCode(slug, lang, code, useCustom ? custom : undefined) });
      else { setOut({ kind: "submit", data: await submitCode(slug, lang, code) }); loadSubs(); }
    } catch (e) {
      setOut({ kind: "error", msg: e.status === 429 ? "You are running code too fast. Wait a few seconds and try again." : e.message });
    } finally { setBusy(null); setCool(COOLDOWN); }
  };

  const openSub = async (id) => {
    try {
      const s = await getSubmission(id);
      if (s.language === lang) setCode(s.sourceCode); else { pending.current = s.sourceCode; setLang(s.language); }
    } catch (e) { setOut({ kind: "error", msg: e.message }); }
  };
  const reset = () => { if (window.confirm("Replace your code with the starter code?")) setCode(starter(lang)); };

  if (err) return <Shell><div className="apt-center glass"><h2>{err}</h2><div className="row"><Link className="btn ghost" to="/coding">Back to problems</Link></div></div></Shell>;
  if (!prob || !langs) return <Shell><div className="apt-center glass"><span className="apt-spin" /><h2>Loading problem</h2></div></Shell>;

  const wait = cool > 0 ? ` (${cool})` : "";
  return (
    <Shell wide>
      <div className="cd-top"><Link to="/coding" className="back">← All problems</Link></div>
      <div className="cd-split">
        <section className="glass cd-left">
          <div className="apt-tabs" role="tablist">
            <button role="tab" aria-selected={tab === "desc"} onClick={() => setTab("desc")}>Problem</button>
            <button role="tab" aria-selected={tab === "subs"} onClick={() => setTab("subs")}>My submissions</button>
          </div>

          {tab === "desc" && (
            <div className="cd-scroll">
              <h2 className="cd-title">{prob.title}</h2>
              <div className="cd-tags"><span className={"dif dif-" + prob.difficulty}>{label(prob.difficulty)}</span><span className="chip">{prob.topic}</span>
                <span className="muted">Time limit {prob.timeLimitSeconds}s · Memory {Math.round(prob.memoryLimitKb / 1024)} MB</span></div>
              <Markdown text={prob.description} />
              <h4 className="md-h">Sample cases</h4>
              {prob.samples.map((s, i) => <div className="cd-case" key={i}><div className="cd-grid"><Block title={`Sample ${i + 1} input`} text={s.input} /><Block title="Expected output" text={s.expectedOutput} /></div></div>)}
              <p className="muted">Read the input from standard input and print the answer to standard output. In Java the class must be named <code>Main</code>.</p>
            </div>
          )}
          {tab === "subs" && (
            <div className="cd-scroll">
              {!subs && <p className="muted">Loading…</p>}
              {subs && subs.length === 0 && <p className="muted">No submissions on this problem yet.</p>}
              {subs && subs.map((s) => (
                <button className="cd-sub" key={s.id} onClick={() => openSub(s.id)} title="Load this code in the editor">
                  <span className={"vb sm " + tone(s.verdict)}>{label(s.verdict)}</span>
                  <b>{s.passedTests}/{s.totalTests}</b><span>{label(s.language)}</span><span className="muted">{new Date(s.createdAt).toLocaleString()}</span>
                </button>
              ))}
            </div>
          )}
        </section>

        <section className="cd-right">
          <div className="glass cd-editor">
            <div className="cd-bar">
              <select value={lang} onChange={(e) => setLang(e.target.value)} aria-label="Language">{langs.map((l) => <option key={l.key} value={l.key}>{l.label}</option>)}</select>
              <button className="btn ghost sm" onClick={reset}>Reset</button>
              <label className="cd-chk"><input type="checkbox" checked={useCustom} onChange={(e) => setUseCustom(e.target.checked)} />Custom input</label>
              <span className="grow" />
              <button className="btn ghost" disabled={!!busy || cool > 0} onClick={() => go("run")}>{busy === "run" ? "Running…" : `Run${wait}`}</button>
              <button className="btn solid" disabled={!!busy || cool > 0} onClick={() => go("submit")}>{busy === "submit" ? "Submitting…" : `Submit${wait}`}</button>
            </div>
            <CodeEditor value={code} onChange={setCode} />
            {useCustom && <textarea className="cd-custom" placeholder="Type your own input here, then press Run" value={custom} onChange={(e) => setCustom(e.target.value)} aria-label="Custom input" />}
          </div>

          <div className="glass cd-out" aria-live="polite">
            <h3>Output</h3>
            {busy && <p className="muted"><span className="apt-spin sm" /> Running your code. This can take up to 10 seconds.</p>}
            {!busy && !out && <p className="muted">Press <b>Run</b> to test on the sample cases, or <b>Submit</b> to check all test cases.</p>}
            {!busy && out && <RunOutput out={out} />}
          </div>
        </section>
      </div>
    </Shell>
  );
}
