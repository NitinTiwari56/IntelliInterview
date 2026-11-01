import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext.jsx";

// <ProtectedRoute><Page/></ProtectedRoute>            -> any logged-in user
// <ProtectedRoute role="ADMIN"><AdminPage/></ProtectedRoute>  -> admins only
export default function ProtectedRoute({ children, role }) {
  const { isAuthed, user } = useAuth(), loc = useLocation();
  if (!isAuthed) return <Navigate to="/login" replace state={{ from: loc.pathname, reason: "Please log in to continue." }} />;
  if (role && user?.role !== role) return <Navigate to="/dashboard" replace />;
  return children;
}
