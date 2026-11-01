import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext.jsx';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import App from './App.jsx';
import AuthPage from './pages/Auth.jsx';
import AptitudeHome from './pages/aptitude/AptitudeHome.jsx';
import AptitudeTest from './pages/aptitude/AptitudeTest.jsx';
import AptitudeResult from './pages/aptitude/AptitudeResult.jsx';
import './index.css';
import CodingList from './pages/coding/CodingList.jsx';
import CodingProblem from './pages/coding/CodingProblem.jsx';
import ResumeHome from './pages/resume/ResumeHome.jsx';
import ResumeResult from './pages/resume/ResumeResult.jsx';
import InterviewHome from './pages/interview/InterviewHome.jsx';
import InterviewSession from './pages/interview/InterviewSession.jsx';
import InterviewResult from './pages/interview/InterviewResult.jsx';
import Dashboard from './pages/analytics/Dashboard.jsx';
import Roadmap from './pages/analytics/Roadmap.jsx';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/signup" element={<AuthPage mode="signup" />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/aptitude"
          element={
            <ProtectedRoute>
              <AptitudeHome />
            </ProtectedRoute>
          }
        />
        <Route
          path="/aptitude/test"
          element={
            <ProtectedRoute>
              <AptitudeTest />
            </ProtectedRoute>
          }
        />
        <Route
          path="/aptitude/result/:resultId"
          element={
            <ProtectedRoute>
              <AptitudeResult />
            </ProtectedRoute>
          }
        />
        <Route
          path="/coding"
          element={
            <ProtectedRoute>
              <CodingList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/coding/:slug"
          element={
            <ProtectedRoute>
              <CodingProblem />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resume"
          element={
            <ProtectedRoute>
              <ResumeHome />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resume/:resumeId"
          element={
            <ProtectedRoute>
              <ResumeResult />
            </ProtectedRoute>
          }
        />
        <Route
          path="/interview"
          element={
            <ProtectedRoute>
              <InterviewHome />
            </ProtectedRoute>
          }
        />
        <Route
          path="/interview/session/:sessionId"
          element={
            <ProtectedRoute>
              <InterviewSession />
            </ProtectedRoute>
          }
        />
        <Route
          path="/interview/result/:sessionId"
          element={
            <ProtectedRoute>
              <InterviewResult />
            </ProtectedRoute>
          }
        />
        <Route
          path="/roadmap"
          element={
            <ProtectedRoute>
              <Roadmap />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  </BrowserRouter>
);
