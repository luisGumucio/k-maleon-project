import { Refine } from "@refinedev/core";
import {
  ErrorComponent,
  ThemedLayout,
  ThemedSider,
  useNotificationProvider,
} from "@refinedev/antd";
import routerProvider, {
  UnsavedChangesNotifier,
  DocumentTitleHandler,
  NavigateToResource,
} from "@refinedev/react-router";
import { RefineKbar } from "@refinedev/kbar";
import {
  DashboardOutlined,
  UserOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import { Outlet, Route, Routes } from "react-router";
import { Header } from "../header";
import { dataProvider } from "../../providers/data";
import { authProvider } from "../../providers/auth";
import { i18nProvider } from "../../i18n/provider";
import { SuperAdminDashboard } from "../../pages/super/dashboard";
import { UserList } from "../../pages/users/list";
import { SettingsPage } from "../../pages/settings";

export const SuperAdminLayout: React.FC = () => {
  return (
    <Refine
      dataProvider={dataProvider}
      authProvider={authProvider}
      i18nProvider={i18nProvider}
      notificationProvider={useNotificationProvider}
      routerProvider={routerProvider}
      resources={[
        {
          name: "super-dashboard",
          list: "/super/dashboard",
          meta: { label: "Dashboard", icon: <DashboardOutlined /> },
        },
        {
          name: "users",
          list: "/users",
          meta: { label: "Usuarios", icon: <UserOutlined /> },
        },
        {
          name: "settings",
          list: "/settings",
          meta: { label: "Configuraciones", icon: <SettingOutlined /> },
        },
      ]}
      options={{
        syncWithLocation: true,
        warnWhenUnsavedChanges: true,
      }}
    >
      <Routes>
        <Route
          element={
            <ThemedLayout
              Header={() => <Header sticky />}
              Sider={(props) => <ThemedSider {...props} fixed />}
            >
              <Outlet />
            </ThemedLayout>
          }
        >
          <Route
            index
            element={<NavigateToResource resource="super-dashboard" />}
          />
          <Route path="/super/dashboard" element={<SuperAdminDashboard />} />
          <Route path="/users" element={<UserList />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="*" element={<ErrorComponent />} />
        </Route>
      </Routes>

      <RefineKbar />
      <UnsavedChangesNotifier />
      <DocumentTitleHandler />
    </Refine>
  );
};
