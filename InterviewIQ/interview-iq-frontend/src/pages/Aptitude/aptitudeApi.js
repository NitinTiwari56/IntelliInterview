// All aptitude backend calls (through the API Gateway). api() adds the Bearer token automatically.
import { api } from "../../auth/api.js";

export const startTest = () => api("/aptitude/tests", { method: "POST" });        // starts OR returns the running test (first/current question)
export const currentTest = () => api("/aptitude/tests/current");                  // used to re-sync after a 409
export const sendAnswer = (testId, questionIndex, selectedIndex) =>
  api(`/aptitude/tests/${testId}/answer`, { method: "POST", body: { questionIndex, selectedIndex } });   // selectedIndex null = skip
export const getResult = (resultId) => api(`/aptitude/results/${resultId}`);      // summary only (review is null)
export const getHistory = () => api("/aptitude/results");
export const getStats = () => api("/aptitude/stats");

export const pretty = (s = "") => s.toLowerCase().replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
export const mmss = (s) => `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
