import { useCallback, useEffect, useRef, useState } from "react";

// Browser speech-to-text (Web Speech API). Works in Chrome and Edge. Text is sent to the backend, never audio.
export function useSpeech(onText) {
  const Rec = typeof window !== "undefined" ? window.SpeechRecognition || window.webkitSpeechRecognition : null;
  const recRef = useRef(null), baseRef = useRef(""), [on, setOn] = useState(false), [error, setError] = useState("");

  const stop = useCallback(() => { try { recRef.current?.stop(); } catch { /* already stopped */ } setOn(false); }, []);
  const start = useCallback((base = "") => {
    if (!Rec) return;
    setError(""); baseRef.current = base && !/\s$/.test(base) ? base + " " : base;
    const r = new Rec(); r.lang = "en-IN"; r.continuous = true; r.interimResults = true;
    r.onresult = (e) => {
      let fin = "", tmp = "";
      for (let i = 0; i < e.results.length; i++) (e.results[i].isFinal ? (fin += e.results[i][0].transcript) : (tmp += e.results[i][0].transcript));
      onText(baseRef.current + fin + tmp);
    };
    r.onerror = (e) => { if (e.error !== "no-speech" && e.error !== "aborted") setError(e.error === "not-allowed" ? "Microphone permission was denied." : "Speech recognition error: " + e.error); setOn(false); };
    r.onend = () => setOn(false);
    recRef.current = r; try { r.start(); setOn(true); } catch { setOn(false); }
  }, [Rec, onText]);
  useEffect(() => () => { try { recRef.current?.abort(); } catch { /* ignore */ } }, []);
  return { supported: !!Rec, listening: on, error, start, stop };
}

export const speak = (text) => {
  if (!("speechSynthesis" in window) || !text) return;
  speechSynthesis.cancel();
  const u = new SpeechSynthesisUtterance(text); u.lang = "en-IN"; u.rate = 1; speechSynthesis.speak(u);
};
export const hush = () => { try { speechSynthesis.cancel(); } catch { /* ignore */ } };
