import { RefineKbarProvider } from "@refinedev/kbar";
import "@refinedev/antd/dist/reset.css";
import { App as AntdApp } from "antd";
import { BrowserRouter, Route, Routes } from "react-router";
import { ColorModeContextProvider } from "./contexts/color-mode";
import { RoleContextProvider, useRole } from "./contexts/role";
import { AdminLayout } from "./components/layouts/AdminLayout";
import { BranchLayout } from "./components/layouts/BranchLayout";
import { InventoryLayout } from "./components/layouts/InventoryLayout";
import { SuperAdminLayout } from "./components/layouts/SuperAdminLayout";
import { LoginPage } from "./pages/login";
import "./i18n";

function AppRouter() {
  const { role, viewingAs } = useRole();

  // Sin sesión → login
  const token = localStorage.getItem("kmaleon_token");
  if (!token) {
    return (
      <Routes>
        <Route path="*" element={<LoginPage />} />
      </Routes>
    );
  }

  // super_admin navegando a un panel hijo
  if (role === "super_admin" && viewingAs === "admin") {
    return <AdminLayout fromSuperAdmin />;
  }
  if (role === "super_admin" && viewingAs === "inventory_admin") {
    return <InventoryLayout fromSuperAdmin />;
  }

  // Layout por rol base
  if (role === "super_admin") return <SuperAdminLayout />;
  if (role === "inventory_admin") return <InventoryLayout />;
  if (role === "branch_manager") return <BranchLayout />;
  return <AdminLayout />;
}

function App() {
  return (
    <BrowserRouter>
      <RefineKbarProvider>
        <ColorModeContextProvider>
          <AntdApp>
            <RoleContextProvider>
              <AppRouter />
            </RoleContextProvider>
          </AntdApp>
        </ColorModeContextProvider>
      </RefineKbarProvider>
    </BrowserRouter>
  );
}

export default App;
