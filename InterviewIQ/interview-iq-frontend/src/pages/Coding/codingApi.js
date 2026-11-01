// All coding backend calls (through the API Gateway). api() adds the Bearer token automatically.
import { api } from "../../auth/api.js";

export const getLanguages = () => api("/coding/languages");
export const getProblems = ({ difficulty, topic } = {}) => {
  const q = new URLSearchParams();
  if (difficulty) q.set("difficulty", difficulty);
  if (topic) q.set("topic", topic);
  const s = q.toString();
  return api("/coding/problems" + (s ? `?${s}` : ""));
};
export const getProblem = (slug) => api(`/coding/problems/${slug}`);
// Run = sample cases only (or your custom input). Nothing is saved.
export const runCode = (slug, language, sourceCode, customInput) =>
  api(`/coding/problems/${slug}/run`, { method: "POST", body: { language, sourceCode, ...(customInput != null ? { customInput } : {}) } });
// Submit = all test cases (hidden too). The result is saved.
export const submitCode = (slug, language, sourceCode) =>
  api(`/coding/problems/${slug}/submit`, { method: "POST", body: { language, sourceCode } });
export const getStats = () => api("/coding/stats");
export const getProblemSubmissions = (slug) => api(`/coding/problems/${slug}/submissions`);
export const getSubmission = (id) => api(`/coding/submissions/${id}`);

export const label = (s = "") => s.toLowerCase().replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
// colour group of a verdict / status
export const tone = (s) =>
  ({ ACCEPTED: "ok", PASSED: "ok", COMPLETED: "info", RAN: "info", WRONG_ANSWER: "bad", FAILED: "bad", RUNTIME_ERROR: "bad", COMPILATION_ERROR: "warn", TIME_LIMIT_EXCEEDED: "warn" }[s] || "info");
