// Single place that talks to the backend and handles the JWT token.
// Today: auth-service directly (8081). After the API Gateway is ready, only change VITE_API_URL to http://localhost:8080
const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081';
const TOKEN_KEY = 'iiq_token',
  USER_KEY = 'iiq_user';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  user: () => {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY));
    } catch {
      return null;
    }
  },
  set: (token, user) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

// JWT payload: sub = userId, email, role, exp
export function decodeToken(token) {
  try {
    return JSON.parse(
      atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    );
  } catch {
    return null;
  }
}
export const isExpired = (token) => {
  const p = decodeToken(token);
  return !p || p.exp * 1000 <= Date.now();
};

// Use this for EVERY backend call. It adds "Authorization: Bearer <token>" automatically.
//   api("/resume/analyze", { method: "POST", body: formData })     (FormData is sent as multipart)
//   api("/auth/login", { method: "POST", body: {...}, auth: false })
export async function api(
  path,
  { method = 'GET', body, auth = true, headers = {} } = {}
) {
  const h = { ...headers },
    token = tokenStore.get();
  const isForm = typeof FormData !== 'undefined' && body instanceof FormData;
  if (body && !isForm) h['Content-Type'] = 'application/json';
  if (auth && token) h.Authorization = 'Bearer ' + token;
  let res;
  try {
    res = await fetch(BASE + path, {
      method,
      headers: h,
      body: body ? (isForm ? body : JSON.stringify(body)) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?');
  }
  const data = await res.json().catch(() => null); // backend errors look like { status, message, timestamp }
  if (!res.ok) {
    if (res.status === 401 && auth && token) {
      tokenStore.clear();
      window.dispatchEvent(new Event('iiq:logout'));
    }
    throw new ApiError(
      res.status,
      data?.message || 'Something went wrong. Please try again.'
    );
  }
  return data;
}
