import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Particles, Mag } from '../App.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import './Auth.css';
import Logo from '../components/Logo.jsx';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const POINTS = [
  'One score across resume, interviews, coding and aptitude',
  'A roadmap that puts your weakest area first',
  'Your progress is saved to your account',
];

function Shield() {
  return (
    <svg className="shield" viewBox="0 0 200 200" aria-hidden="true">
      <circle className="orbit" cx="100" cy="100" r="86" />
      <circle className="orbit" cx="100" cy="100" r="64" />
      <g className="spin1">
        <circle className="dotc" cx="100" cy="14" r="5" />
      </g>
      <g className="spin2">
        <circle className="dotv" cx="100" cy="36" r="4" />
      </g>
      <path
        className="sh"
        d="M100 52l34 12v26c0 24-14 40-34 50-20-10-34-26-34-50V64z"
      />
      <path className="ck" d="M84 98l12 12 22-24" />
    </svg>
  );
}
function UserPlus() {
  return (
    <svg className="shield" viewBox="0 0 200 200" aria-hidden="true">
      <circle className="orbit" cx="100" cy="100" r="86" />
      <circle className="orbit" cx="100" cy="100" r="64" />
      <g className="spin1">
        <circle className="dotc" cx="100" cy="14" r="5" />
      </g>
      <g className="spin2">
        <circle className="dotv" cx="100" cy="36" r="4" />
      </g>
      <circle className="usr" cx="96" cy="84" r="15" />
      <path className="usr" d="M66 138c0-17 13-28 30-28s30 11 30 28z" />
      <circle className="plus-bg" cx="136" cy="72" r="15" />
      <path className="plus" d="M136 65v14M129 72h14" />
    </svg>
  );
}
export default function AuthPage({ mode: initial = 'login' }) {
  const [mode, setMode] = useState(initial);
  const [f, setF] = useState({ name: '', email: '', password: '' });
  const [show, setShow] = useState(false),
    [busy, setBusy] = useState(false),
    [err, setErr] = useState('');
  const { login, register, isAuthed } = useAuth();
  const nav = useNavigate(),
    loc = useLocation();
  const from = loc.state?.from || '/',
    reason = loc.state?.reason,
    signup = mode === 'signup';

  useEffect(() => setMode(initial), [initial]);
  useEffect(() => {
    if (isAuthed) nav(from, { replace: true });
  }, [isAuthed]); // logged in -> go on
  useEffect(() => {
    const mv = (e) => {
      document.documentElement.style.setProperty('--mx', e.clientX + 'px');
      document.documentElement.style.setProperty('--my', e.clientY + 'px');
    };
    addEventListener('mousemove', mv);
    return () => removeEventListener('mousemove', mv);
  }, []);

  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  const switchTo = (m) => {
    setMode(m);
    setErr('');
  };

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    if (signup && !f.name.trim()) return setErr('Name is required');
    if (!EMAIL.test(f.email.trim()))
      return setErr('Enter a valid email address');
    if (f.password.length < 8 || f.password.length > 64)
      return setErr('Password must be 8-64 characters');
    setBusy(true);
    try {
      if (signup) await register(f.name.trim(), f.email.trim(), f.password);
      else await login(f.email.trim(), f.password);
    } catch (x) {
      setErr(x.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <div className="blobs" aria-hidden="true">
        <i />
        <i />
        <i />
      </div>
      <Particles />
      <div className="glow" aria-hidden="true" />
      <main className="auth">
        <aside className="auth-art">
          <Link className="logo rise" to="/">
            <Logo />
            Interview IQ
          </Link>
          <div className="rise" style={{ animationDelay: '120ms' }}>
            <div key={mode} className="art-swap">
              {signup ? <UserPlus /> : <Shield />}
            </div>
          </div>
          <h1 className="rise" style={{ animationDelay: '220ms' }}>
            {signup ? 'Create your account' : 'Welcome back'}
          </h1>
          <ul className="rise" style={{ animationDelay: '320ms' }}>
            {POINTS.map((p) => (
              <li key={p}>{p}</li>
            ))}
          </ul>
        </aside>

        <section className="auth-card glass" aria-labelledby="auth-h">
          <div className="tabs" role="tablist" data-mode={mode}>
            <i className="thumb" aria-hidden="true" />
            <button
              type="button"
              role="tab"
              aria-selected={!signup}
              onClick={() => switchTo('login')}
            >
              Log in
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={signup}
              onClick={() => switchTo('signup')}
            >
              Sign up
            </button>
          </div>
          <div key={mode} className="fade">
            <h2 id="auth-h">
              {signup ? 'Sign up for free' : 'Log in to continue'}
            </h2>
            <p className="sub">
              {reason ||
                (signup
                  ? 'It takes less than a minute.'
                  : 'Good to see you again.')}
            </p>
            {err && (
              <p className="err" role="alert" key={err}>
                {err}
              </p>
            )}
            <form onSubmit={submit} noValidate>
              {signup && (
                <label>
                  Full name
                  <input
                    value={f.name}
                    onChange={set('name')}
                    autoComplete="name"
                    maxLength={100}
                  />
                </label>
              )}
              <label>
                College email
                <input
                  type="email"
                  value={f.email}
                  onChange={set('email')}
                  autoComplete="email"
                />
              </label>
              <label>
                Password
                <div className="pw">
                  <input
                    type={show ? 'text' : 'password'}
                    value={f.password}
                    onChange={set('password')}
                    autoComplete={signup ? 'new-password' : 'current-password'}
                    maxLength={64}
                  />
                  <button
                    type="button"
                    className="eye"
                    onClick={() => setShow(!show)}
                  >
                    {show ? 'Hide' : 'Show'}
                  </button>
                </div>
                {signup && <small>8 to 64 characters</small>}
              </label>
              <Mag className="solid wide" disabled={busy}>
                {busy ? (
                  <>
                    <i className="spinner" />
                    Please wait
                  </>
                ) : signup ? (
                  'Create account'
                ) : (
                  'Log in'
                )}
              </Mag>
            </form>
            <p className="swap">
              <Link to="/">← Back to home</Link>
            </p>
          </div>
        </section>
      </main>
    </>
  );
}
