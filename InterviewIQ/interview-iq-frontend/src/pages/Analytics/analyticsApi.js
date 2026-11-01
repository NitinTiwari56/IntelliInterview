// All analytics backend calls (through the API Gateway). api() adds the Bearer token automatically.
import { api } from "../../auth/api.js";

export const getDashboard = () => api("/analytics/dashboard");
export const getRoadmap = () => api("/analytics/roadmap");
export const regenerateRoadmap = () => api("/analytics/roadmap/regenerate", { method: "POST" });
export const getHealth = () => api("/analytics/roadmap/health");

export const pretty = (s = "") => String(s).toLowerCase().replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()).replace(/\bHr\b/g, "HR");

// roadmap actionType -> page of the app (null = no page, show plain text)
export const ACTION_ROUTES = {
  PRACTICE_CODING: ["/coding", "Open coding practice"],
  PRACTICE_APTITUDE: ["/aptitude", "Open aptitude tests"],
  RETAKE_MOCK_INTERVIEW: ["/interview", "Open mock interview"],
  UPDATE_RESUME: ["/resume", "Open resume analysis"],
  REVISE_CONCEPTS: null,
};

// readiness level text from the backend -> css tone
export const levelTone = (l = "") => (/ready/i.test(l) ? "ok" : /targeted|improv/i.test(l) ? "warn" : "bad");

export const num = (v) => (typeof v === "number" && !isNaN(v) ? v : null);
