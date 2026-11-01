// All resume backend calls (through the API Gateway). api() adds the Bearer token automatically.
import { api } from "../../auth/api.js";

export const getRoles = () => api("/resume/target-roles");
export const analyze = (file, targetRole) => {
  const fd = new FormData();
  fd.append("file", file);
  if (targetRole) fd.append("targetRole", targetRole);
  return api("/resume/analyze", { method: "POST", body: fd });       // do NOT set Content-Type yourself
};
export const getHistory = () => api("/resume/history");
export const getLatest = () => api("/resume/latest");                  // 404 = no resume yet
export const getResume = (id) => api(`/resume/${id}`);
export const saveDetails = (body) => api("/resume/details", { method: "PUT", body });   // edits the latest analysis in place
export const getRoadmap = () => api("/resume/roadmap");

export const MAX_BYTES = 2 * 1024 * 1024;
export const checkFile = (f) => {
  if (!f) return "Choose a PDF file first.";
  if (!/\.pdf$/i.test(f.name) && f.type !== "application/pdf") return "Only PDF files are allowed.";
  if (f.size > MAX_BYTES) return "The file is bigger than 2 MB.";
  return "";
};
export const kb = (n) => (n >= 1048576 ? (n / 1048576).toFixed(1) + " MB" : Math.max(1, Math.round(n / 1024)) + " KB");
export const dt = (iso) => { const d = iso ? new Date(iso) : null; return d && !isNaN(d) ? d.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" }) : ""; };
export const tone = (v) => (v == null ? "" : v >= 75 ? "ok" : v >= 55 ? "mid" : "low");
