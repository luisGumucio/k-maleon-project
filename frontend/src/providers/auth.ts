import { AuthProvider } from "@refinedev/core";
import { API_URL } from "./constants";

const TOKEN_KEY = "kmaleon_token";
const ROLE_KEY = "kmaleon_role";
const NAME_KEY = "kmaleon_name";
const LOCATION_KEY = "kmaleon_location_id";

export const authProvider: AuthProvider = {
  login: async ({ email, password }) => {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      return {
        success: false,
        error: { name: "Login failed", message: "Email o contraseña incorrectos" },
      };
    }

    const data = await response.json();
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(ROLE_KEY, data.role);
    localStorage.setItem(NAME_KEY, data.name ?? "");
    if (data.locationId) localStorage.setItem(LOCATION_KEY, data.locationId);
    else localStorage.removeItem(LOCATION_KEY);

    return { success: true, redirectTo: "/" };
  },

  logout: async () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      fetch(`${API_URL}/api/auth/logout`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      }).catch(() => {});
    }
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(NAME_KEY);
    localStorage.removeItem(LOCATION_KEY);
    window.location.href = "/";
    return { success: true };
  },

  check: async () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      return { authenticated: false, redirectTo: "/login" };
    }

    const response = await fetch(`${API_URL}/api/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    if (!response.ok) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(ROLE_KEY);
      localStorage.removeItem(NAME_KEY);
      return { authenticated: false, redirectTo: "/login" };
    }

    const data = await response.json();
    localStorage.setItem(ROLE_KEY, data.role);
    localStorage.setItem(NAME_KEY, data.name ?? "");
    if (data.locationId) localStorage.setItem(LOCATION_KEY, data.locationId);
    else localStorage.removeItem(LOCATION_KEY);

    return { authenticated: true };
  },

  getPermissions: async () => {
    return localStorage.getItem(ROLE_KEY);
  },

  getIdentity: async () => {
    const name = localStorage.getItem(NAME_KEY);
    const role = localStorage.getItem(ROLE_KEY);
    if (!role) return null;
    return { name: name ?? role, role };
  },

  onError: async (error) => {
    if (error?.status === 401 || error?.status === 403) {
      return { logout: true, redirectTo: "/login" };
    }
    return { error };
  },
};

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getLocationId(): string | null {
  return localStorage.getItem(LOCATION_KEY);
}
