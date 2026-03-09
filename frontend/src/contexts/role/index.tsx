import React, { createContext, useContext, useEffect, useState } from "react";

export type AppRole = "super_admin" | "admin" | "inventory_admin" | "branch_manager";

interface RoleContextType {
  role: AppRole;
  viewingAs: AppRole | null;
  setViewingAs: (role: AppRole | null) => void;
}

export const RoleContext = createContext<RoleContextType>({
  role: "admin",
  viewingAs: null,
  setViewingAs: () => {},
});

export const useRole = () => useContext(RoleContext);

const VALID_ROLES: AppRole[] = ["super_admin", "admin", "inventory_admin", "branch_manager"];

function getRole(): AppRole {
  const stored = localStorage.getItem("kmaleon_role");
  if (stored && VALID_ROLES.includes(stored as AppRole)) {
    return stored as AppRole;
  }
  return "admin";
}

export const RoleContextProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [role, setRole] = useState<AppRole>(getRole);
  const [viewingAs, setViewingAs] = useState<AppRole | null>(null);

  // Sincronizar si el localStorage cambia (login/logout)
  useEffect(() => {
    const onStorage = () => setRole(getRole());
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  return (
    <RoleContext.Provider value={{ role, viewingAs, setViewingAs }}>
      {children}
    </RoleContext.Provider>
  );
};
