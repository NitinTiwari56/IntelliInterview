import { useEffect } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { Particles } from '../../App.jsx';
import Logo from '../../components/Logo.jsx';
import { useAuth } from '../../auth/AuthContext.jsx';
import './aptitude.css';

// Common page frame (background + top bar) for the aptitude and coding pages.
// minimal = no navigation (used during the aptitude test).  wide = wider content area (coding editor).
export default function Shell({ children, minimal = false, wide = false }) {
  const { user, logout } = useAuth();
  useEffect(() => {
    const mv = (e) => {
      document.documentElement.style.setProperty('--mx', e.clientX + 'px');
      document.documentElement.style.setProperty('--my', e.clientY + 'px');
    };
    addEventListener('mousemove', mv);
    return () => removeEventListener('mousemove', mv);
  }, []);
  return (
    <>
      <div className="blobs" aria-hidden="true">
        <i />
        <i />
        <i />
      </div>
      <Particles />
      <div className="glow" aria-hidden="true" />
      <header className="apt-top">
        {minimal ? (
          <span className="logo">
            <Logo />
            Interview IQ
          </span>
        ) : (
          <Link className="logo" to="/">
            <Logo />
            Interview IQ
          </Link>
        )}
        {!minimal && (
          <nav aria-label="Main">
            <NavLink to="/" end>
              Home
            </NavLink>
            <NavLink to="/dashboard">Dashboard</NavLink>
            <NavLink to="/resume">Resume</NavLink>
            <NavLink to="/interview">Interview</NavLink>
            <NavLink to="/aptitude">Aptitude</NavLink>
            <NavLink to="/coding">Coding</NavLink>

            <NavLink to="/roadmap">Roadmap</NavLink>
          </nav>
        )}
        {!minimal && (
          <div className="apt-user">
            <span>{user?.name}</span>
            <button className="btn ghost" onClick={logout}>
              Logout
            </button>
          </div>
        )}
      </header>
      <main className={'apt-main' + (wide ? ' wide' : '')}>{children}</main>
    </>
  );
}
