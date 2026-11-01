// All interview backend calls (through the API Gateway). api() adds the Bearer token automatically.
import { api } from "../../auth/api.js";

export const startSession = (body) => api("/interview/sessions/start", { method: "POST", body });
export const sendAnswer = (id, answer) => api(`/interview/sessions/${id}/answers`, { method: "POST", body: { answer } });
export const getSession = (id) => api(`/interview/sessions/${id}`);
export const reconnect = (id) => api(`/interview/sessions/${id}/reconnect`, { method: "POST" });
export const endSession = (id) => api(`/interview/sessions/${id}/end`, { method: "POST" });
export const listSessions = () => api("/interview/sessions");
export const getSummary = () => api("/interview/summary");
export const getRoles = () => api("/resume/target-roles");          // same role list as the resume service

export const MAX_ANSWER = 5000;
export const TYPES = [["TECHNICAL", "Technical", "Concepts, projects and problem solving from your resume."], ["HR", "HR", "Behavioural and communication questions."]];
export const MODES = [["TEXT", "Type", "Write your answers."], ["SPEECH", "Speak", "Answer by voice, we turn it into text."], ["VIDEO", "Video", "Speak with your camera on. Video never leaves your browser."]];

export const pretty = (s = "") => String(s).toLowerCase().replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()).replace(/\bHr\b/g, "HR");
export const dt = (iso) => { const d = iso ? new Date(iso) : null; return d && !isNaN(d) ? d.toLocaleString(undefined, { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" }) : ""; };
export const tone = (v) => (v == null ? "" : v >= 75 ? "ok" : v >= 55 ? "mid" : "low");
export const avg = (list) => { const n = list.filter((x) => typeof x === "number"); return n.length ? n.reduce((a, b) => a + b, 0) / n.length : null; };
export const uniq = (lists, max = 6) => [...new Set(lists.flat().filter(Boolean).map((s) => s.trim()))].slice(0, max);
