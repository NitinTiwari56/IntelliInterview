import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './auth/AuthContext.jsx';
import Logo from './components/Logo.jsx';

const REDUCE =
  typeof matchMedia !== 'undefined' &&
  matchMedia('(prefers-reduced-motion: reduce)').matches;
const QUESTIONS = [
  'Your resume lists Spring Boot. Walk me through the hardest bug you fixed in that project.',
  'You used MySQL. How would you index a table with ten million rows?',
  'How does an API gateway validate a JWT before routing a request?',
];
const SCORES = [
  ['Resume', 68],
  ['Technical interview', 54],
  ['HR interview', 72],
  ['DSA', 41],
  ['Quantitative', 63],
  ['Logical', 77],
  ['Verbal', 58],
];
const MODULES = [
  [
    'Resume analysis',
    'Upload a PDF, pick a role, get an ATS-style score, your missing skills and what to rewrite. Every attempt is saved.',
    ['ATS score', 'Skill gap', 'History'],
    '#7C5CFF',
  ],
  [
    'AI interviews',
    'Technical and HR questions built from your resume, streamed live. Type, speak or go on video and get feedback per answer.',
    ['Text', 'Speech', 'Video'],
    '#3DE0E6',
  ],
  [
    'Coding practice',
    'DSA problems in an in-browser editor, run in a Judge0 sandbox against hidden test cases.',
    ['Java', 'C++', 'Python'],
    '#FFB84D',
  ],
  [
    'Aptitude tests',
    'Fresh quantitative, logical and verbal questions with a 30-second timer on every question.',
    ['2 min timer', '20 Qs', 'Review'],
    '#FF6B8B',
  ],
];
const STEPS = [
  ['Upload your resume', 'Pick the role you want. First score in seconds.'],
  [
    'Practise anywhere',
    'Interviews, coding, aptitude. Every result is scored out of 100.',
  ],
  [
    'Follow the roadmap',
    'Your weakest categories come first, with one clear action each.',
  ],
];
const ACTIONS = [
  ['Practise DP problems', 'DSA', 41],
  ['Retake the technical mock interview', 'Technical interview', 54],
  ['Take a verbal ability test', 'Verbal', 58],
];
// module card title -> page. Add a line here when a new module page is ready.const MODULE_ROUTES = { "Aptitude tests": "/aptitude", "Coding practice": "/coding" };
const MODULE_ROUTES = {
  'Resume analysis': '/resume',
  'AI interviews': '/interview',
  'Coding practice': '/coding',
  'Aptitude tests': '/aptitude',
};

const ICONS = [
  <svg key="r" className="ill" viewBox="0 0 120 90" aria-hidden="true">
    <rect className="st" x="22" y="6" width="58" height="76" rx="8" />
    <circle className="st" cx="38" cy="22" r="6" />
    <path className="st" d="M50 20h22M32 38h40M32 48h40M32 58h26" />
    <rect className="fl scan" x="26" y="10" width="50" height="3" rx="2" />
    <circle className="fl" cx="88" cy="64" r="15" />
    <path
      d="M81 64l5 5 9-10"
      fill="none"
      stroke="#07060F"
      strokeWidth="3"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>,
  <svg key="i" className="ill" viewBox="0 0 120 90" aria-hidden="true">
    <path
      className="st"
      d="M14 10h66a8 8 0 0 1 8 8v20a8 8 0 0 1-8 8H34l-12 10V46h-8a8 8 0 0 1-8-8V18a8 8 0 0 1 8-8z"
    />
    <circle className="fl dot" cx="38" cy="28" r="3.5" />
    <circle
      className="fl dot"
      cx="50"
      cy="28"
      r="3.5"
      style={{ animationDelay: '.25s' }}
    />
    <circle
      className="fl dot"
      cx="62"
      cy="28"
      r="3.5"
      style={{ animationDelay: '.5s' }}
    />
    <rect className="st" x="40" y="54" width="68" height="30" rx="8" />
    {[52, 62, 72, 82, 92].map((x, k) => (
      <rect
        key={x}
        className="fl bar"
        x={x}
        y="62"
        width="4"
        height="14"
        rx="2"
        style={{ animationDelay: k * 0.15 + 's' }}
      />
    ))}
  </svg>,
  <svg key="c" className="ill" viewBox="0 0 120 90" aria-hidden="true">
    <rect className="st" x="6" y="8" width="108" height="74" rx="10" />
    <path className="st" d="M6 24h108" />
    <circle className="fl" cx="18" cy="16" r="2.5" />
    <circle className="fl" cx="28" cy="16" r="2.5" />
    <circle className="fl" cx="38" cy="16" r="2.5" />
    <path
      className="st draw"
      d="M38 42L24 54l14 12M82 42l14 12-14 12M66 40L54 68"
    />
    <rect className="fl caret2" x="100" y="68" width="6" height="3" rx="1.5" />
  </svg>,
  <svg key="a" className="ill" viewBox="0 0 120 90" aria-hidden="true">
    <rect className="st" x="53" y="6" width="14" height="8" rx="3" />
    <path className="st" d="M60 14v8M92 28l6-6" />
    <circle className="st faint" cx="60" cy="54" r="30" />
    <circle className="st ring" cx="60" cy="54" r="30" />
    <path className="st hand" d="M60 54V34" />
    <circle className="fl" cx="60" cy="54" r="3.5" />
    <path className="st" d="M60 28v3M60 77v3M34 54h3M83 54h3" />
  </svg>,
];

const Bot = () => (
  <svg className="bot" viewBox="0 0 64 64" aria-hidden="true">
    <path className="bst" d="M32 13V7" />
    <circle className="bfl beat" cx="32" cy="5" r="3" />
    <rect className="bst" x="10" y="14" width="44" height="34" rx="12" />
    <rect className="bfl eye" x="20" y="26" width="6" height="8" rx="3" />
    <rect className="bfl eye" x="38" y="26" width="6" height="8" rx="3" />
    <path className="bst mouth" d="M24 40q8 5 16 0" />
    <path className="bst" d="M4 28v8M60 28v8M22 48v6h20v-6" />
  </svg>
);

const SHOTS = [
  [
    'Dashboard',
    '/images/dashboard.png',
    'Your readiness and weak areas at a glance',
  ],
  [
    'AI interview',
    '/images/interview.png',
    'Live questions with instant feedback',
  ],
  ['Roadmap', '/images/roadmap.png', 'What to practise next, in order'],
];

function Shot({ t, src, d }) {
  const [ok, setOk] = useState(true);
  return (
    <figure className="shot glass">
      <div className="shot-img">
        {ok ? (
          <img
            src={src}
            alt={t + ' screen'}
            loading="lazy"
            onError={() => setOk(false)}
          />
        ) : (
          <div className="ph" aria-hidden="true">
            {t}
          </div>
        )}
      </div>
      <figcaption>
        <b>{t}</b>
        <span>{d}</span>
      </figcaption>
    </figure>
  );
}

export function Particles() {
  const ref = useRef(null);
  useEffect(() => {
    const cv = ref.current,
      ctx = cv.getContext('2d');
    let w,
      h,
      raf,
      pts = [],
      mx = -999,
      my = -999;
    const size = () => {
      w = cv.width = innerWidth;
      h = cv.height = innerHeight;
      pts = Array.from(
        { length: Math.min(70, Math.floor((w * h) / 22000)) },
        () => ({
          x: Math.random() * w,
          y: Math.random() * h,
          vx: (Math.random() - 0.5) * 0.25,
          vy: (Math.random() - 0.5) * 0.25,
        })
      );
    };
    const mv = (e) => {
      mx = e.clientX;
      my = e.clientY;
    };
    const line = (a, b, x, y, c, k) => {
      ctx.strokeStyle = `rgba(${c},${k})`;
      ctx.beginPath();
      ctx.moveTo(a, b);
      ctx.lineTo(x, y);
      ctx.stroke();
    };
    const draw = () => {
      ctx.clearRect(0, 0, w, h);
      for (const p of pts) {
        p.x += p.vx;
        p.y += p.vy;
        if (p.x < 0 || p.x > w) p.vx *= -1;
        if (p.y < 0 || p.y > h) p.vy *= -1;
        ctx.fillStyle = 'rgba(190,180,255,.7)';
        ctx.beginPath();
        ctx.arc(p.x, p.y, 1.3, 0, 7);
        ctx.fill();
      }
      for (let i = 0; i < pts.length; i++)
        for (let j = i + 1; j < pts.length; j++) {
          const d = Math.hypot(pts[i].x - pts[j].x, pts[i].y - pts[j].y);
          if (d < 120)
            line(
              pts[i].x,
              pts[i].y,
              pts[j].x,
              pts[j].y,
              '140,120,255',
              0.18 * (1 - d / 120)
            );
        }
      for (const p of pts) {
        const d = Math.hypot(p.x - mx, p.y - my);
        if (d < 160) line(p.x, p.y, mx, my, '61,224,230', 0.4 * (1 - d / 160));
      }
      raf = requestAnimationFrame(draw);
    };
    size();
    draw();
    if (REDUCE) cancelAnimationFrame(raf);
    addEventListener('resize', size);
    addEventListener('mousemove', mv);
    return () => {
      cancelAnimationFrame(raf);
      removeEventListener('resize', size);
      removeEventListener('mousemove', mv);
    };
  }, []);
  return <canvas ref={ref} className="particles" aria-hidden="true" />;
}

function Typed({ lines }) {
  const [t, setT] = useState('');
  useEffect(() => {
    let i = 0,
      j = 0,
      dead = false,
      to;
    const tick = () => {
      if (dead) return;
      const s = lines[i];
      if (REDUCE) {
        setT(s);
        to = setTimeout(() => {
          i = (i + 1) % lines.length;
          tick();
        }, 4500);
        return;
      }
      if (j <= s.length) {
        setT(s.slice(0, j++));
        to = setTimeout(tick, 26);
      } else
        to = setTimeout(() => {
          j = 0;
          i = (i + 1) % lines.length;
          tick();
        }, 2600);
    };
    to = setTimeout(tick, 1800);
    return () => {
      dead = true;
      clearTimeout(to);
    };
  }, [lines]);
  return (
    <span>
      {t}
      <i className="caret" />
    </span>
  );
}

function Count({ to }) {
  const ref = useRef(null),
    [v, setV] = useState(0);
  useEffect(() => {
    const io = new IntersectionObserver(
      ([e]) => {
        if (!e.isIntersecting) return;
        io.disconnect();
        const t0 = performance.now();
        const step = (n) => {
          const k = Math.min(1, (n - t0) / 1500);
          setV(Math.round(to * (1 - Math.pow(1 - k, 3))));
          if (k < 1) requestAnimationFrame(step);
        };
        requestAnimationFrame(step);
      },
      { threshold: 0.4 }
    );
    io.observe(ref.current);
    return () => io.disconnect();
  }, [to]);
  return <span ref={ref}>{v}</span>;
}

function Tilt({ children, className = '' }) {
  const ref = useRef(null);
  const mv = (e) => {
    const r = ref.current.getBoundingClientRect(),
      x = (e.clientX - r.left) / r.width - 0.5,
      y = (e.clientY - r.top) / r.height - 0.5;
    ref.current.style.transform = `perspective(800px) rotateY(${x * 10}deg) rotateX(${-y * 10}deg)`;
    ref.current.style.setProperty('--gx', (x + 0.5) * 100 + '%');
    ref.current.style.setProperty('--gy', (y + 0.5) * 100 + '%');
  };
  return (
    <div
      ref={ref}
      className={'glass tilt ' + className}
      onMouseMove={mv}
      onMouseLeave={() => (ref.current.style.transform = '')}
    >
      {children}
    </div>
  );
}

export function Mag({ children, className = '', onClick, ...rest }) {
  const ref = useRef(null);
  const mv = (e) => {
    const r = ref.current.getBoundingClientRect();
    ref.current.style.transform = `translate(${(e.clientX - r.left - r.width / 2) * 0.25}px,${(e.clientY - r.top - r.height / 2) * 0.35}px)`;
  };
  return (
    <button
      ref={ref}
      className={'btn ' + className}
      onClick={onClick}
      {...rest}
      onMouseMove={mv}
      onMouseLeave={() => (ref.current.style.transform = '')}
    >
      <span>{children}</span>
    </button>
  );
}

const Words = ({ text, delay }) =>
  text.split(' ').map((w, i) => (
    <span className="w" key={i}>
      <span style={{ animationDelay: delay + i * 70 + 'ms' }}>{w}&nbsp;</span>
    </span>
  ));

export default function App() {
  const nav = useNavigate(),
    { user, logout } = useAuth();
  const [drag, setDrag] = useState(false);
  const open =
    (mode, reason = '') =>
    () =>
      nav(mode === 'signup' ? '/signup' : '/login', { state: { reason } });
  const [toast, setToast] = useState('');
  const flash = (m) => {
    setToast(m);
    setTimeout(() => setToast(''), 2800);
  };
  const gate = () =>
    user
      ? nav('/resume')
      : nav('/login', {
          state: {
            from: '/resume',
            reason: 'Log in to upload your resume and see your score.',
          },
        });
  const goModule = (title) => {
    const p = MODULE_ROUTES[title];
    if (!p) return;
    user
      ? nav(p)
      : nav('/login', {
          state: { from: p, reason: 'Log in to start practising.' },
        });
  };
  useEffect(() => {
    const io = new IntersectionObserver(
      (es) =>
        es.forEach(
          (e) =>
            e.isIntersecting &&
            (e.target.classList.add('in'), io.unobserve(e.target))
        ),
      { threshold: 0.15 }
    );
    document.querySelectorAll('.rv').forEach((el) => io.observe(el));
    let raf;
    const mv = (e) => {
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        document.documentElement.style.setProperty('--mx', e.clientX + 'px');
        document.documentElement.style.setProperty('--my', e.clientY + 'px');
      });
    };
    addEventListener('mousemove', mv);
    return () => {
      io.disconnect();
      removeEventListener('mousemove', mv);
    };
  }, []);

  return (
    <>
      <div className="curtain" aria-hidden="true">
        <Logo size={72} />
      </div>
      <div className="blobs" aria-hidden="true">
        <i />
        <i />
        <i />
      </div>
      <Particles />
      <div className="glow" aria-hidden="true" />

      <header className="nav">
        <a className="logo" href="#top">
          <Logo />
          Interview IQ
        </a>
        <nav aria-label="Main">
          <a href="#top">Home</a>
          <a href="#modules">Modules</a>
          <a href="#how">How it works</a>
          <a href="#preview">Preview</a>
          <a href="#roadmap">Roadmap</a>
          <a href="#contact">Contact</a>
        </nav>
        <div className="nav-cta">
          {user ? (
            <>
              <Mag className="ghost" onClick={() => nav('/dashboard')}>
                Dashboard
              </Mag>
              <span className="hi">Hi, {user.name.split(' ')[0]}</span>
              <Mag className="solid" onClick={logout}>
                Logout
              </Mag>
            </>
          ) : (
            <>
              <Mag className="ghost" onClick={open('login')}>
                Log in
              </Mag>
              <Mag className="solid" onClick={open('signup')}>
                Sign up
              </Mag>
            </>
          )}
        </div>
      </header>

      <main id="top">
        <section className="hero">
          <div>
            <h1>
              <Words text="Prepare smarter. Get placed." delay={1300} />
            </h1>
            <p className="lead rise" style={{ animationDelay: '2100ms' }}>
              Resume, interviews, coding and aptitude in one place.
            </p>
            <div
              className={'drop glass rise' + (drag ? ' over' : '')}
              style={{ animationDelay: '2300ms' }}
              role="button"
              tabIndex={0}
              onClick={gate}
              onKeyDown={(e) =>
                (e.key === 'Enter' || e.key === ' ') &&
                (e.preventDefault(), gate(e))
              }
              onDragOver={(e) => {
                e.preventDefault();
                setDrag(true);
              }}
              onDragLeave={() => setDrag(false)}
              onDrop={(e) => {
                e.preventDefault();
                setDrag(false);
                gate(e);
              }}
            >
              <span className="drop-ic" aria-hidden="true">
                PDF
              </span>
              <span>
                <strong>Drop your resume here</strong>
                <br />
                or click to choose a file. We score it against your target role.
              </span>
            </div>
          </div>
          <div
            className="live glass rise"
            style={{ animationDelay: '1900ms' }}
            aria-label="Live AI interview preview"
          >
            <div className="live-top">
              <div className="avatar">
                <Bot />
              </div>
              <div>
                <b>AI interviewer</b>
                <br />
                <span className="muted">Technical round · question 2 of 5</span>
              </div>
              <span className="rec">
                <i />
                Listening
              </span>
            </div>
            <p className="q">
              <Typed lines={QUESTIONS} />
            </p>
            <div className="wave" aria-hidden="true">
              {Array.from({ length: 44 }, (_, k) => (
                <i
                  key={k}
                  style={{
                    '--i': k,
                    '--m': (
                      0.3 +
                      0.7 * Math.sin((Math.PI * (k + 0.5)) / 44)
                    ).toFixed(2),
                  }}
                />
              ))}
            </div>
            <div className="live-foot">
              <span>
                Answer score <b>8/10</b>
              </span>
              <span>Feedback streams as you speak</span>
            </div>
          </div>
        </section>

        <section id="modules" className="sec">
          <h2 className="rv up">Everything you need to prepare</h2>
          <div className="grid">
            {MODULES.map(([t, d, chips, c], i) => (
              <div
                className="rv up"
                style={{ transitionDelay: i * 90 + 'ms' }}
                key={t}
              >
                <Tilt>
                  <span className="ill-wrap" style={{ '--c': c }}>
                    {ICONS[i]}
                  </span>
                  <h3>{t}</h3>
                  <p>{d}</p>
                  <div className="chips">
                    {chips.map((x) => (
                      <span key={x}>{x}</span>
                    ))}
                  </div>
                  <button
                    className="card-go"
                    disabled={!MODULE_ROUTES[t]}
                    onClick={() => goModule(t)}
                  >
                    {MODULE_ROUTES[t] ? 'Start practising →' : 'Coming soon'}
                  </button>
                </Tilt>
              </div>
            ))}
          </div>
        </section>

        <section id="how" className="sec">
          <h2 className="rv up">How it works</h2>
          <ol className="steps rv">
            {STEPS.map(([t, d], i) => (
              <li key={t} style={{ '--s': i }}>
                <span className="n">{i + 1}</span>
                <h3>{t}</h3>
                <p>{d}</p>
              </li>
            ))}
          </ol>
        </section>

        <section id="preview" className="sec">
          <h2 className="rv up">See it in action</h2>
          <div className="shots">
            {SHOTS.map(([t, src, d], i) => (
              <div
                className="rv up"
                style={{ transitionDelay: i * 90 + 'ms' }}
                key={t}
              >
                <Shot t={t} src={src} d={d} />
              </div>
            ))}
          </div>
        </section>

        <section id="roadmap" className="sec split">
          <div className="rv up">
            <h2>Your personal roadmap</h2>
            <p className="lead">
              Every module score is put on a 0–100 scale. Below 60, or ranked
              lowest, means weak. Weak areas are ordered by priority.
            </p>
          </div>
          <div
            className="rv up glass dash"
            style={{ transitionDelay: '120ms' }}
          >
            <div className="dash-top">
              <span className="muted">Placement readiness</span>
              <span className="pill">Sample data</span>
            </div>
            <div className="score">
              <b>
                <Count to={62} />
              </b>
              <span>/100</span>
            </div>
            <ul className="bars">
              {SCORES.map(([n, v], i) => (
                <li key={n} className={v < 60 ? 'weak' : ''}>
                  <span>{n}</span>
                  <div className="track">
                    <i style={{ '--w': v + '%', '--d': i * 90 + 'ms' }} />
                  </div>
                  <em>{v}</em>
                </li>
              ))}
            </ul>
            <ul className="acts">
              {ACTIONS.map(([a, c, s]) => (
                <li key={a}>
                  <b>{a}</b>
                  <span>
                    {c} · now {s}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        </section>

        <section className="cta rv up">
          <h2>Start with your resume</h2>
          <Mag className="solid lg" onClick={open('signup')}>
            Create free account
          </Mag>
        </section>
      </main>
      {toast && (
        <div className="toast" role="status">
          {toast}
        </div>
      )}

      <footer className="foot" id="contact">
        <span>
          <b>Interview IQ</b> · Department of Information Technology, IET
          (DAVV), Indore
        </span>
        <span>Manasvi Mahajan · Nitin Tiwari · Khushi Jalandra</span>
      </footer>
    </>
  );
}
