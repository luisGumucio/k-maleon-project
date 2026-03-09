import React, { createContext, useContext, useState } from "react";

export type AppRole = "super_admin" | "admin" | "inventory_admin" | "branch_manager";

interface RoleContextType {
  role: AppRole;
  // viewingAs: rol temporal cuando super_admin navega a otro panel
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

function getMockRole(): AppRole {
  const envRole = import.meta.env.VITE_MOCK_ROLE as string | undefined;
  if (envRole && VALID_ROLES.includes(envRole as AppRole)) {
    return envRole as AppRole;
  }
  return "admin";
}

export const RoleContextProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const role = getMockRole();
  const [viewingAs, setViewingAs] = useState<AppRole | null>(null);

  return (
    <RoleContext.Provider value={{ role, viewingAs, setViewingAs }}>
      {children}
    </RoleContext.Provider>
  );
};
